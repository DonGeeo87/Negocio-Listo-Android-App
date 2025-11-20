# 🛠️ Scripts de Utilidad

**Desarrollador:** Giorgio Interdonato Palacios  
**GitHub:** @DonGeeo87

---

## 📋 Scripts Disponibles

### 🔨 Build y Desarrollo

- **`clean-and-build.ps1`**: Limpia el proyecto y ejecuta build debug
  ```powershell
  .\scripts\clean-and-build.ps1
  ```

### 🔥 Firebase

- **`deploy_firebase.ps1`**: Despliega Firebase Hosting
- **`deploy_functions.ps1`**: Despliega Cloud Functions

### 📊 Logs y Monitoreo

- **`capture-auth-logs.ps1`**: Captura logs de autenticación
- **`capture-backup-logs.ps1`**: Captura logs de backup
- **`monitor-logcat.ps1`**: Monitorea logcat en tiempo real

### 🧪 Testing

- **`EJECUTAR_PRUEBA_ANALYTICS.ps1`**: Ejecuta pruebas de Analytics

### 🎨 Assets

- **`generate_icons.ps1`**: Genera iconos de la app
- **`generate_icons_simple.ps1`**: Versión simplificada de generación de iconos
- **`update_internal_logos.ps1`**: Actualiza logos internos

### 📱 Dispositivos

- **`start-scrcpy.ps1`**: Inicia scrcpy para mostrar pantalla del dispositivo

### 🔄 Utilidades

- **`RESINCRONIZAR_IMAGENES.kt`**: Script Kotlin para resincronizar imágenes desde Firebase Storage

---

## 📝 Uso

Todos los scripts están diseñados para ejecutarse desde la raíz del proyecto:

```powershell
# Ejemplo: Build
.\scripts\clean-and-build.ps1

# Ejemplo: Deploy Firebase
.\scripts\deploy_firebase.ps1
```

---

## ⚠️ Notas

- Los scripts PowerShell requieren permisos de ejecución
- Algunos scripts pueden requerir configuración previa (Firebase CLI, ADB, etc.)
- Revisa cada script antes de ejecutarlo para entender qué hace

---

**Última actualización:** Noviembre 2025

