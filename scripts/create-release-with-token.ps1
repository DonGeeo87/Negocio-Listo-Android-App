# Script para crear release con token proporcionado
param(
    [Parameter(Mandatory=$true)]
    [string]$Token,
    
    [string]$Version = "1.0.2"
)

$ErrorActionPreference = "Stop"

Write-Host "🚀 Creando Release v$Version..." -ForegroundColor Cyan

$apkPath = "app-release-v1.0.2.apk"
$notesPath = "RELEASE_NOTES_v1.0.2.md"

# Verificar archivos
if (-not (Test-Path $apkPath)) {
    Write-Host "❌ Error: APK no encontrada" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $notesPath)) {
    Write-Host "❌ Error: Notas no encontradas" -ForegroundColor Red
    exit 1
}

# Leer notas
$notes = [System.IO.File]::ReadAllText($notesPath, [System.Text.Encoding]::UTF8)

# Preparar datos
$tagName = "v$Version"
$releaseName = "Release v$Version - Portal del Cliente como Core Feature"
$repo = "DonGeeo87/NegocioListoApp"

# Crear JSON manualmente para evitar problemas de encoding
$jsonBody = @"
{
  "tag_name": "$tagName",
  "name": "$($releaseName -replace '"', '\"')",
  "body": $(($notes -replace '\\', '\\' -replace '"', '\"' -replace "`r`n", "`n" -replace "`n", "\n") | ConvertTo-Json),
  "draft": false,
  "prerelease": false
}
"@

$headers = @{
    "Authorization" = "Bearer $Token"
    "Accept" = "application/vnd.github.v3+json"
}

Write-Host "📝 Creando release..." -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri "https://api.github.com/repos/$repo/releases" -Method Post -Headers $headers -Body $jsonBody -ContentType "application/json; charset=utf-8"
    
    Write-Host "✅ Release creado! ID: $($response.id)" -ForegroundColor Green
    Write-Host "🔗 URL: $($response.html_url)" -ForegroundColor Cyan
    
    # Subir APK
    Write-Host "📤 Subiendo APK..." -ForegroundColor Yellow
    $apk = Get-Item $apkPath
    $uploadUrl = $response.upload_url -replace "\{.*\}", "?name=$($apk.Name)"
    
    $uploadHeaders = @{
        "Authorization" = "Bearer $Token"
        "Accept" = "application/vnd.github.v3+json"
    }
    
    $uploadResponse = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders -InFile $apk.FullName -ContentType "application/vnd.android.package-archive"
    
    Write-Host "✅ APK subida exitosamente!" -ForegroundColor Green
    Write-Host "`n🎉 Release publicado:" -ForegroundColor Green
    Write-Host "   $($response.html_url)" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Error:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host $_.ErrorDetails.Message -ForegroundColor Red
    }
    Write-Host "`n💡 Si el error es 404, verifica que:" -ForegroundColor Yellow
    Write-Host "   1. El token tenga permisos 'repo'" -ForegroundColor White
    Write-Host "   2. El repositorio sea accesible" -ForegroundColor White
    Write-Host "   3. El nombre del repositorio sea correcto" -ForegroundColor White
    exit 1
}

