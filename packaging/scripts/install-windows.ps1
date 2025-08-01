# Corese-Command Windows Installer & Updater

<#
.SYNOPSIS
    Corese-Command CLI installer for Windows

.DESCRIPTION
    This PowerShell script installs, updates, or uninstalls the Corese-Command CLI on Windows.
    It checks for Java 21 or higher, prompts the user if Java is not found,
    and fetches the requested release from GitHub. It also adds Corese to the user's PATH.

.PARAMETER Install
    Installs the specified version of Corese-Command (e.g., v4.4.1).

.PARAMETER InstallLatest
    Automatically installs the latest available version.

.PARAMETER Uninstall
    Completely removes Corese-Command and cleans up the user's PATH.

.PARAMETER Help
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
$AutoYes = ($Install -or $InstallLatest) -or $Uninstall

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
        Write-Host "Internet connection is OK."
    } catch {
        Write-Error "No internet connection. Please connect and try again."
        exit 1
    }
    Write-Host ""
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
        Write-Host "Java is required. Aborting."
        exit 1
    }
}

function Get-Versions {
    try {
        $releases = Invoke-RestMethod "$ReleaseApi"
        return ($releases |
            Where-Object { -not $_.prerelease -and -not $_.draft } |
            Select-Object -ExpandProperty tag_name) |
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
    for ($i = 0; $i -lt $versions.Count; $i++) {
        $label = if ($i -eq 0) { "$($versions[$i]) (latest)" } else { $versions[$i] }
        Write-Host "   [$($i + 1)] $label"
    }

    while ($true) {
        $choice = Read-Host "`nEnter version number to install [default: 1]"

        if (-not $choice) {
            $index = 0
            break
        }
        elseif ($choice -match '^\d+$' -and [int]$choice -ge 1 -and [int]$choice -le $versions.Count) {
            $index = [int]$choice - 1
            break
        }
        else {
            Write-Host "Invalid input. Please enter a number between 1 and $($versions.Count)."
        }
    }

    Write-Host ""
    Write-Host "Selected version: $($versions[$index])"
    Write-Host ""
    return $versions[$index]
}

function Show-Installed-Version {
    Write-Host "Current installation:"
    if (Test-Path "$InstallDir\$JarName") {
        try {
            & java @("-Dfile.encoding=UTF-8", "-jar", "$InstallDir\$JarName", "-V")
        } catch {
            Write-Host "    Unable to run jar."
        }
    } else {
        Write-Host "   No version currently installed."
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
    Write-Host ""


    Write-Host "Creating launcher script..."
    $launcherContent = "@echo off`njava -Dfile.encoding=UTF-8 -jar `"$InstallDir\$JarName`" %*"
    Set-Content -Path $WrapperPath -Value $launcherContent -Encoding ASCII
    Write-Host "   Wrapper created: $WrapperPath"

    Add-ToPath
    Write-Host ""
    Write-Host "Corese-Command $version installed successfully!"
    Write-Host "Launch Corese-Command by running 'corese' in your terminal (terminal must be restarted)."
    Write-Host "Installed in: $InstallDir"
    Write-Host ""
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
    if (-not $Global:AutoYes) {
     $confirm = Read-Host "`nThis will remove Corese-Command from your system. Are you sure? [y/N]"
      if ($confirm -notmatch '^[Yy]') {
            Write-Host "Uninstall cancelled."
         return
        }
    }

    Write-Host "`nRemoving Corese-Command files..."
    if (Test-Path $InstallDir) {
        Remove-Item -Recurse -Force $InstallDir
        Write-Host "   Removed: $InstallDir"
    }

    # Remove the wrapper script
    $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
    if ($userPath -match [regex]::Escape($InstallDir)) {
        $newPath = ($userPath -split ';') -ne $InstallDir -join ';'
        [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
        Write-Host "Removed from PATH (User)"
    }

    Write-Host ""
    Write-Host "Corese-Command has been uninstalled."
    Write-Host ""
}

function Main {
    Write-Host ""
    Write-Centered "--------------------------------------------"
    Write-Centered "   Corese-Command CLI - Windows Installer"
    Write-Centered "--------------------------------------------"
    Write-Host ""

    Show-Installed-Version
    
    Write-Host "-------------- Menu --------------"
    Write-Host "| [1] Install or update          |"
    Write-Host "| [2] Uninstall                  |"
    Write-Host "| [3] Exit                       |"
    Write-Host "----------------------------------"
    
    $opt = Read-Host "`nSelect an option [1/2/3]"
    switch ($opt) {
        1 {
            Check-Internet
            Check-Java
            $v = Choose-Version
            Download-And-Install $v
        }
        2 { Uninstall }
        3 { Write-Host "Goodbye!" }
        default {
            Write-Host "Invalid option."
            Main
        }
    }
}

# Handle command line arguments
if ($Help) {
    Write-Host "Usage:"
    Write-Host "  installinstall-windows-command.ps1 --install [version]       Install specific version"
    Write-Host "  installinstall-windows-command.ps1 --install-latest          Install latest version"
    Write-Host "  installinstall-windows-command.ps1 --uninstall               Uninstall Corese-Command"
    Write-Host "  installinstall-windows-command.ps1 --help                    Show this help"
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
