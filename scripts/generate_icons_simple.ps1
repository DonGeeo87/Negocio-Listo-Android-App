# Script simple para generar iconos de launcher
Write-Host "Generando iconos de launcher para NegocioListo..." -ForegroundColor Green

# Definir las densidades y sus tamaños
$densities = @{
    "mipmap-mdpi" = 48
    "mipmap-hdpi" = 72
    "mipmap-xhdpi" = 96
    "mipmap-xxhdpi" = 144
    "mipmap-xxxhdpi" = 192
}

$sourceIcon = "app\src\main\res\drawable\icon_negociolisto.png"
$basePath = "app\src\main\res"

# Verificar que el archivo fuente existe
if (-not (Test-Path $sourceIcon)) {
    Write-Host "Error: No se encontro el archivo fuente: $sourceIcon" -ForegroundColor Red
    exit 1
}

Write-Host "Archivo fuente encontrado: $sourceIcon" -ForegroundColor Yellow

# Crear directorios si no existen
foreach ($density in $densities.Keys) {
    $dir = "$basePath\$density"
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        Write-Host "Creado directorio: $dir" -ForegroundColor Cyan
    }
}

# Funcion para redimensionar imagen usando .NET
function Resize-Image {
    param(
        [string]$SourcePath,
        [string]$DestinationPath,
        [int]$Width,
        [int]$Height
    )
    
    try {
        # Cargar la imagen
        Add-Type -AssemblyName System.Drawing
        $sourceImage = [System.Drawing.Image]::FromFile((Resolve-Path $SourcePath))
        
        # Crear nueva imagen con el tamano deseado
        $newImage = New-Object System.Drawing.Bitmap($Width, $Height)
        $graphics = [System.Drawing.Graphics]::FromImage($newImage)
        
        # Configurar alta calidad
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        
        # Dibujar la imagen redimensionada
        $graphics.DrawImage($sourceImage, 0, 0, $Width, $Height)
        
        # Guardar como PNG
        $newImage.Save($DestinationPath, [System.Drawing.Imaging.ImageFormat]::Png)
        
        # Liberar recursos
        $graphics.Dispose()
        $newImage.Dispose()
        $sourceImage.Dispose()
        
        return $true
    }
    catch {
        Write-Host "Error redimensionando imagen: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Generar iconos para cada densidad
foreach ($density in $densities.GetEnumerator()) {
    $size = $density.Value
    $destFile = "$basePath\$($density.Key)\ic_launcher.png"
    
    Write-Host "Generando $($density.Key) (${size}x${size}px)..." -ForegroundColor Yellow
    
    if (Resize-Image -SourcePath $sourceIcon -DestinationPath $destFile -Width $size -Height $size) {
        Write-Host "Generado: $destFile" -ForegroundColor Green
    } else {
        Write-Host "Error generando: $destFile" -ForegroundColor Red
    }
}

# Generar icono redondo (mismo tamano que ic_launcher)
foreach ($density in $densities.GetEnumerator()) {
    $size = $density.Value
    $destFile = "$basePath\$($density.Key)\ic_launcher_round.png"
    
    Write-Host "Generando $($density.Key) redondo (${size}x${size}px)..." -ForegroundColor Yellow
    
    if (Resize-Image -SourcePath $sourceIcon -DestinationPath $destFile -Width $size -Height $size) {
        Write-Host "Generado: $destFile" -ForegroundColor Green
    } else {
        Write-Host "Error generando: $destFile" -ForegroundColor Red
    }
}

# Generar icono de foreground para adaptive icons (108dp = 108px para xxxhdpi)
$foregroundSize = 108
$destFile = "$basePath\mipmap-xxxhdpi\ic_launcher_foreground.png"

Write-Host "Generando foreground (${foregroundSize}x${foregroundSize}px)..." -ForegroundColor Yellow

if (Resize-Image -SourcePath $sourceIcon -DestinationPath $destFile -Width $foregroundSize -Height $foregroundSize) {
    Write-Host "Generado: $destFile" -ForegroundColor Green
} else {
    Write-Host "Error generando: $destFile" -ForegroundColor Red
}

Write-Host "Generacion de iconos completada!" -ForegroundColor Green
Write-Host "Los iconos estan listos para usar en la app" -ForegroundColor Cyan
