#!/usr/bin/env bash

# ------------------------------------------------------------------------------
# Corese-Command macOs Installer
# ------------------------------------------------------------------------------
# This script installs or updates the Corese-Command CLI on a macOs system.
# It automatically checks for Java (>= 11), installs it if necessary,
# fetches the desired version of Corese-Command from GitHub, and adds
# the binary to the user's PATH via shell configuration files.
#
# Usage:
#   ./install.sh                     # Interactive mode
#   ./install.sh --install <version> # Install a specific version (e.g. v4.4.1)
#   ./install.sh --install-latest    # Install the latest available version
#   ./install.sh --uninstall         # Remove Corese-Command from the system
# ------------------------------------------------------------------------------


INSTALL_DIR="$HOME/.local/corese-command"
BIN_NAME="corese"
WRAPPER_PATH="$INSTALL_DIR/$BIN_NAME"
JAR_NAME="corese-command-standalone.jar"
GITHUB_REPO="corese-stack/corese-command"
RELEASE_API="https://api.github.com/repos/$GITHUB_REPO/releases"

check_internet() {
    echo "🌐 Checking internet connection..."
    if ! curl -s --max-time 5 https://github.com/ > /dev/null; then
        echo "❌ No internet connection or GitHub is unreachable. Please connect and retry."
        exit 1
    fi
    echo "✅ Internet connection is OK."
    echo
}

check_java() {
    echo "🔍 Checking Java..."

    if ! command -v java &> /dev/null; then
        echo "❌ Java is not installed."
        prompt_install_java
        check_java
        return
    fi

    set +e
    JAVA_OUTPUT=$(java -version 2>&1)
    JAVA_EXIT_CODE=$?
    set -e

    if [ "$JAVA_EXIT_CODE" -ne 0 ]; then
        echo "❌ Failed to execute java -version."
        prompt_install_java
        check_java
        return
    fi

    if echo "$JAVA_OUTPUT" | grep -qE "No Java runtime present|Unable to locate a Java Runtime"; then
        echo "❌ Java is not actually installed (Apple stub detected)."
        prompt_install_java
        check_java
        return
    fi

    JAVA_VERSION=$(echo "$JAVA_OUTPUT" | grep -oE 'version "([0-9]+)' | grep -oE '[0-9]+')

    if ! [[ "$JAVA_VERSION" =~ ^[0-9]+$ ]]; then
        echo "⚠️ Unable to detect a valid Java version."
        prompt_install_java
        check_java
        return
    fi

    if [ "$JAVA_VERSION" -lt 11 ]; then
        echo "❌ Java version 11 or higher is required (found: $JAVA_VERSION)."
        prompt_install_java
        check_java
    else
        echo "✅ Java version $JAVA_VERSION detected."
    fi

    echo
}

prompt_install_java() {
    read -rp "→ Install OpenJDK 21 using Homebrew? [Y/n] " answer
    if [[ "$answer" =~ ^[Nn]$ ]]; then
        echo "❌ Java is required. Aborting."
        exit 1
    fi

    if ! command -v brew >/dev/null 2>&1; then
        echo "❌ Homebrew is not installed. Please install it first: https://brew.sh/"
        exit 1
    fi

    echo "📦 Installing OpenJDK 21..."
    brew install openjdk@21

    echo "🔗 Linking Java 21 into your environment..."
    sudo ln -sfn "$(brew --prefix)/opt/openjdk@21/libexec/openjdk.jdk" \
     /Library/Java/JavaVirtualMachines/openjdk-21.jdk

    echo "✅ Java 21 installed. Test:"
    /usr/libexec/java_home -v 21
    java -version
}

list_versions() {
    curl -s "$RELEASE_API" | grep '"tag_name":' | cut -d '"' -f 4 | uniq
}

