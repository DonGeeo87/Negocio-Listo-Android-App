# Script para capturar logs del backup en tiempo real
# Uso: .\capture-backup-logs.ps1

$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$logDir = Join-Path $PSScriptRoot "logs"
if (-not (Test-Path $logDir)) {
    New-Item -Path $logDir -ItemType Directory | Out-Null
}
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = Join-Path $logDir "backup-$timestamp.log"

Write-Host "Verificando dispositivo..." -ForegroundColor Cyan
& $adb devices

Write-Host "`nLimpiando logs anteriores..." -ForegroundColor Yellow
& $adb logcat -c

Write-Host "`nCapturando logs del backup..." -ForegroundColor Green
Write-Host "   Filtros: FirebaseBackup, BackupService, BackupRestore, ERROR, Exception" -ForegroundColor Gray
Write-Host "   Los logs filtrados se guardarán en: $logFile" -ForegroundColor Gray
Write-Host "   Presiona Ctrl+C para detener`n" -ForegroundColor Yellow

# Capturar logs con filtros específicos
& $adb logcat -v time FirebaseBackup:D BackupService:D BackupRestoreViewModel:D AndroidRuntime:E *:E |
    Tee-Object -FilePath $logFile |
    Select-String -Pattern "FirebaseBackup|BackupService|BackupRestore|ERROR|Exception|FATAL|backup" -Context 1,1

