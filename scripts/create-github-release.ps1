# Script para crear release en GitHub usando la API
# Requiere: Token de GitHub con permisos de repo

param(
    [Parameter(Mandatory=$true)]
    [string]$GitHubToken,
    
    [string]$Version = "1.0.2",
    [string]$APKPath = "app-release-v1.0.2.apk",
    [string]$ReleaseNotes = "RELEASE_NOTES_v1.0.2.md",
    [string]$Repo = "DonGeeo87/NegocioListoApp"
)

$ErrorActionPreference = "Stop"

Write-Host "🚀 Creando release v$Version en GitHub..." -ForegroundColor Cyan

# Verificar que existe la APK
if (-not (Test-Path $APKPath)) {
    Write-Host "❌ Error: No se encontró la APK en $APKPath" -ForegroundColor Red
    exit 1
}

# Verificar que existen las notas de release
if (-not (Test-Path $ReleaseNotes)) {
    Write-Host "❌ Error: No se encontraron las notas de release en $ReleaseNotes" -ForegroundColor Red
    exit 1
}

# Leer notas de release
$notes = Get-Content $ReleaseNotes -Raw
$notes = $notes -replace "`r`n", "`n"  # Normalizar line breaks

# Obtener información de la APK
$apk = Get-Item $APKPath
$apkSize = [math]::Round($apk.Length / 1MB, 2)
Write-Host "✅ APK encontrada: $($apk.Name) ($apkSize MB)" -ForegroundColor Green

# Preparar datos del release
$tagName = "v$Version"
$releaseName = "Release v$Version - Portal del Cliente como Core Feature"

$releaseData = @{
    tag_name = $tagName
    name = $releaseName
    body = $notes
    draft = $false
    prerelease = $false
} | ConvertTo-Json

Write-Host "📝 Creando release..." -ForegroundColor Yellow

# Crear el release
$headers = @{
    "Authorization" = "token $GitHubToken"
    "Accept" = "application/vnd.github.v3+json"
}

try {
    $createUrl = "https://api.github.com/repos/$Repo/releases"
    $response = Invoke-RestMethod -Uri $createUrl -Method Post -Headers $headers -Body $releaseData -ContentType "application/json"
    
    $releaseId = $response.id
    Write-Host "✅ Release creado con ID: $releaseId" -ForegroundColor Green
    Write-Host "🔗 URL: $($response.html_url)" -ForegroundColor Cyan
    
    # Subir la APK
    Write-Host "📤 Subiendo APK..." -ForegroundColor Yellow
    
    $uploadUrl = $response.upload_url -replace "\{.*\}", "?name=$($apk.Name)"
    
    $fileBytes = [System.IO.File]::ReadAllBytes($apk.FullName)
    $fileEnc = [System.Text.Encoding]::GetEncoding("ISO-8859-1").GetString($fileBytes)
    $boundary = [System.Guid]::NewGuid().ToString()
    
    $bodyLines = @(
        "--$boundary",
        "Content-Disposition: form-data; name=`"file`"; filename=`"$($apk.Name)`"",
        "Content-Type: application/vnd.android.package-archive",
        "",
        $fileEnc,
        "--$boundary--"
    )
    $body = $bodyLines -join "`r`n"
    
    $uploadHeaders = @{
        "Authorization" = "token $GitHubToken"
        "Accept" = "application/vnd.github.v3+json"
        "Content-Type" = "multipart/form-data; boundary=$boundary"
    }
    
    $uploadResponse = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders -Body $body
    
    Write-Host "✅ APK subida exitosamente!" -ForegroundColor Green
    Write-Host "`n🎉 Release publicado:" -ForegroundColor Green
    Write-Host "   $($response.html_url)" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Error al crear el release:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host $_.ErrorDetails.Message -ForegroundColor Red
    }
    exit 1
}

