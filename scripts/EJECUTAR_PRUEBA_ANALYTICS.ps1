# 🧪 Script de Prueba: Eventos de Analytics
# Desarrollador: Giorgio Interdonato Palacios - GitHub @DonGeeo87

Write-Host "`n=== PRUEBA DE EVENTOS DE ANALYTICS ===" -ForegroundColor Cyan
Write-Host ""

# Verificar que hay un dispositivo conectado
Write-Host "1. Verificando dispositivos conectados..." -ForegroundColor Yellow
$devices = adb devices
if ($devices -match "device$") {
    Write-Host "   ✅ Dispositivo conectado" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  No hay dispositivos conectados" -ForegroundColor Red
    Write-Host "   Conecta un dispositivo y ejecuta: adb devices" -ForegroundColor Yellow
    exit 1
}

# Instalar APK si existe
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    Write-Host "`n2. Instalando APK..." -ForegroundColor Yellow
    adb install -r $apkPath
    Write-Host "   ✅ APK instalado" -ForegroundColor Green
} else {
    Write-Host "`n2. ⚠️  APK no encontrado. Compilando..." -ForegroundColor Yellow
    .\gradlew assembleDebug -x test
    if (Test-Path $apkPath) {
        adb install -r $apkPath
        Write-Host "   ✅ APK compilado e instalado" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Error al compilar APK" -ForegroundColor Red
        exit 1
    }
}

# Limpiar logs anteriores
Write-Host "`n3. Limpiando logs anteriores..." -ForegroundColor Yellow
adb logcat -c
Write-Host "   ✅ Logs limpiados" -ForegroundColor Green

# Iniciar monitoreo de logs en segundo plano
Write-Host "`n4. Iniciando monitoreo de logs de Analytics..." -ForegroundColor Yellow
Write-Host "   Los logs se mostrarán a continuación..." -ForegroundColor Cyan
Write-Host ""

# Instrucciones para el usuario
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Magenta
Write-Host "📱 INSTRUCCIONES PARA LA PRUEBA:" -ForegroundColor Magenta
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Magenta
Write-Host ""
Write-Host "1. Abre Firebase Console en tu navegador:" -ForegroundColor White
Write-Host "   https://console.firebase.google.com/project/app-negocio-listo/analytics/events" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. Click en 'View real-time events' o 'Ver eventos en tiempo real'" -ForegroundColor White
Write-Host ""
Write-Host "3. En la app del dispositivo, realiza las siguientes acciones:" -ForegroundColor White
Write-Host "   • Crear una venta → Debe aparecer 'sale_created'" -ForegroundColor Yellow
Write-Host "   • Agregar un producto → Debe aparecer 'product_added'" -ForegroundColor Yellow
Write-Host "   • Agregar un cliente → Debe aparecer 'customer_added'" -ForegroundColor Yellow
Write-Host "   • Navegar entre pantallas → Debe aparecer 'screen_view'" -ForegroundColor Yellow
Write-Host ""
Write-Host "4. Espera 1-2 minutos después de cada acción" -ForegroundColor White
Write-Host ""
Write-Host "5. Presiona Ctrl+C para detener el monitoreo de logs" -ForegroundColor White
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Magenta
Write-Host ""

# Monitorear logs de Analytics
Write-Host "📊 Monitoreando logs de Analytics (presiona Ctrl+C para detener)..." -ForegroundColor Green
Write-Host ""

adb logcat | Select-String -Pattern "Analytics"

