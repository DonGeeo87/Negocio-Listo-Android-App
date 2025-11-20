# 🔒 Solución para Release en Repositorio Privado

## Problema

El token de GitHub no tiene acceso al repositorio privado `NegocioListoApp`.

## Solución: Actualizar Token con Permisos Completos

### Paso 1: Crear/Actualizar Token

1. Ve a: https://github.com/settings/tokens
2. Si ya tienes un token, edítalo. Si no, crea uno nuevo:
   - Click en "Generate new token" → "Generate new token (classic)"
3. **Configuración del token**:
   - **Note**: "NegocioListo Release - Full Access"
   - **Expiration**: Elige una duración (recomendado: 90 días o No expiration)
   - **Scopes**: **Marca TODOS estos permisos**:
     - ✅ **repo** (acceso completo a repositorios privados)
       - ✅ repo:status
       - ✅ repo_deployment
       - ✅ public_repo
       - ✅ repo:invite
       - ✅ security_events
   - Click en "Generate token"
4. **Copia el token** (solo se muestra una vez)

### Paso 2: Usar el Nuevo Token

Una vez que tengas el token con permisos `repo` completos, ejecuta:

```powershell
.\scripts\create-release-now.ps1
```

O proporciona el nuevo token y lo ejecuto automáticamente.

## Alternativa: Crear Release Manualmente

Si prefieres hacerlo manualmente (más rápido):

1. **Abre GitHub**: https://github.com/DonGeeo87/NegocioListoApp/releases/new

2. **Completa el formulario**:
   - **Tag**: Selecciona `v1.0.2` (ya existe)
   - **Título**: `Release v1.0.2 - Portal del Cliente como Core Feature`
   - **Descripción**: 
     - Abre `RELEASE_NOTES_v1.0.2.md`
     - Copia TODO el contenido
     - Pégalo en el campo de descripción
   - **Archivos**: 
     - Arrastra o selecciona `app-release-v1.0.2.apk`
     - Ubicación: `C:\Users\DonGeeo87\AndroidStudioProjects\NegocioListo2\app-release-v1.0.2.apk`
   - **Latest release**: ✅ Marca esta casilla
   - **Pre-release**: ❌ NO marques

3. **Publica**: Click en "Publish release"

## Verificación

Después de crear el release, estará disponible en:
- https://github.com/DonGeeo87/NegocioListoApp/releases/latest

---

**Nota**: Para repositorios privados, el token DEBE tener el scope `repo` completo.

