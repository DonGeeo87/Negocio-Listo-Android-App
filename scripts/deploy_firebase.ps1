$ErrorActionPreference = "Stop"
Write-Host "Desplegando Reglas de Firestore y Hosting" -ForegroundColor Cyan

if (-not (Get-Command firebase -ErrorAction SilentlyContinue)) {
  Write-Host "Firebase CLI no encontrado. Instalar con: npm i -g firebase-tools" -ForegroundColor Red
  exit 1
}

Write-Host "Reglas de Firestore..." -ForegroundColor Yellow
firebase deploy --only firestore:rules

Write-Host "Hosting (public/) ..." -ForegroundColor Yellow
firebase deploy --only hosting

Write-Host "Despliegue completado" -ForegroundColor Green

