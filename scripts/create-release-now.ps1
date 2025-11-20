# Script para crear release en GitHub automáticamente
# Requiere token de GitHub

param(
    [string]$Version = "1.0.2"
)

$ErrorActionPreference = "Stop"

Write-Host "🚀 Creando Release v$Version en GitHub..." -ForegroundColor Cyan

# Verificar archivos
$apkPath = "app-release-v1.0.2.apk"
$notesPath = "RELEASE_NOTES_v1.0.2.md"

if (-not (Test-Path $apkPath)) {
    Write-Host "❌ Error: No se encontró la APK en $apkPath" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $notesPath)) {
    Write-Host "❌ Error: No se encontraron las notas de release" -ForegroundColor Red
    exit 1
}

# Solicitar token de GitHub
$token = Read-Host "🔑 Ingresa tu GitHub Personal Access Token (con permisos 'repo')"
if ([string]::IsNullOrWhiteSpace($token)) {
    Write-Host "❌ Token requerido. Puedes crearlo en: https://github.com/settings/tokens" -ForegroundColor Red
    exit 1
}

# Leer notas
$notes = Get-Content $notesPath -Raw

# Preparar datos
$tagName = "v$Version"
$releaseName = "Release v$Version - Portal del Cliente como Core Feature"
$repo = "DonGeeo87/NegocioListoApp"

$releaseData = @{
    tag_name = $tagName
    name = $releaseName
    body = $notes
    draft = $false
    prerelease = $false
} | ConvertTo-Json

# Headers
$headers = @{
    "Authorization" = "token $token"
    "Accept" = "application/vnd.github.v3+json"
}

try {
    Write-Host "📝 Creando release..." -ForegroundColor Yellow
    $createUrl = "https://api.github.com/repos/$repo/releases"
    $response = Invoke-RestMethod -Uri $createUrl -Method Post -Headers $headers -Body $releaseData -ContentType "application/json"
    
    $releaseId = $response.id
    Write-Host "✅ Release creado! ID: $releaseId" -ForegroundColor Green
    
    # Subir APK
    Write-Host "📤 Subiendo APK..." -ForegroundColor Yellow
    $apk = Get-Item $apkPath
    $apkName = $apk.Name
    
    # Construir URL de upload
    $uploadUrl = $response.upload_url -replace "\{.*\}", "?name=$apkName"
    
    # Headers para upload
    $uploadHeaders = @{
        "Authorization" = "token $token"
        "Accept" = "application/vnd.github.v3+json"
    }
    
    # Leer archivo como bytes
    $apkBytes = [System.IO.File]::ReadAllBytes($apk.FullName)
    
    # Crear boundary para multipart/form-data
    $boundary = [System.Guid]::NewGuid().ToString()
    $LF = "`r`n"
    
    # Construir body multipart
    $bodyParts = @()
    $bodyParts += "--$boundary"
    $bodyParts += "Content-Disposition: form-data; name=`"file`"; filename=`"$apkName`""
    $bodyParts += "Content-Type: application/vnd.android.package-archive"
    $bodyParts += ""
    
    $bodyString = $bodyParts -join $LF
    $bodyBytes = [System.Text.Encoding]::ASCII.GetBytes($bodyString + $LF)
    $endBytes = [System.Text.Encoding]::ASCII.GetBytes($LF + "--$boundary--" + $LF)
    
    # Combinar todo
    $finalBody = $bodyBytes + $apkBytes + $endBytes
    
    # Headers finales
    $finalHeaders = @{
        "Authorization" = "token $token"
        "Accept" = "application/vnd.github.v3+json"
        "Content-Type" = "multipart/form-data; boundary=$boundary"
    }
    
    try {
        $uploadResponse = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $finalHeaders -Body $finalBody
    } catch {
        # Si falla, intentar método alternativo más simple
        Write-Host "⚠️ Intentando método alternativo..." -ForegroundColor Yellow
        $uploadHeaders2 = @{
            "Authorization" = "token $token"
            "Accept" = "application/vnd.github.v3+json"
        }
        $uploadResponse = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders2 -InFile $apk.FullName -ContentType "application/vnd.android.package-archive"
    }
    
    Write-Host "✅ APK subida exitosamente!" -ForegroundColor Green
    Write-Host "`n🎉 Release publicado:" -ForegroundColor Green
    Write-Host "   $($response.html_url)" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Error:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        $errorJson = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "Detalles: $($errorJson.message)" -ForegroundColor Red
    }
    exit 1
}

