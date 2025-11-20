# Script para actualizar logos internos de la app
Write-Host "Actualizando logos internos de la app..." -ForegroundColor Green

$sourceIcon = "app\src\main\res\drawable\icon_negociolisto.png"
$basePath = "app\src\main\res\drawable"

# Verificar que el archivo fuente existe
if (-not (Test-Path $sourceIcon)) {
    Write-Host "Error: No se encontro el archivo fuente: $sourceIcon" -ForegroundColor Red
    exit 1
}

Write-Host "Archivo fuente encontrado: $sourceIcon" -ForegroundColor Yellow

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

# Actualizar logo_negociolisto.png (para pantallas internas)
$destFile = "$basePath\logo_negociolisto.png"
Write-Host "Actualizando logo_negociolisto.png (256x256px)..." -ForegroundColor Yellow

if (Resize-Image -SourcePath $sourceIcon -DestinationPath $destFile -Width 256 -Height 256) {
    Write-Host "Actualizado: $destFile" -ForegroundColor Green
} else {
    Write-Host "Error actualizando: $destFile" -ForegroundColor Red
}

# Actualizar logo_negociolistoapp.png (para splash screen)
$destFile = "$basePath\logo_negociolistoapp.png"
Write-Host "Actualizando logo_negociolistoapp.png (512x512px)..." -ForegroundColor Yellow

if (Resize-Image -SourcePath $sourceIcon -DestinationPath $destFile -Width 512 -Height 512) {
    Write-Host "Actualizado: $destFile" -ForegroundColor Green
} else {
    Write-Host "Error actualizando: $destFile" -ForegroundColor Red
}

Write-Host "Actualizacion de logos internos completada!" -ForegroundColor Green
Write-Host "Los logos internos estan actualizados con el nuevo diseno" -ForegroundColor Cyan


