# 🔧 Solución: Problema de Autenticación (Login/Registro)

**Fecha:** 17 de Noviembre 2025  
**Desarrollador:** Giorgio Interdonato Palacios  
**GitHub:** @DonGeeo87

---

## 🔴 Problema Identificado

No se puede iniciar sesión ni crear cuenta en la app. Esto es muy probablemente porque el **SHA-1 del nuevo keystore no está registrado en Firebase Console**.

Firebase Auth requiere que el SHA-1 del certificado de firma esté registrado en Firebase Console para funcionar correctamente.

---

## ✅ Solución: Registrar SHA-1 en Firebase Console

### **Paso 1: Obtener SHA-1 del Keystore**

El SHA-1 del nuevo keystore es:
```
90:5A:91:D2:3E:B1:6D:08:D3:85:03:72:69:41:F2:BF:3F:02:5D:58
```

**SHA-256 (también recomendado):**
```
C6:F1:95:60:D2:AE:01:85:59:37:AD:93:80:E5:85:61:A6:EF:63:00:9E:D9:00:82:DD:FA:58:65:EE:9A:2D:E6
```

### **Paso 2: Agregar SHA-1 en Firebase Console**

1. Abre Firebase Console: https://console.firebase.google.com/project/app-negocio-listo/settings/general

2. En la sección **"Tus aplicaciones"**, busca la app Android (`com.negociolisto.app`)

3. Haz clic en el ícono de **configuración (⚙️)** o en **"Agregar huella digital"**

4. Agrega el SHA-1:
   - **SHA-1:** `90:5A:91:D2:3E:B1:6D:08:D3:85:03:72:69:41:F2:BF:3F:02:5D:58`
   - **SHA-256 (opcional pero recomendado):** `C6:F1:95:60:D2:AE:01:85:59:37:AD:93:80:E5:85:61:A6:EF:63:00:9E:D9:00:82:DD:FA:58:65:EE:9A:2D:E6`

5. Haz clic en **"Guardar"**

6. **Espera 5-10 minutos** para que los cambios se propaguen

### **Paso 3: Verificar Firebase Auth está Habilitado**

1. Ve a: https://console.firebase.google.com/project/app-negocio-listo/authentication/providers

2. Verifica que **"Correo electrónico/Contraseña"** esté **habilitado**

3. Si no está habilitado:
   - Haz clic en **"Correo electrónico/Contraseña"**
   - Activa el toggle
   - Haz clic en **"Guardar"**

### **Paso 4: Probar Nuevamente**

1. Desinstala la app del dispositivo:
   ```bash
   adb uninstall com.negociolisto.app
   ```

2. Reinstala el APK:
   ```bash
   adb install app\build\outputs\apk\release\app-release.apk
   ```

3. Intenta hacer login o crear cuenta nuevamente

---

## 🔍 Verificación Adicional

### **Verificar google-services.json**

El archivo `app/google-services.json` debe existir y contener la configuración correcta del proyecto Firebase.

### **Verificar Conectividad**

Asegúrate de que el dispositivo tenga conexión a internet.

### **Capturar Logs Detallados**

Si el problema persiste después de agregar el SHA-1, ejecuta:

```powershell
.\capture-auth-logs.ps1
```

Y luego intenta hacer login. Los logs mostrarán el error específico.

---

## 📝 Notas Importantes

- **El SHA-1 debe agregarse ANTES de usar Firebase Auth en producción**
- **Los cambios en Firebase Console pueden tardar 5-10 minutos en propagarse**
- **Si cambias de keystore, DEBES actualizar el SHA-1 en Firebase Console**
- **El SHA-1 del keystore de debug es diferente al de release**

---

## 🎯 Checklist de Verificación

- [ ] SHA-1 agregado en Firebase Console
- [ ] SHA-256 agregado en Firebase Console (opcional)
- [ ] Firebase Auth (Email/Password) habilitado
- [ ] Esperado 5-10 minutos después de agregar SHA-1
- [ ] App desinstalada y reinstalada
- [ ] Prueba de login/registro realizada

---

**Última actualización:** 17 de Noviembre 2025  
**Estado:** ⚠️ Pendiente agregar SHA-1 en Firebase Console

