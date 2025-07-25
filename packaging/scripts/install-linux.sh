#!/usr/bin/env bash

# ------------------------------------------------------------------------------
# Corese-Command Linux Installer
# ------------------------------------------------------------------------------
# This script installs or updates the Corese-Command CLI on a Linux system.
# It automatically checks for Java (>= 21), installs it if necessary,
# fetches the desired version of Corese-Command from GitHub, and adds
# the binary to the user's PATH via shell configuration files.
#
# Usage:
#   ./install.sh                     # Interactive mode
#   ./install.sh --install <version> # Install a specific version (e.g. v4.4.1)
#   ./install.sh --install-latest    # Install the latest available version
#   ./install.sh --uninstall         # Remove Corese-Command from the system
# ------------------------------------------------------------------------------

set -e

XDG_DATA_HOME="${XDG_DATA_HOME:-$HOME/.local/share}"
INSTALL_DIR="${XDG_DATA_HOME:-$HOME/.local/share}/corese-command"
BIN_NAME="corese"
WRAPPER_PATH="$INSTALL_DIR/$BIN_NAME"
JAR_NAME="corese-command-standalone.jar"
GITHUB_REPO="corese-stack/corese-command"
RELEASE_API="https://api.github.com/repos/$GITHUB_REPO/releases"
AUTO_YES=0

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
        return
    fi

    JAVA_VERSION=$(java -version 2>&1 | grep -oE 'version "([0-9]+)' | grep -oE '[0-9]+')
    if [[ -z "$JAVA_VERSION" || "$JAVA_VERSION" -lt 21 ]]; then
        echo "❌ Java version 21 or higher is required (found: ${JAVA_VERSION:-unknown})."
        prompt_install_java
        return
    else
        echo "✅ Java version $JAVA_VERSION detected."
    fi
    echo
}

prompt_install_java() {
    if [[ "$AUTO_YES" -eq 1 ]]; then
        install_java_by_distro
        return
    fi
    
    echo -n "→ Install OpenJDK 21 now? [Y/n] "
    read -r answer
    if [[ "$answer" =~ ^[Nn]$ ]]; then
        echo "❌ Java is required. Aborting."
        exit 1
    fi
    install_java_by_distro
}

detect_distro() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release

        if [[ "$ID_LIKE" =~ (debian|ubuntu) ]]; then
            echo "debian"
        elif [[ "$ID" == "debian" || "$ID" == "ubuntu" || "$ID" == "pop" || "$ID" == "linuxmint" ]]; then
            echo "debian"
        elif [[ "$ID" == "fedora" || "$ID_LIKE" == "fedora" ]]; then
            echo "fedora"
        elif [[ "$ID" == "arch" ]]; then
            echo "arch"
        elif [[ "$ID" == "alpine" ]]; then
            echo "alpine"
        else
            echo "unknown"
        fi
    else
        echo "unknown"
    fi
}

install_java_by_distro() {
    DISTRO=$(detect_distro)
    echo "📦 Installing Java 21 on $DISTRO..."

    case "$DISTRO" in
        debian)
            sudo apt update && sudo apt install -y openjdk-21-jre ;;
        fedora)
            sudo dnf install -y java-21-openjdk ;;
        arch)
            sudo pacman -Sy --noconfirm jdk21-openjdk ;;
        alpine)
            if ! command -v apk &>/dev/null; then
                echo "❌ apk not found. Cannot install on Alpine."
                exit 1
            fi
            echo "📦 Installing openjdk21 using apk..."
            apk add --no-cache openjdk21 ;;
        *)
            echo "❌ Unsupported distro: $DISTRO"
            echo "Please install Java 21 or higher manually."
            exit 1 ;;
    esac
    echo
}

