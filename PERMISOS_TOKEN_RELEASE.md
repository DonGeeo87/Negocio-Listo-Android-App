# 🔑 Permisos Necesarios para Crear Releases

## Permiso Requerido

Para crear releases en GitHub, necesitas activar el permiso **"Contents"** en tu token.

### Pasos:

1. En la página de configuración del token, busca la sección **"Permissions"**
2. Busca **"Contents"** en la lista
3. **Marca la casilla** (puede ser "Read and write" o solo "Write")
4. **Guarda** los cambios del token

### ¿Qué hace el permiso "Contents"?

El permiso "Contents" incluye:
- ✅ Repository contents
- ✅ Commits
- ✅ Branches
- ✅ Downloads
- ✅ **RELEASES** ← Esto es lo que necesitas
- ✅ Merges

### Otros permisos útiles (opcionales):

- **Metadata**: Ya está marcado como "Required" - déjalo así
- **Pull requests**: Si quieres crear releases desde PRs
- **Issues**: Si quieres vincular releases con issues

---

**Una vez que actives "Contents", guarda el token y vuelve a intentar crear el release.**

