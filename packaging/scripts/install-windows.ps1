# Corese-Command Windows Installer & Updater

<#
.SYNOPSIS
    Corese-Command CLI installer for Windows

.DESCRIPTION
    This PowerShell script installs, updates, or uninstalls the Corese-Command CLI on Windows.
    It checks for Java 21 or higher, prompts the user if Java is not found,
    and fetches the requested release from GitHub. It also adds Corese to the user's PATH.

.PARAMETER --install <version>
    Installs the specified version of Corese-Command (e.g., v4.4.1).

.PARAMETER --install-latest
    Automatically installs the latest available version.

.PARAMETER --uninstall
    Completely removes Corese-Command and cleans up the user's PATH.

.PARAMETER --help
    Displays usage instructions.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File install-windows.ps1 --install v4.4.1

.NOTES
    This script supports both interactive mode and CLI arguments.
    It should be executed with appropriate permissions to modify PATH.
#>

$InstallDir = "$env:USERPROFILE\.corese-command"
$BinName = "corese"
$JarName = "corese-command-standalone.jar"
$WrapperPath = "$InstallDir\$BinName.cmd"
$GitHubRepo = "corese-stack/corese-command"
$ReleaseApi = "https://api.github.com/repos/$GitHubRepo/releases"

# Argument parsing (for iex compatibility)
$Install = ""
$InstallLatest = $false
$Uninstall = $false
$Help = $false

for ($i = 0; $i -lt $args.Length; $i++) {
    switch ($args[$i]) {
        "--install" {
            if ($i + 1 -lt $args.Length) {
                $Install = $args[$i + 1]
                $i++
            }
        }
        "--install-latest" { $InstallLatest = $true }
        "--uninstall"      { $Uninstall = $true }
        "--help"           { $Help = $true }
    }
}

function Write-Centered($text) {
    $width = [console]::WindowWidth
    $padding = [Math]::Max(0, ($width - $text.Length) / 2)
    $line = (" " * [int]$padding) + $text
    Write-Host $line
}

function Check-Internet {
    Write-Host "Checking internet connection..."
    try {
        $result = Test-Connection -Count 1 -Quiet github.com
        if (-not $result) {
            throw "No response"
        }
    } catch {
        Write-Error "No internet connection. Please connect and try again."
        exit 1
    }
}

function Check-Java {
    Write-Host "Checking Java..."
    $java = Get-Command java -ErrorAction SilentlyContinue
    if (-not $java) {
        Write-Host "Java is not installed."
        Ask-Java-Install
        return
    }

    $versionOutput = & java -version 2>&1
    $versionLine = $versionOutput | Where-Object { $_ -match 'version' }

    $major = $null
    if ($versionLine -match 'version "(\d+)(\.(\d+))?') {
        $major = [int]$Matches[1]
    } elseif ($versionLine -match "version ""(\d+)") {
        $major = [int]$Matches[1]
    }

    if ($null -eq $major) {
        Write-Host "Unable to detect Java version."
        Ask-Java-Install
        return
    }

    if ($major -lt 21) {
        Write-Host "Java 21 or higher is required (found: $major)."
        Ask-Java-Install
    } else {
        Write-Host "Java version $major detected."
    }
    Write-Host ""
}

function Ask-Java-Install {
    $ans = Read-Host "Please install Java 21 or higher manually and press Enter to continue (or type N to abort)"
    if ($ans -match '^[Nn]') {
        exit 1
    }
}

function Get-Versions {
    try {
        $releases = Invoke-RestMethod "$ReleaseApi"
        return ($releases | Select-Object -ExpandProperty tag_name) |
            Sort-Object { [version]($_ -replace '[^\d.]') } -Descending
    } catch {
        Write-Error "Failed to fetch versions from GitHub"
        exit 1
    }
}

function Choose-Version {
    $versions = Get-Versions
    if (-not $versions) {
        Write-Error "No versions found."
        exit 1
    }

    Write-Host "`nAvailable versions:"
    $i = 1
    foreach ($v in $versions) {
        $label = if ($i -eq 1) { "$v (latest)" } else { "$v" }
        Write-Host " [$i] $label"
        $i++
    }

    $choice = Read-Host "`nEnter version number to install [default: 1]"

    if (-not $choice -match '^\d+$' -or [int]$choice -lt 1 -or [int]$choice -gt $versions.Count) {
        $index = 0
    } else {
        $index = [int]$choice - 1
    }

    return $versions[$index]
}

