param(
    [ValidateSet("app-image", "exe")]
    [string]$Type = "app-image",
    [string]$AppName = "SOC AI Bot",
    [string]$MavenPath = "",
    [string]$JdkHome = ""
)

$ErrorActionPreference = "Stop"

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path (Join-Path $ScriptRoot "..")
$JarName = "soc-ai-bot-java-1.0.0.jar"
$DistRoot = Join-Path $ProjectRoot "target\dist"
$PackageWorkDir = Join-Path $ProjectRoot "target\package"
$PackageInputDir = Join-Path $PackageWorkDir "input"
$IconPath = Join-Path $PackageWorkDir "soc-ai-bot.ico"

function Resolve-Maven {
    if ($MavenPath) {
        if (Test-Path -LiteralPath $MavenPath) { return (Resolve-Path $MavenPath).Path }
        throw "MavenPath does not exist: $MavenPath"
    }

    $mvn = Get-Command "mvn.cmd" -ErrorAction SilentlyContinue
    if (-not $mvn) { $mvn = Get-Command "mvn" -ErrorAction SilentlyContinue }
    if ($mvn) { return $mvn.Source }

    $intellijMaven = "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.1.3\plugins\maven\lib\maven3\bin\mvn.cmd"
    if (Test-Path -LiteralPath $intellijMaven) { return $intellijMaven }

    throw "Maven was not found. Install Maven, add it to PATH, or pass -MavenPath."
}

function Resolve-JPackage {
    if ($JdkHome) {
        $candidate = Join-Path $JdkHome "bin\jpackage.exe"
        if (Test-Path -LiteralPath $candidate) { return (Resolve-Path $candidate).Path }
        throw "jpackage.exe was not found under JdkHome: $JdkHome"
    }

    $jpackage = Get-Command "jpackage" -ErrorAction SilentlyContinue
    if ($jpackage) { return $jpackage.Source }

    throw "jpackage was not found. Use JDK 17+ and pass -JdkHome if needed."
}

function New-AppIcon {
    param([string]$Path)

    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $Path) | Out-Null
    Add-Type -AssemblyName System.Drawing

    $bitmap = [System.Drawing.Bitmap]::new(256, 256)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.Clear([System.Drawing.Color]::Transparent)

    $rect = [System.Drawing.Rectangle]::new(0, 0, 256, 256)
    $background = [System.Drawing.Drawing2D.LinearGradientBrush]::new(
        $rect,
        [System.Drawing.Color]::FromArgb(255, 9, 72, 116),
        [System.Drawing.Color]::FromArgb(255, 24, 190, 174),
        45
    )
    $graphics.FillEllipse($background, 10, 10, 236, 236)

    $shield = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $points = [System.Drawing.PointF[]]@(
        [System.Drawing.PointF]::new(128, 34),
        [System.Drawing.PointF]::new(198, 62),
        [System.Drawing.PointF]::new(184, 154),
        [System.Drawing.PointF]::new(128, 222),
        [System.Drawing.PointF]::new(72, 154),
        [System.Drawing.PointF]::new(58, 62)
    )
    $shield.AddPolygon($points)
    $shieldBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(245, 255, 255, 255))
    $graphics.FillPath($shieldBrush, $shield)

    $inner = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $inner.AddLine(128, 58, 176, 78)
    $inner.AddLine(166, 146, 128, 194)
    $inner.AddLine(90, 146, 80, 78)
    $inner.CloseFigure()
    $innerBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 12, 114, 157))
    $graphics.FillPath($innerBrush, $inner)

    $font = [System.Drawing.Font]::new("Segoe UI", 42, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $textBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)
    $format = [System.Drawing.StringFormat]::new()
    $format.Alignment = [System.Drawing.StringAlignment]::Center
    $format.LineAlignment = [System.Drawing.StringAlignment]::Center
    $graphics.DrawString("SOC", $font, $textBrush, [System.Drawing.RectangleF]::new(63, 92, 130, 58), $format)

    $pngStream = [System.IO.MemoryStream]::new()
    $bitmap.Save($pngStream, [System.Drawing.Imaging.ImageFormat]::Png)
    $pngBytes = $pngStream.ToArray()

    $file = [System.IO.File]::Create($Path)
    $writer = [System.IO.BinaryWriter]::new($file)
    try {
        $writer.Write([UInt16]0)
        $writer.Write([UInt16]1)
        $writer.Write([UInt16]1)
        $writer.Write([Byte]0)
        $writer.Write([Byte]0)
        $writer.Write([Byte]0)
        $writer.Write([Byte]0)
        $writer.Write([UInt16]1)
        $writer.Write([UInt16]32)
        $writer.Write([UInt32]$pngBytes.Length)
        $writer.Write([UInt32]22)
        $writer.Write($pngBytes)
    }
    finally {
        $writer.Dispose()
        $file.Dispose()
        $pngStream.Dispose()
        $graphics.Dispose()
        $bitmap.Dispose()
        $background.Dispose()
        $shieldBrush.Dispose()
        $innerBrush.Dispose()
        $textBrush.Dispose()
        $font.Dispose()
        $format.Dispose()
        $shield.Dispose()
        $inner.Dispose()
    }
}

Set-Location $ProjectRoot
$maven = Resolve-Maven
$jpackage = Resolve-JPackage

Write-Host "Using Maven: $maven"
Write-Host "Using jpackage: $jpackage"

& $maven clean package
if ($LASTEXITCODE -ne 0) { throw "Maven build failed." }

$jarPath = Join-Path $ProjectRoot "target\$JarName"
if (-not (Test-Path -LiteralPath $jarPath)) { throw "Built jar was not found: $jarPath" }

New-AppIcon -Path $IconPath
New-Item -ItemType Directory -Force -Path $DistRoot | Out-Null
New-Item -ItemType Directory -Force -Path $PackageInputDir | Out-Null
Get-ChildItem -LiteralPath $PackageInputDir -Force | Remove-Item -Recurse -Force
Copy-Item -LiteralPath $jarPath -Destination (Join-Path $PackageInputDir $JarName) -Force

if ($Type -eq "app-image") {
    $appImagePath = Join-Path $DistRoot $AppName
    if (Test-Path -LiteralPath $appImagePath) {
        $targetRoot = (Resolve-Path (Join-Path $ProjectRoot "target")).Path
        $resolvedAppPath = (Resolve-Path $appImagePath).Path
        if (-not $resolvedAppPath.StartsWith($targetRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
            throw "Refusing to clean a path outside target: $resolvedAppPath"
        }
        Remove-Item -LiteralPath $appImagePath -Recurse -Force
    }
}

$args = @(
    "--type", $Type,
    "--name", $AppName,
    "--app-version", "1.0.0",
    "--vendor", "UA Education",
    "--input", $PackageInputDir,
    "--main-jar", $JarName,
    "--dest", $DistRoot,
    "--icon", $IconPath,
    "--java-options", "-Dfile.encoding=UTF-8",
    "--java-options", "-Dspring.main.banner-mode=off"
)

& $jpackage @args
if ($LASTEXITCODE -ne 0) { throw "jpackage failed." }

if ($Type -eq "app-image") {
    $exePath = Join-Path $DistRoot "$AppName\$AppName.exe"
} else {
    $exePath = (Get-ChildItem -LiteralPath $DistRoot -Filter "*.exe" | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName
}

Write-Host "Created: $exePath"
Write-Host "Start it. The browser should open automatically at http://localhost:8080"
