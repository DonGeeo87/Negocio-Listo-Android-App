# 📦 Instrucciones para Publicar Release v1.0.2

## ✅ Estado Actual

- ✅ **APK generada**: `app/build/outputs/apk/release/app-release.apk` (16.41 MB)
- ✅ **APK copiada**: `app-release-v1.0.2.apk` (en la raíz del proyecto)
- ✅ **Notas de release**: `RELEASE_NOTES_v1.0.2.md`
- ✅ **Script de ayuda**: `scripts/create-release.ps1`
- ✅ **Landing actualizada**: Ya apunta a GitHub Releases

## 📋 Información del Release

- **Versión**: 1.0.2
- **Version Code**: 3
- **Tamaño APK**: ~16.4 MB
- **Android mínimo**: 7.0 (API 24)
- **Repositorio**: https://github.com/DonGeeo87/NegocioListoApp

## 🚀 Pasos para Publicar en GitHub

### Opción 1: Interfaz Web de GitHub (Recomendado)

1. **Ir a la página de releases**:
   ```
   https://github.com/DonGeeo87/NegocioListoApp/releases/new
   ```

2. **Completar el formulario**:
   - **Tag**: `v1.0.2` (o crear uno nuevo si prefieres)
   - **Título**: `Release v1.0.2 - Portal del Cliente como Core Feature`
   - **Descripción**: Copiar el contenido completo de `RELEASE_NOTES_v1.0.2.md`
   - **Archivos**: Arrastrar o seleccionar `app-release-v1.0.2.apk`
   - **Marcar como**: "Latest release" (si es la versión más reciente)
   - **Pre-release**: Desmarcar (si quieres que sea público)

3. **Publicar**: Hacer clic en "Publish release"

### Opción 2: GitHub CLI (gh)

Si tienes GitHub CLI instalado:

```powershell
# Desde la raíz del proyecto
gh release create v1.0.2 `
  "app-release-v1.0.2.apk" `
  --title "Release v1.0.2 - Portal del Cliente como Core Feature" `
  --notes-file "RELEASE_NOTES_v1.0.2.md" `
  --latest
```

### Opción 3: Usar el Script de Ayuda

```powershell
.\scripts\create-release.ps1
```

El script te mostrará instrucciones detalladas.

## 📝 Notas Adicionales

### Si el tag v1.0.2 ya existe:

Si el tag `v1.0.2` ya existe en GitHub y quieres actualizarlo:

1. **Eliminar el tag local** (opcional):
   ```powershell
   git tag -d v1.0.2
   ```

2. **Crear nuevo tag**:
   ```powershell
   git tag -a v1.0.2 -m "Release v1.0.2 - Portal del Cliente como Core Feature"
   git push origin v1.0.2 --force
   ```

3. **O crear un tag diferente** (ej: v1.0.2.1):
   ```powershell
   git tag -a v1.0.2.1 -m "Release v1.0.2.1 - Actualización"
   git push origin v1.0.2.1
   ```

### Verificar la APK

Antes de publicar, puedes verificar la APK:

```powershell
# Ver información de la APK
Get-Item "app-release-v1.0.2.apk" | Select-Object Name, Length, LastWriteTime

# Verificar que la APK está firmada (requiere Android SDK)
# aapt dump badging app-release-v1.0.2.apk | Select-String "package"
```

## 🌐 Actualización de la Landing

La landing ya está configurada para apuntar a:
- **Descarga**: https://github.com/DonGeeo87/NegocioListoApp/releases
- **Repositorio**: https://github.com/DonGeeo87/NegocioListoApp

No se requieren cambios adicionales en la landing.

## 📡 Presentación pública oficial

La experiencia que explicamos en este repo se complementa con dos URL públicas que muestran el producto y su propuesta de valor:

- **presentación de la app** — https://app-negocio-listo.web.app/presentacion-app: descripción del stack, arquitectura y beneficios para emprendedores, con énfasis en la capacidad offline-first y el Portal del Cliente como característica estrella.
- **landing general** — https://app-negocio-listo.web.app/landing: vista comercial que repite la promesa de gestión integral, módulos (inventario, ventas, clientes, gastos, facturación) y acceso directo al Portal del Cliente y descarga de la app.

Incluir estos enlaces en la documentación ayuda a que cualquier persona que revise el repo entienda fácilmente qué se está entregando y cómo se ve el producto en vivo.

## 🔐 Revisión de datos sensibles antes de publicar

Antes de hacer público este repositorio, confirmar que ningún archivo con credenciales privadas llega al remote:

- `local.properties` contiene rutas del SDK y contraseñas de keystore (`Limache87`) usadas solo localmente; no debe incluirse en Git (ya está en `.gitignore`).
- `config/keys/` alberga las claves y archivos de configuración de Firebase/Google; el directorio también está ignorado, pero vale la pena verificar que sigue fuera del control de versiones.
- Validar que no haya `.env`, `.jks`, ni `google-services.json` adicionales (fuera de los mínimos de la app) con tokens sensibles; estos casos típicamente ya se filtran mediante `.gitignore`.

Si se detecta alguna credencial sensible, moverla a un almacenamiento seguro (secret manager, variables de entorno) y actualizar la documentación para explicar cómo reconstruirla sin exponerla.

## ✅ Checklist Final

- [ ] APK generada y verificada
- [ ] Notas de release preparadas
- [ ] Tag creado/actualizado en GitHub
- [ ] Release publicado en GitHub
- [ ] APK adjuntada al release
- [ ] Landing verificada (ya está lista)
- [ ] Probar descarga desde GitHub

## 🎉 ¡Listo!

Una vez completados estos pasos, el release estará disponible en:
- **GitHub Releases**: https://github.com/DonGeeo87/NegocioListoApp/releases
- **Landing**: El botón de descarga apuntará automáticamente al release

---

**Desarrollador**: Giorgio Interdonato Palacios  
**GitHub**: [@DonGeeo87](https://github.com/DonGeeo87)

