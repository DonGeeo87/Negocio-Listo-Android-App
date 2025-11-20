# 🔥 Deploy en Firebase Hosting - Instrucciones

## ⚠️ IMPORTANTE: Antes de empezar

1. **Cierra el servidor de desarrollo** si está corriendo (`Ctrl+C` en la terminal donde corre `npm run dev`)
2. **Cierra Android Studio** si está abierto
3. **Cierra cualquier proceso de Node.js** que pueda estar usando archivos

## 🚀 Pasos para Deploy

### Opción 1: Usar el Script Automático (Recomendado)

1. Abre PowerShell en la carpeta `presentation`
2. Ejecuta:
```powershell
.\deploy-firebase.ps1
```

El script hará:
- ✅ Build de Next.js
- ✅ Copiar archivos a `public`
- ✅ Deploy a Firebase Hosting

### Opción 2: Manual

1. **Build**:
```powershell
cd presentation
npm run build
```

2. **Copiar archivos** (desde la carpeta `presentation`):
```powershell
Copy-Item -Path ".next-export\*" -Destination "..\public" -Recurse -Force
```

3. **Deploy** (desde la carpeta raíz del proyecto):
```powershell
cd ..
firebase deploy --only hosting
```

## 🌐 URL del Deploy

Después del deploy, Firebase te mostrará la URL. Normalmente será:
- `https://TU_PROYECTO.web.app`
- `https://TU_PROYECTO.firebaseapp.com`

## 🔧 Si tienes problemas de permisos

1. Cierra TODOS los procesos de Node.js
2. Reinicia PowerShell como Administrador
3. Intenta de nuevo

## 📝 Nota

Los archivos existentes en `public` (como `collection.html`) se preservarán, pero `index.html` será reemplazado por la presentación.