choose_version() {
    echo "📦 Available versions:"
    VERSIONS=()
    while IFS= read -r line; do
        VERSIONS+=("$line")
    done < <(list_versions)

    for i in "${!VERSIONS[@]}"; do
        if [ "$i" -eq 0 ]; then
            printf "   [%d] %s (latest)\n" $((i + 1)) "${VERSIONS[$i]}"
        else
            printf "   [%d] %s\n" $((i + 1)) "${VERSIONS[$i]}"
        fi
    done

    echo -n "→ Enter the number of the version to install [default: 1]: "
    read -r VERSION_INDEX

    if [[ -z "$VERSION_INDEX" || ! "$VERSION_INDEX" =~ ^[0-9]+$ || "$VERSION_INDEX" -lt 1 || "$VERSION_INDEX" -gt "${#VERSIONS[@]}" ]]; then
        VERSION_INDEX=1
    fi

    VERSION_TAG="${VERSIONS[$((VERSION_INDEX - 1))]}"
    echo
    echo "✔️  Selected version: $VERSION_TAG"
    echo
}

display_installed_version() {
    echo "📦 Current installation:"
    if [ -f "$INSTALL_DIR/$JAR_NAME" ]; then
        if command -v java &>/dev/null; then
            set +e
            RAW_VERSION=$(java -jar "$INSTALL_DIR/$JAR_NAME" -V 2>/dev/null | head -n 1)
            set -e
            echo "   ✔️ Installed: ${RAW_VERSION:-unknown}"
        else
            echo "   ⚠️  Java missing, unable to detect version"
        fi
    else
        echo "   ❌ No version installed."
    fi
    echo
}

download_and_install() {
    mkdir -p "$INSTALL_DIR"
    cd "$INSTALL_DIR" || exit 1

    echo "⬇️  Downloading Corese-Command $VERSION_TAG..."

    # Vérifie si le tag existe
    if ! RESPONSE=$(curl -s -f "$RELEASE_API/tags/$VERSION_TAG"); then
        echo
        echo "❌ Version '$VERSION_TAG' was not found on GitHub."
        echo
        echo "Available versions:"
        list_versions | sed 's/^/ - /'
        echo
        exit 1
    fi

    ASSET_URL=$(echo "$RESPONSE" | grep "browser_download_url" | grep "$JAR_NAME" | cut -d '"' -f 4 | head -n 1)

    if [[ -z "$ASSET_URL" ]]; then
        echo "❌ Could not find asset '$JAR_NAME' in release '$VERSION_TAG'."
        exit 1
    fi

    curl --progress-bar -L "$ASSET_URL" -o "$JAR_NAME"
    echo

    create_wrapper
    add_to_all_available_shell_rcs

    echo "✅ Corese-Command $VERSION_TAG installed successfully!"
    echo "🔧 Run it with: $BIN_NAME"
    echo "📁 Installed in: $INSTALL_DIR"
    echo
}

create_wrapper() {
    cat > "$WRAPPER_PATH" <<EOF
#!/usr/bin/env bash
java -jar "$INSTALL_DIR/$JAR_NAME" "\$@"
EOF
    chmod +x "$WRAPPER_PATH"
}

add_to_all_available_shell_rcs() {
    BLOCK_START="# >>> Corese-Command >>>"
    BLOCK_END="# <<< Corese-Command <<<"

    echo "🧩 Adding Corese-Command to available shell configs..."

    declare -a CONFIG_FILES=()

    command -v bash &>/dev/null && CONFIG_FILES+=("$HOME/.bash_profile")
    command -v zsh &>/dev/null && CONFIG_FILES+=("$HOME/.zshrc")
    command -v fish &>/dev/null && CONFIG_FILES+=("$HOME/.config/fish/config.fish")

    CONFIG_FILES+=("$HOME/.profile")

    for rc in "${CONFIG_FILES[@]}"; do
        mkdir -p "$(dirname "$rc")"

        if [[ -f "$rc" && "$(grep -F "$BLOCK_START" "$rc")" ]]; then
            echo "   ✔ Already added in $(basename "$rc")"
            continue
        fi

        echo "   ➕ Updating $(basename "$rc")"

        [ -f "$rc" ] && [ "$(tail -c1 "$rc")" != "" ] && echo "" >> "$rc"

        {
            echo "$BLOCK_START"
            if [[ "$rc" == *"fish"* ]]; then
                echo "set -gx PATH $PATH $INSTALL_DIR"
            else
                echo "export PATH=\"$INSTALL_DIR:\$PATH\""
            fi
            echo "$BLOCK_END"
        } >> "$rc"
    done

    echo
    echo "✅ Corese-Command path added."
    echo "🔁 Restart your terminal or run: source ~/.zshrc | source ~/.bash_profile | exec fish"
    echo
}

