# Script para iniciar scrcpy y ver la pantalla del dispositivo Android
# Desarrollador: Giorgio Interdonato Palacios - GitHub @DonGeeo87

Write-Host "Iniciando scrcpy..." -ForegroundColor Cyan

# Ruta a scrcpy
$scrcpyPath = Join-Path $env:USERPROFILE "scrcpy\scrcpy-win64-v2.4\scrcpy.exe"
$adbPath = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"

# Verificar que ADB existe
if (-not (Test-Path $adbPath)) {
    Write-Host "[ERROR] ADB no encontrado en: $adbPath" -ForegroundColor Red
    Write-Host "Por favor, instala Android SDK Platform Tools" -ForegroundColor Yellow
    exit 1
}

# Verificar que scrcpy existe
if (-not (Test-Path $scrcpyPath)) {
    Write-Host "[ERROR] scrcpy no encontrado en: $scrcpyPath" -ForegroundColor Red
    Write-Host "Por favor, descarga scrcpy desde: https://github.com/Genymobile/scrcpy/releases" -ForegroundColor Yellow
    exit 1
}

# Verificar dispositivos conectados
Write-Host ""
Write-Host "Verificando dispositivos conectados..." -ForegroundColor Cyan

try {
    $devicesOutput = & $adbPath devices 2>&1
    Write-Host $devicesOutput
    
    # Convertir a string para verificar
    $devicesString = $devicesOutput -join "`n"
    
    # Verificar si hay dispositivos conectados (buscar línea que termine con "device")
    $hasDevice = $devicesString -match "\s+device\s*$"
    
    if (-not $hasDevice) {
        Write-Host ""
        Write-Host "No se encontraron dispositivos conectados" -ForegroundColor Yellow
        Write-Host "Por favor:" -ForegroundColor Yellow
        Write-Host "  1. Conecta tu dispositivo Android por USB" -ForegroundColor Yellow
        Write-Host "  2. Habilita 'Depuracion USB' en Opciones de desarrollador" -ForegroundColor Yellow
        Write-Host "  3. Acepta la autorizacion en tu dispositivo" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Luego ejecuta este script nuevamente." -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "[ERROR] No se pudo ejecutar ADB: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Dispositivo detectado. Abriendo scrcpy..." -ForegroundColor Green
Write-Host "Presiona Ctrl+C en esta ventana para cerrar scrcpy" -ForegroundColor Cyan
Write-Host ""

# Ejecutar scrcpy
try {
    & $scrcpyPath
} catch {
    Write-Host "[ERROR] No se pudo ejecutar scrcpy: $_" -ForegroundColor Red
    exit 1
}

