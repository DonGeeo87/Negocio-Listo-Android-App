# 🔐 Configurar Variables de Entorno - Firebase Functions

**Desarrollador:** Giorgio Interdonato Palacios  
**GitHub:** @DonGeeo87  
**Versión:** 1.0.1

---

## 📋 Variables Necesarias

Para que la función `onOrderCreated` pueda enviar correos, necesitas configurar una de estas opciones:

### Opción 1: Gmail (Desarrollo/Pruebas)

```bash
firebase functions:secrets:set GMAIL_EMAIL
firebase functions:secrets:set GMAIL_PASSWORD
```

**Nota:** Para Gmail, necesitas usar una "App Password", no tu contraseña normal:
1. Ir a [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
2. Generar una contraseña de aplicación
3. Usar esa contraseña (no tu contraseña normal)

### Opción 2: SendGrid (Producción - Recomendado)

```bash
firebase functions:secrets:set SENDGRID_API_KEY
```

**Para obtener API Key de SendGrid:**
1. Crear cuenta en [https://sendgrid.com](https://sendgrid.com)
2. Ir a Settings > API Keys
3. Crear nueva API Key con permisos de "Mail Send"
4. Copiar la API Key (empieza con `SG.`)

---

## 🚀 Comandos de Configuración

### Configurar Gmail

```bash
# Configurar email de Gmail
firebase functions:secrets:set GMAIL_EMAIL
# Cuando te pida el valor, ingresa: tu-email@gmail.com

# Configurar contraseña de aplicación de Gmail
firebase functions:secrets:set GMAIL_PASSWORD
# Cuando te pida el valor, ingresa: tu-app-password (de 16 caracteres)
```

### Configurar SendGrid

```bash
# Configurar API Key de SendGrid
firebase functions:secrets:set SENDGRID_API_KEY
# Cuando te pida el valor, ingresa: SG.tu-api-key-aqui
```

### Ver Variables Configuradas

```bash
firebase functions:secrets:access GMAIL_EMAIL
firebase functions:secrets:access GMAIL_PASSWORD
firebase functions:secrets:access SENDGRID_API_KEY
```

### Eliminar Variables

```bash
firebase functions:secrets:destroy GMAIL_EMAIL
firebase functions:secrets:destroy GMAIL_PASSWORD
firebase functions:secrets:destroy SENDGRID_API_KEY
```

---

## ⚠️ Importante

1. **Después de configurar secrets**, necesitas **redesplegar la función** para que tome los nuevos valores:
   ```bash
   firebase deploy --only functions
   ```

2. **Los secrets son seguros** y no se muestran en logs ni código.

3. **Solo configura UNA opción** (Gmail O SendGrid), no ambas.

---

## ✅ Verificar Configuración

### Probar la Función

1. Crear un pedido desde una colección compartida en la app
2. Verificar logs:
   ```bash
   firebase functions:log --only onOrderCreated
   ```
3. Verificar que se recibió el correo

### Ver Logs en Tiempo Real

```bash
firebase functions:log --only onOrderCreated --follow
```

---

## 🔄 Actualizar Variables

Si necesitas cambiar las variables:

1. Configurar nuevo valor:
   ```bash
   firebase functions:secrets:set GMAIL_EMAIL
   ```

2. Redesplegar función:
   ```bash
   firebase deploy --only functions
   ```

---

**Última actualización:** Enero 2025  
**Versión:** 1.0.1