list_versions() {
    curl -s "$RELEASE_API" \
        | jq -r '.[] | select(.prerelease == false and .draft == false) | [.tag_name, .published_at] | @tsv' \
        | sort -k2 -r \
        | cut -f1
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

    while true; do
        echo -n "→ Enter the number of the version to install [default: 1]: "
        read -r VERSION_INDEX

        if [[ -z "$VERSION_INDEX" ]]; then
            VERSION_INDEX=1
            break
        elif [[ "$VERSION_INDEX" =~ ^[0-9]+$ && "$VERSION_INDEX" -ge 1 && "$VERSION_INDEX" -le "${#VERSIONS[@]}" ]]; then
            break
        else
            echo "❌ Invalid input. Please enter a number between 1 and ${#VERSIONS[@]}."
        fi
    done

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

    command -v bash &>/dev/null && CONFIG_FILES+=("$HOME/.bashrc")
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

        # Add a newline before the block only if the file doesn't already end with one
        [ -f "$rc" ] && [ "$(tail -c1 "$rc")" != "" ] && echo "" >> "$rc"

        {
            echo "$BLOCK_START"
            if [[ "$rc" == *"fish"* ]]; then
                echo "set -gx PATH \$PATH $INSTALL_DIR"
            else
                echo "export PATH=\"$INSTALL_DIR:\$PATH\""
            fi
            echo "$BLOCK_END"
        } >> "$rc"
    done

    echo
    echo "✅ Corese-Command path added."
    echo "🔁 Restart your terminal or run: source ~/.bashrc | source ~/.zshrc | exec fish"
    echo
}

uninstall() {
    echo
    if [[ "$AUTO_YES" -ne 1 ]]; then
        echo "⚠️  This will completely remove Corese-Command from your system."
        echo -n "→ Are you sure? [y/N] "
        read -r confirm
        if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
            echo "❌ Uninstall cancelled."
            echo
            exit 0
        fi
    fi

    echo "🗑️  Removing Corese-Command files..."
    rm -rf "$INSTALL_DIR"

    BLOCK_START="# >>> Corese-Command >>>"
    BLOCK_END="# <<< Corese-Command <<<"

    echo "🧹 Cleaning PATH from config files..."
    declare -a CONFIG_FILES=()

    [ -f "$HOME/.bashrc" ] && CONFIG_FILES+=("$HOME/.bashrc")
    [ -f "$HOME/.zshrc" ] && CONFIG_FILES+=("$HOME/.zshrc")
    [ -f "$HOME/.config/fish/config.fish" ] && CONFIG_FILES+=("$HOME/.config/fish/config.fish")
    [ -f "$HOME/.profile" ] && CONFIG_FILES+=("$HOME/.profile")

    for rc in "${CONFIG_FILES[@]}"; do
        if [ -f "$rc" ]; then
            sed -i "/$BLOCK_START/,/$BLOCK_END/d" "$rc"
            sed -i '/^$/N;/^\n$/D' "$rc"
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
    echo "│           Corese-Command CLI           │"
    echo "│        Linux Installer & Updater       │"
    echo "╰────────────────────────────────────────╯"
    echo

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
            check_internet
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

# Platform check (Linux only)
if [[ "$(uname)" == "Darwin" ]]; then
    echo "❌ This installer is intended for Linux only."
    echo "Please use the macOS version instead."
    exit 1
fi

# Entry point
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    echo "Usage:"
    echo "  ./install-linux-command.sh --install <version>       Install specific version"
    echo "  ./install-linux-command.sh --install-latest          Install latest version"
    echo "  ./install-linux-command.sh --uninstall               Uninstall Corese-Command"
    echo
    exit 0
fi

if [[ "$1" == "--install" && -n "$2" ]]; then
    AUTO_YES=1
    VERSION_TAG="$2"
    check_java
    download_and_install
    exit 0
fi

if [[ "$1" == "--install-latest" ]]; then
    AUTO_YES=1
    VERSION_TAG=$(list_versions | head -n 1)
    check_java
    download_and_install
    exit 0
fi

if [[ "$1" == "--uninstall" ]]; then
    AUTO_YES=1
    uninstall
    exit 0
fi

main
