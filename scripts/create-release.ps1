# Script para crear release en GitHub
# Uso: .\scripts\create-release.ps1

param(
    [string]$Version = "1.0.2",
    [string]$APKPath = "app\build\outputs\apk\release\app-release.apk",
    [string]$ReleaseNotes = "RELEASE_NOTES_v1.0.2.md"
)

Write-Host "🚀 Preparando release v$Version..." -ForegroundColor Cyan

# Verificar que existe la APK
if (-not (Test-Path $APKPath)) {
    Write-Host "❌ Error: No se encontró la APK en $APKPath" -ForegroundColor Red
    Write-Host "   Ejecuta primero: .\gradlew assembleRelease" -ForegroundColor Yellow
    exit 1
}

# Verificar que existen las notas de release
if (-not (Test-Path $ReleaseNotes)) {
    Write-Host "❌ Error: No se encontraron las notas de release en $ReleaseNotes" -ForegroundColor Red
    exit 1
}

# Obtener información de la APK
$apk = Get-Item $APKPath
$apkSize = [math]::Round($apk.Length / 1MB, 2)
Write-Host "✅ APK encontrada: $($apk.Name) ($apkSize MB)" -ForegroundColor Green

# Leer notas de release
$notes = Get-Content $ReleaseNotes -Raw
Write-Host "✅ Notas de release cargadas" -ForegroundColor Green

Write-Host "`n📋 Instrucciones para crear el release en GitHub:" -ForegroundColor Yellow
Write-Host "`n1. Ve a: https://github.com/DonGeeo87/NegocioListoApp/releases/new" -ForegroundColor White
Write-Host "2. Tag: v$Version" -ForegroundColor White
Write-Host "3. Título: Release v$Version - Portal del Cliente como Core Feature" -ForegroundColor White
Write-Host "4. Descripción: Copia el contenido de $ReleaseNotes" -ForegroundColor White
Write-Host "5. Sube el archivo: $APKPath" -ForegroundColor White
Write-Host "6. Marca como 'Latest release' si es la versión más reciente" -ForegroundColor White
Write-Host "7. Publica el release" -ForegroundColor White

Write-Host "`n💡 Alternativa con GitHub CLI (gh):" -ForegroundColor Cyan
Write-Host "   gh release create v$Version `"$APKPath`" --title `"Release v$Version - Portal del Cliente como Core Feature`" --notes-file `"$ReleaseNotes`"" -ForegroundColor Gray

Write-Host "`n✅ Preparación completada!" -ForegroundColor Green