function Show-Installed-Version {
    if (Test-Path "$InstallDir\$JarName") {
        Write-Host "Corese-Command is currently installed:"
        try {
            & java @("-Dfile.encoding=UTF-8", "-jar", "$InstallDir\$JarName", "-V")
        } catch {
            Write-Host "Unable to run jar."
        }
    } else {
        Write-Host "No version currently installed."
    }
    Write-Host ""
}

function Download-And-Install($version) {
    if (-not (Test-Path $InstallDir)) {
        New-Item -ItemType Directory -Path $InstallDir | Out-Null
    }

    Write-Host "`nDownloading Corese-Command $version..."
    try {
        $release = Invoke-RestMethod "$ReleaseApi/tags/$version" -ErrorAction Stop
    } catch {
        Write-Host ""
        Write-Host "Error: the version '$version' was not found on GitHub." -ForegroundColor Red
        $allVersions = Get-Versions
        Write-Host "`nAvailable versions are:"
        foreach ($v in $allVersions) {
            Write-Host " - $v"
        }
        exit 1
    }

    $assetUrl = $release.assets |
        Where-Object { $_.name -eq $JarName } |
        Select-Object -ExpandProperty browser_download_url -ErrorAction SilentlyContinue

    if (-not $assetUrl) {
        Write-Warning "Could not find asset '$JarName' in release '$version'."
        exit 1
    }

    if (Get-Command curl.exe -ErrorAction SilentlyContinue) {
        & curl.exe -L -# -o "$InstallDir\$JarName" $assetUrl
    } else {
        Invoke-WebRequest $assetUrl -OutFile "$InstallDir\$JarName"
    }

    Write-Host "Creating launcher script..."
    $launcherContent = "@echo off`njava -Dfile.encoding=UTF-8 -jar `"$InstallDir\$JarName`" %*"
    Set-Content -Path $WrapperPath -Value $launcherContent -Encoding ASCII
    Write-Host "Wrapper created: $WrapperPath"

    Add-ToPath
}

function Add-ToPath {
    $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
    if ($userPath -notmatch [regex]::Escape($InstallDir)) {
        Write-Host "Adding Corese-Command to PATH..."
        $newPath = "$userPath;$InstallDir"
        [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
        Write-Host "Added to PATH (User)"
        Write-Host "Restart your terminal to use 'corese'"
    } else {
        Write-Host "Already in PATH"
    }
}

function Uninstall {
    $confirm = Read-Host "`nThis will remove Corese-Command. Are you sure? [y/N]"
    if ($confirm -notmatch '^[Yy]') {
        Write-Host "Uninstall cancelled."
        return
    }

    if (Test-Path $InstallDir) {
        Remove-Item -Recurse -Force $InstallDir
        Write-Host "Removed: $InstallDir"
    }

    $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
    if ($userPath -match [regex]::Escape($InstallDir)) {
        $newPath = ($userPath -split ';') -ne $InstallDir -join ';'
        [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
        Write-Host "Removed from PATH (User)"
    }

    Write-Host "Corese-Command has been uninstalled."
}

function Main {
    Write-Host ""
    Write-Centered "--------------------------------------------"
    Write-Centered "Corese-Command CLI - Windows Installer"
    Write-Centered "--------------------------------------------"
    Write-Host ""

    Check-Internet
    Show-Installed-Version

    Write-Host "Menu:"
    Write-Host " [1] Install or update"
    Write-Host " [2] Uninstall"
    Write-Host " [3] Exit"

    $opt = Read-Host "`nSelect an option [1/2/3]"
    switch ($opt) {
        1 {
            Check-Java
            $v = Choose-Version
            Download-And-Install $v
        }
        2 { Uninstall }
        3 { Write-Host "Goodbye!" }
        default { Write-Host "Invalid option." }
    }
}

# Handle args
if ($Help) {
    Write-Host "Usage:"
    Write-Host "  install.ps1 --install <version>       Install specific version"
    Write-Host "  install.ps1 --install-latest          Install latest version"
    Write-Host "  install.ps1 --uninstall               Uninstall Corese-Command"
    Write-Host "  install.ps1 --help                    Show this help"
    exit
}

if ($Install) {
    Check-Internet
    Check-Java
    Download-And-Install $Install
    exit
}

if ($InstallLatest) {
    Check-Internet
    Check-Java
    $v = (Get-Versions)[0]
    Download-And-Install $v
    exit
}

if ($Uninstall) {
    Uninstall
    exit
}

Main
