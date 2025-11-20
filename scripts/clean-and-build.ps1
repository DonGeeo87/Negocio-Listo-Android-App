# Script para limpiar y reconstruir el proyecto Android
Write-Host "Limpiando proyecto Android..." -ForegroundColor Green

# Detener todos los procesos de Java/Gradle
Write-Host "Deteniendo procesos de Java..." -ForegroundColor Yellow
try { taskkill /f /im java.exe 2>$null } catch {}

# Limpiar directorios de build
Write-Host "Eliminando directorios de build..." -ForegroundColor Yellow
if (Test-Path "app\build") { Remove-Item -Recurse -Force "app\build" }
if (Test-Path "build") { Remove-Item -Recurse -Force "build" }
if (Test-Path ".gradle") { Remove-Item -Recurse -Force ".gradle" }

# Limpiar cache de Gradle global
Write-Host "Limpiando cache global de Gradle..." -ForegroundColor Yellow
$gradleHome = "$env:USERPROFILE\.gradle"
if (Test-Path "$gradleHome\caches") { Remove-Item -Recurse -Force "$gradleHome\caches" }
if (Test-Path "$gradleHome\daemon") { Remove-Item -Recurse -Force "$gradleHome\daemon" }

Write-Host "Limpieza completada." -ForegroundColor Green
Write-Host "Iniciando construccion del proyecto..." -ForegroundColor Green

# Crear plantilla de local.properties si no existe
if (!(Test-Path -Path "local.properties")) {
    Write-Host "local.properties no encontrado. Creando plantilla..." -ForegroundColor Yellow
    @"
## Configura la ruta del SDK de Android localmente
# sdk.dir=C:\\Android\\Sdk
"@ | Out-File -Encoding UTF8 local.properties
    Write-Host "Define sdk.dir en local.properties antes de compilar." -ForegroundColor Yellow
}

# Intentar construir el proyecto
try {
    if (Test-Path ".\gradlew.bat") {
        & ".\gradlew.bat" clean
        & ".\gradlew.bat" compileDebugKotlin -x test
        & ".\gradlew.bat" assembleDebug -x test
    } else {
        & ".\gradle-temp\gradle-8.4\bin\gradle.bat" clean build --no-daemon
    }
    Write-Host "Proyecto construido exitosamente." -ForegroundColor Green
    Write-Host "APK: app\\build\\outputs\\apk\\debug\\app-debug.apk" -ForegroundColor Green
} catch {
    Write-Host "Error durante la construccion: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Sugerencia: Android Studio -> Build > Clean Project" -ForegroundColor Yellow
}
