<!-- markdownlint-disable MD033 -->

# Installation

## Linux

<a href="https://flathub.org/fr/apps/fr.inria.corese.CoreseCommand">
  <img src="./_static/logo/badge_flathub.svg" alt="Flathub" width="140">
</a>

```bash
curl -fsSL https://raw.githubusercontent.com/corese-stack/corese-command/main/packaging/scripts/install-linux.sh -o /tmp/corese.sh && bash /tmp/corese.sh
```

## macOS

```bash
curl -fsSL https://raw.githubusercontent.com/corese-stack/corese-command/main/packaging/scripts/install-macos.sh -o /tmp/corese.sh && bash /tmp/corese.sh
```

## Windows

```powershell
iwr -useb https://raw.githubusercontent.com/corese-stack/corese-command/main/packaging/scripts/install-windows.ps1 | iex
```

## Manual / Cross-platform Installation

Use Corese CLI as a standalone `.jar`, or add it to your Java project via Maven.  
> Requires Java 11 or higher.

<a href="https://github.com/corese-stack/corese-command/releases">
  <img src="./_static/logo/badge_github.svg" alt="GitHub Release" width="140">
</a>
<a href="https://central.sonatype.com/artifact/fr.inria.corese/corese-command">
  <img src="./_static/logo/badge_maven.svg" alt="Maven Central" width="140">
</a>

## Uninstall / Update

If you installed Corese using one of the scripts above, you can uninstall or update it by simply running the same script again.
