# Script para deploy en Firebase Hosting
Write-Host "🚀 Iniciando deploy en Firebase Hosting..." -ForegroundColor Cyan

# Paso 1: Build de Next.js
Write-Host "`n📦 Construyendo aplicación..." -ForegroundColor Yellow
npm run build

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error en el build. Revisa los errores arriba." -ForegroundColor Red
    exit 1
}

Write-Host "✅ Build completado!" -ForegroundColor Green

# Paso 2: Copiar archivos a public/presentacion-app (preservando archivos existentes)
Write-Host "`n📋 Copiando archivos a public/presentacion-app..." -ForegroundColor Yellow
$outDir = "out"
$publicDir = "..\public"
$presentacionDir = "$publicDir\presentacion-app"

# Crear directorio de presentación si no existe
if (-not (Test-Path $presentacionDir)) {
    New-Item -ItemType Directory -Path $presentacionDir -Force | Out-Null
    Write-Host "  - Creado directorio presentacion-app" -ForegroundColor Gray
}

# Preservar archivos importantes del portal del cliente (no se tocan)
$preservedFiles = @("collection.html", "customer-portal.html", "css", "js", "assets", "index.html")
Write-Host "  - Preservando archivos del portal del cliente" -ForegroundColor Gray

# Limpiar solo el directorio de presentación (no tocar otros archivos)
if (Test-Path $presentacionDir) {
    Write-Host "  - Limpiando directorio presentacion-app anterior..." -ForegroundColor Gray
    Remove-Item -Path "$presentacionDir\*" -Recurse -Force -ErrorAction SilentlyContinue
}

# Copiar todos los archivos de out a public/presentacion-app
Write-Host "  - Copiando archivos de la presentación..." -ForegroundColor Gray
Copy-Item -Path "$outDir\*" -Destination $presentacionDir -Recurse -Force

Write-Host "✅ Archivos copiados a public/presentacion-app!" -ForegroundColor Green

# Paso 3: Deploy a Firebase
Write-Host "`n🔥 Desplegando en Firebase Hosting..." -ForegroundColor Yellow
cd ..
firebase deploy --only hosting

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ ¡Deploy exitoso!" -ForegroundColor Green
    Write-Host "🌐 Tu presentación está disponible en Firebase Hosting" -ForegroundColor Cyan
    Write-Host "🔗 Revisa la URL en la consola de Firebase" -ForegroundColor Cyan
} else {
    Write-Host "`n❌ Error en el deploy. Revisa los errores arriba." -ForegroundColor Red
}
