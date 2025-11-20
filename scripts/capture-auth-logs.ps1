# Script para capturar logs de autenticación
Write-Host "📱 Capturando logs de autenticación..." -ForegroundColor Cyan
Write-Host "Intenta hacer login o crear cuenta en la app ahora..." -ForegroundColor Yellow
Write-Host "Presiona Ctrl+C para detener la captura" -ForegroundColor Yellow
Write-Host ""

adb logcat -c
adb logcat | Select-String -Pattern "negociolisto|FirebaseAuth|AuthViewModel|ERROR|Exception|FirebaseException|W/System.err" -Context 2,2

