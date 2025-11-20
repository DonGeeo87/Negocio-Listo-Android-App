# Script de despliegue para el Landing de NegocioListo
# Desarrollador: Giorgio Interdonato Palacios
# GitHub: @DonGeeo87

Write-Host "🚀 Desplegando Landing de NegocioListo..." -ForegroundColor Cyan

# Ruta del proyecto
$projectRoot = Split-Path -Parent $PSScriptRoot
$landingPath = Join-Path $projectRoot "landing"
$publicLandingPath = Join-Path $projectRoot "public\landing"

# Verificar que existe la carpeta landing
if (-not (Test-Path $landingPath)) {
    Write-Host "❌ Error: No se encontró la carpeta landing" -ForegroundColor Red
    exit 1
}

# Crear carpeta public/landing si no existe
if (-not (Test-Path $publicLandingPath)) {
    Write-Host "📁 Creando carpeta public/landing..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $publicLandingPath -Force | Out-Null
}

# Copiar archivos
Write-Host "📋 Copiando archivos a public/landing..." -ForegroundColor Yellow
Copy-Item "$landingPath\*" -Destination $publicLandingPath -Recurse -Force

Write-Host "✅ Archivos copiados exitosamente" -ForegroundColor Green

# Verificar Firebase CLI
$firebaseInstalled = Get-Command firebase -ErrorAction SilentlyContinue
if (-not $firebaseInstalled) {
    Write-Host "⚠️  Firebase CLI no está instalado" -ForegroundColor Yellow
    Write-Host "   Para instalar: npm install -g firebase-tools" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "✅ Archivos listos para desplegar manualmente" -ForegroundColor Green
    exit 0
}

# Preguntar si desea desplegar
$deploy = Read-Host "¿Deseas desplegar a Firebase Hosting ahora? (s/n)"
if ($deploy -eq "s" -or $deploy -eq "S") {
    Write-Host "🚀 Desplegando a Firebase Hosting..." -ForegroundColor Cyan
    Set-Location $projectRoot
    firebase deploy --only hosting
} else {
    Write-Host "✅ Archivos listos para desplegar" -ForegroundColor Green
    Write-Host "   Ejecuta: firebase deploy --only hosting" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "📱 Landing disponible en: https://TU_PROYECTO.web.app/landing" -ForegroundColor Cyan

