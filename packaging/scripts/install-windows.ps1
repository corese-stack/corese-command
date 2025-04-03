# Corese-Command Windows Installer & Updater

$InstallDir = "$env:USERPROFILE\.corese-command"
$BinName = "corese"
$JarName = "corese-command-standalone.jar"
$WrapperPath = "$InstallDir\$BinName.cmd"
$GitHubRepo = "corese-stack/corese-command"
$ReleaseApi = "https://api.github.com/repos/$GitHubRepo/releases"

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
    if ($versionLine -match 'version "(\d+)\.(\d+)\.(\d+)') {
        $major = [int]$Matches[1]
    }
    elseif ($versionLine -match "version ""(\d+)") {
        $major = [int]$Matches[1]
    }

    if ($null -eq $major) {
        Write-Host "Unable to detect Java version."
        Ask-Java-Install
        return
    }

    if ($major -lt 11) {
        Write-Host "Java 11 or higher is required (found: $major)."
        Ask-Java-Install
    } else {
        Write-Host "Java version $major detected."
    }
    Write-Host ""
}

function Ask-Java-Install {
    $ans = Read-Host "Java 21 required. Install it manually and press Enter to continue (or type N to abort)"
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
        $index = 0  # par défaut, index 0 = première version (triée)
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
    $assetUrl = Invoke-RestMethod "$ReleaseApi/tags/$version" |
        Select-Object -ExpandProperty assets |
        Where-Object { $_.name -eq $JarName } |
        Select-Object -ExpandProperty browser_download_url

    if (-not $assetUrl) {
        Write-Error "Could not find $JarName in release $version"
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

Main