uninstall() {
    echo
    echo "⚠️  This will completely remove Corese-Command from your system."
    echo -n "→ Are you sure? [y/N] "
    read -r confirm
    if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
        echo "❌ Uninstall cancelled."
        echo
        exit 0
    fi

    echo "🗑️  Removing Corese-Command files..."
    rm -rf "$INSTALL_DIR"

    BLOCK_START="# >>> Corese-Command >>>"
    BLOCK_END="# <<< Corese-Command <<<"

    echo "🧹 Cleaning PATH from config files..."
    declare -a CONFIG_FILES=()

    [ -f "$HOME/.bash_profile" ] && CONFIG_FILES+=("$HOME/.bash_profile")
    [ -f "$HOME/.zshrc" ] && CONFIG_FILES+=("$HOME/.zshrc")
    [ -f "$HOME/.config/fish/config.fish" ] && CONFIG_FILES+=("$HOME/.config/fish/config.fish")
    [ -f "$HOME/.profile" ] && CONFIG_FILES+=("$HOME/.profile")

    for rc in "${CONFIG_FILES[@]}"; do
        if [ -f "$rc" ]; then
            sed -i '' "/$BLOCK_START/,/$BLOCK_END/d" "$rc"
            sed -i '' '/^$/N;/^\n$/D' "$rc"
            echo "   🧼 Cleaned $(basename "$rc")"
        fi
    done

    echo
    echo "✅ Corese-Command has been removed."
    echo
}

main() {
    echo
    echo "╭────────────────────────────────────────╮"
    echo "│         🧠 Corese-Command CLI          │"
    echo "│        macOS Installer & Updater       │"
    echo "╰────────────────────────────────────────╯"
    echo

    check_internet
    display_installed_version

    echo "┌──────────── Menu ─────────────┐"
    echo "│ [1] Install or update         │"
    echo "│ [2] Uninstall                 │"
    echo "│ [3] Exit                      │"
    echo "└───────────────────────────────┘"
    echo
    read -rp "👉 Select an option [1/2/3]: " choice

    case "$choice" in
        1)
            check_java
            choose_version
            download_and_install
            ;;
        2)
            uninstall
            ;;
        3)
            echo "👋 Goodbye!"
            exit 0
            ;;
        *)
            echo "❌ Invalid option."
            main
            ;;
    esac
}

# Platform check (macOs only)
if [[ "$(uname)" != "Darwin" ]]; then
    echo "❌ This installer is intended for macOs only."
    echo "Please use the Linux version instead."
    exit 1
fi

# Entry point
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    echo "Usage:"
    echo "  ./install.sh --install <version>       Install specific version"
    echo "  ./install.sh --install-latest          Install latest version"
    echo "  ./install.sh --uninstall               Uninstall Corese-Command"
    echo
    exit 0
fi

if [[ "$1" == "--install" && -n "$2" ]]; then
    VERSION_TAG="$2"
    check_java
    download_and_install
    exit 0
fi

if [[ "$1" == "--install-latest" ]]; then
    VERSION_TAG=$(list_versions | head -n 1)
    check_java
    download_and_install
    exit 0
fi

if [[ "$1" == "--uninstall" ]]; then
    uninstall
    exit 0
fi

main

