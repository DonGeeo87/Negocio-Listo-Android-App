# Script para monitorear logcat de la app NegocioListo en tiempo real
# Uso: .\monitor-logcat.ps1 [filtros opcionales]
# Ejemplos:
#   .\monitor-logcat.ps1                    # Todos los logs de la app
#   .\monitor-logcat.ps1 -Filter "ERROR"    # Solo errores
#   .\monitor-logcat.ps1 -Filter "Firebase" # Solo logs de Firebase

param(
    [string]$Filter = "",
    [switch]$SaveToFile = $false,
    [switch]$ClearLogs = $true
)

$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$logDir = Join-Path $PSScriptRoot "logs"
if (-not (Test-Path $logDir)) {
    New-Item -Path $logDir -ItemType Directory | Out-Null
}

# Verificar que ADB existe
if (-not (Test-Path $adb)) {
    Write-Host "[ERROR] ADB no encontrado en: $adb" -ForegroundColor Red
    Write-Host "   Asegurate de tener Android SDK instalado" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n[INFO] Verificando dispositivos conectados..." -ForegroundColor Cyan
$devices = & $adb devices
Write-Host $devices

if ($devices -match "device$") {
    Write-Host "[OK] Dispositivo conectado`n" -ForegroundColor Green
} else {
    Write-Host "[WARN] No se detecto ningun dispositivo conectado" -ForegroundColor Yellow
    Write-Host "   Conecta un dispositivo o inicia un emulador y vuelve a intentar`n" -ForegroundColor Gray
    exit 1
}

# Limpiar logs anteriores si se solicita
if ($ClearLogs) {
    Write-Host "[INFO] Limpiando logs anteriores..." -ForegroundColor Yellow
    & $adb logcat -c
    Start-Sleep -Milliseconds 500
}

# Configurar archivo de log si se solicita
$logFile = $null
if ($SaveToFile) {
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $logFile = Join-Path $logDir "logcat-$timestamp.log"
    Write-Host "[INFO] Los logs se guardaran en: $logFile" -ForegroundColor Cyan
}

# Configurar filtros de logcat
# Tag principal de la app
$appTag = "NegocioListo"
$packageName = "com.negociolisto.app"

# Filtros por defecto para la app
$logcatFilters = @(
    "$appTag:D",           # Debug de la app
    "AndroidRuntime:E",     # Errores de runtime
    "*:E",                  # Todos los errores
    "*:F"                   # Todos los fatales
)

# Agregar filtros específicos si se proporcionan
if ($Filter -ne "") {
    Write-Host "[INFO] Aplicando filtro: $Filter" -ForegroundColor Cyan
    $logcatFilters += "$Filter:D"
}

# Construir argumentos del comando logcat
$logcatArgs = @("logcat", "-v", "time") + $logcatFilters

Write-Host "`n[INFO] Iniciando monitoreo de logcat..." -ForegroundColor Green
Write-Host "   App: $packageName" -ForegroundColor Gray
Write-Host "   Tag principal: $appTag" -ForegroundColor Gray
if ($Filter -ne "") {
    Write-Host "   Filtro adicional: $Filter" -ForegroundColor Gray
}
Write-Host "   Presiona Ctrl+C para detener`n" -ForegroundColor Yellow

# Función para colorear la salida según el nivel de log
function Format-LogLine {
    param([string]$line)
    
    if ($line -match " E | ERROR|FATAL") {
        Write-Host $line -ForegroundColor Red
    } elseif ($line -match " W | WARN") {
        Write-Host $line -ForegroundColor Yellow
    } elseif ($line -match " I | INFO") {
        Write-Host $line -ForegroundColor Cyan
    } elseif ($line -match " D | DEBUG") {
        Write-Host $line -ForegroundColor Gray
    } else {
        Write-Host $line
    }
}

try {
    if ($SaveToFile -and $logFile) {
        # Guardar en archivo y mostrar en consola
        & $adb $logcatArgs | Tee-Object -FilePath $logFile | ForEach-Object {
            Format-LogLine $_
        }
    } else {
        # Solo mostrar en consola
        & $adb $logcatArgs | ForEach-Object {
            Format-LogLine $_
        }
    }
} catch {
    Write-Host "`n[ERROR] Error al ejecutar logcat: $_" -ForegroundColor Red
    exit 1
}

