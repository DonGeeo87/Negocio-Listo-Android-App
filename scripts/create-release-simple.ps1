# Script simplificado para crear release - Abre la página de GitHub
# Alternativa si no tienes token de GitHub

param(
    [string]$Version = "1.0.2",
    [string]$APKPath = "app-release-v1.0.2.apk",
    [string]$ReleaseNotes = "RELEASE_NOTES_v1.0.2.md"
)

Write-Host "🚀 Preparando release v$Version..." -ForegroundColor Cyan

# Verificar archivos
if (-not (Test-Path $APKPath)) {
    Write-Host "❌ Error: No se encontró la APK en $APKPath" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $ReleaseNotes)) {
    Write-Host "❌ Error: No se encontraron las notas de release" -ForegroundColor Red
    exit 1
}

$apk = Get-Item $APKPath
$apkSize = [math]::Round($apk.Length / 1MB, 2)

Write-Host "✅ APK: $($apk.Name) ($apkSize MB)" -ForegroundColor Green
Write-Host "✅ Notas: $ReleaseNotes" -ForegroundColor Green

# Leer notas
$notes = Get-Content $ReleaseNotes -Raw

# Crear URL pre-rellenada (GitHub no soporta esto directamente, pero podemos preparar todo)
$releaseUrl = "https://github.com/DonGeeo87/NegocioListoApp/releases/new"

Write-Host "`n📋 INSTRUCCIONES:" -ForegroundColor Yellow
Write-Host "`n1. Abre esta URL en tu navegador:" -ForegroundColor White
Write-Host "   $releaseUrl" -ForegroundColor Cyan

Write-Host "`n2. Completa el formulario:" -ForegroundColor White
Write-Host "   - Tag: v$Version" -ForegroundColor Gray
Write-Host "   - Título: Release v$Version - Portal del Cliente como Core Feature" -ForegroundColor Gray
Write-Host "   - Descripción: (copia desde $ReleaseNotes)" -ForegroundColor Gray
Write-Host "   - Archivo: $APKPath" -ForegroundColor Gray
Write-Host "   - Marca como 'Latest release'" -ForegroundColor Gray

Write-Host "`n3. Publica el release" -ForegroundColor White

# Intentar abrir el navegador
Write-Host "`n🌐 Abriendo GitHub en el navegador..." -ForegroundColor Yellow
Start-Process $releaseUrl

# Mostrar las notas en la consola para copiar fácilmente
Write-Host "`n📝 NOTAS DE RELEASE (copia esto):" -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────" -ForegroundColor Gray
Write-Host $notes -ForegroundColor White
Write-Host "─────────────────────────────────────────" -ForegroundColor Gray

