# Script para desplegar Cloud Functions de Firebase
# Desarrollador: Giorgio Interdonato Palacios - GitHub @DonGeeo87

Write-Host "🔥 Desplegando Cloud Functions de Firebase..." -ForegroundColor Cyan

# Verificar que estamos en el directorio correcto
if (-not (Test-Path "functions")) {
    Write-Host "❌ Error: No se encuentra la carpeta 'functions'" -ForegroundColor Red
    Write-Host "   Asegúrate de ejecutar este script desde la raíz del proyecto" -ForegroundColor Yellow
    exit 1
}

# Verificar que Firebase CLI está instalado
try {
    $firebaseVersion = firebase --version 2>&1
    Write-Host "✅ Firebase CLI encontrado: $firebaseVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: Firebase CLI no está instalado" -ForegroundColor Red
    Write-Host "   Instala con: npm install -g firebase-tools" -ForegroundColor Yellow
    exit 1
}

# Verificar que estamos logueados en Firebase
Write-Host "`n🔐 Verificando autenticación..." -ForegroundColor Cyan
try {
    $currentUser = firebase login:list 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "⚠️  No estás logueado en Firebase" -ForegroundColor Yellow
        Write-Host "   Ejecutando: firebase login" -ForegroundColor Yellow
        firebase login
    } else {
        Write-Host "✅ Autenticado en Firebase" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  Error al verificar autenticación" -ForegroundColor Yellow
}

# Verificar configuración de correo
Write-Host "`n📧 Verificando configuración de correo..." -ForegroundColor Cyan
$emailConfig = firebase functions:config:get 2>&1
if ($LASTEXITCODE -eq 0 -and $emailConfig -match "gmail|sendgrid") {
    Write-Host "✅ Configuración de correo encontrada" -ForegroundColor Green
} else {
    Write-Host "⚠️  No hay configuración de correo" -ForegroundColor Yellow
    Write-Host "   Configura Gmail con:" -ForegroundColor Yellow
    Write-Host "   firebase functions:config:set gmail.email=`"tu-email@gmail.com`" gmail.password=`"tu-app-password`"" -ForegroundColor Gray
    Write-Host "`n   O SendGrid con:" -ForegroundColor Yellow
    Write-Host "   firebase functions:config:set sendgrid.api_key=`"SG.tu-api-key`"" -ForegroundColor Gray
    Write-Host "`n   ¿Deseas continuar de todas formas? (S/N)" -ForegroundColor Yellow
    $continue = Read-Host
    if ($continue -ne "S" -and $continue -ne "s") {
        exit 0
    }
}

# Instalar dependencias si es necesario
Write-Host "`n📦 Verificando dependencias..." -ForegroundColor Cyan
if (-not (Test-Path "functions/node_modules")) {
    Write-Host "   Instalando dependencias..." -ForegroundColor Yellow
    Set-Location functions
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Error al instalar dependencias" -ForegroundColor Red
        Set-Location ..
        exit 1
    }
    Set-Location ..
    Write-Host "✅ Dependencias instaladas" -ForegroundColor Green
} else {
    Write-Host "✅ Dependencias ya instaladas" -ForegroundColor Green
}

# Ejecutar linter
Write-Host "`n🔍 Ejecutando linter..." -ForegroundColor Cyan
Set-Location functions
npm run lint
if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️  Advertencias del linter encontradas" -ForegroundColor Yellow
    Write-Host "   ¿Deseas continuar con el despliegue? (S/N)" -ForegroundColor Yellow
    $continue = Read-Host
    if ($continue -ne "S" -and $continue -ne "s") {
        Set-Location ..
        exit 0
    }
}
Set-Location ..

# Desplegar funciones
Write-Host "`n🚀 Desplegando funciones a Firebase..." -ForegroundColor Cyan
firebase deploy --only functions

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ ¡Despliegue exitoso!" -ForegroundColor Green
    Write-Host "`n📊 Para ver los logs:" -ForegroundColor Cyan
    Write-Host "   firebase functions:log" -ForegroundColor Gray
    Write-Host "`n📝 Para ver funciones desplegadas:" -ForegroundColor Cyan
    Write-Host "   firebase functions:list" -ForegroundColor Gray
} else {
    Write-Host "`n❌ Error en el despliegue" -ForegroundColor Red
    Write-Host "   Revisa los mensajes de error arriba" -ForegroundColor Yellow
    exit 1
}


