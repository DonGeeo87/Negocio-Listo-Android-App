# 🔥 Configuración de Cloud Functions - NegocioListo

**Desarrollador**: Giorgio Interdonato Palacios  
**GitHub**: @DonGeeo87  
**Fecha**: Noviembre 2025

## 📋 Descripción

Este documento explica cómo configurar y desplegar Cloud Functions de Firebase para el envío automático de correos cuando se crea un nuevo pedido.

## 🎯 Funcionalidad

La Cloud Function `onOrderCreated` se ejecuta automáticamente cuando:
- Un cliente crea un nuevo pedido desde la mini-web pública
- El pedido se guarda en Firestore en: `collections/{collectionId}/responses/{responseId}`

La función:
1. Detecta el nuevo pedido
2. Obtiene información de la colección y productos
3. Genera un correo de confirmación
4. Envía el correo al cliente automáticamente

## 📦 Requisitos Previos

1. **Node.js 18+** instalado
2. **Firebase CLI** instalado y configurado
3. **Cuenta de Firebase** con el proyecto `app-negocio-listo`
4. **Cuenta de correo** para enviar emails (Gmail, SendGrid, etc.)

### Instalar Firebase CLI

```bash
npm install -g firebase-tools
```

### Iniciar sesión en Firebase

```bash
firebase login
```

### Verificar proyecto

```bash
firebase projects:list
```

## 🔧 Configuración

### Opción 1: Usar Gmail (Recomendado para desarrollo)

1. **Habilitar App Password en Gmail:**
   - Ir a: https://myaccount.google.com/apppasswords
   - Generar una contraseña de aplicación
   - Copiar la contraseña generada

2. **Configurar variables de entorno en Firebase:**

```bash
firebase functions:config:set gmail.email="tu-email@gmail.com" gmail.password="tu-app-password"
```

### Opción 2: Usar SendGrid (Recomendado para producción)

1. **Crear cuenta en SendGrid:**
   - Ir a: https://sendgrid.com
   - Crear cuenta gratuita (permite 100 emails/día)
   - Ir a Settings > API Keys
   - Crear un nuevo API Key con permisos de "Mail Send"

2. **Configurar API Key en Firebase:**

```bash
firebase functions:config:set sendgrid.api_key="SG.tu-api-key-aqui"
```

### Opción 3: Usar otro proveedor SMTP

Edita `functions/index.js` y configura el transporter según tu proveedor:

```javascript
return nodemailer.createTransport({
  host: 'smtp.tu-proveedor.com',
  port: 587,
  secure: false,
  auth: {
    user: 'tu-email@ejemplo.com',
    pass: 'tu-contraseña'
  }
});
```

## 📥 Instalación

1. **Instalar dependencias:**

```bash
cd functions
npm install
```

2. **Verificar configuración:**

```bash
firebase functions:config:get
```

Deberías ver algo como:
```
{
  "gmail": {
    "email": "tu-email@gmail.com",
    "password": "****"
  }
}
```

## 🚀 Despliegue

### Desplegar todas las funciones

```bash
firebase deploy --only functions
```

### Desplegar una función específica

```bash
firebase deploy --only functions:onOrderCreated
```

### Ver logs en tiempo real

```bash
firebase functions:log
```

### Ver logs de una función específica

```bash
firebase functions:log --only onOrderCreated
```

## 🧪 Pruebas Locales

### Iniciar emulador de Functions

```bash
cd functions
npm run serve
```

O desde la raíz del proyecto:

```bash
firebase emulators:start --only functions
```

### Probar la función localmente

1. Abre otra terminal
2. Ejecuta:

```bash
curl -X POST http://localhost:5001/app-negocio-listo/us-central1/onOrderCreated
```

O usa el shell de Firebase:

```bash
firebase functions:shell
```

Luego en el shell:

```javascript
onOrderCreated({data: {clientEmail: "test@example.com", clientName: "Test"}}, {params: {collectionId: "test", responseId: "test123"}})
```

## 📊 Monitoreo

### Ver funciones desplegadas

```bash
firebase functions:list
```

### Ver estadísticas en Firebase Console

1. Ir a: https://console.firebase.google.com/project/app-negocio-listo/functions
2. Ver métricas de ejecución, errores, y logs

## 🔍 Solución de Problemas

### Error: "Configuración de correo no encontrada"

**Solución:** Configura las variables de entorno:

```bash
firebase functions:config:set gmail.email="tu-email@gmail.com" gmail.password="tu-password"
```

Luego vuelve a desplegar:

```bash
firebase deploy --only functions
```

### Error: "Authentication failed"

**Solución para Gmail:**
- Asegúrate de usar un "App Password", no tu contraseña normal
- Verifica que "Acceso de aplicaciones menos seguras" esté habilitado (si no usas App Password)

### Error: "Function execution took longer than expected"

**Solución:**
- La función tiene un timeout de 60 segundos por defecto
- Si necesitas más tiempo, edita `functions/index.js` y agrega:

```javascript
exports.onOrderCreated = functions
  .runWith({ timeoutSeconds: 120, memory: '256MB' })
  .firestore
  .document('collections/{collectionId}/responses/{responseId}')
  .onCreate(async (snap, context) => {
    // ... código
  });
```

### Los correos no se envían

1. **Verifica los logs:**
   ```bash
   firebase functions:log --only onOrderCreated
   ```

2. **Verifica que el pedido tenga email:**
   - El pedido debe tener `clientEmail` válido

3. **Verifica la configuración:**
   ```bash
   firebase functions:config:get
   ```

## 📝 Estructura de Archivos

```
functions/
├── index.js          # Código principal de las funciones
├── package.json      # Dependencias de Node.js
├── .eslintrc.js     # Configuración de ESLint
└── .gitignore       # Archivos a ignorar en Git
```

## 🔐 Seguridad

- **Nunca** subas las credenciales de correo al repositorio
- Usa `firebase functions:config:set` para configurar secretos
- Las variables de configuración están encriptadas en Firebase
- Considera usar Secret Manager de Google Cloud para producción

## 📚 Recursos Adicionales

- [Documentación de Firebase Functions](https://firebase.google.com/docs/functions)
- [Documentación de Nodemailer](https://nodemailer.com/about/)
- [Guía de SendGrid](https://sendgrid.com/docs/)
- [Firebase CLI Reference](https://firebase.google.com/docs/cli)

## ✅ Checklist de Despliegue

- [ ] Node.js 18+ instalado
- [ ] Firebase CLI instalado y configurado
- [ ] Dependencias instaladas (`npm install` en `functions/`)
- [ ] Variables de configuración establecidas (Gmail o SendGrid)
- [ ] Función probada localmente
- [ ] Función desplegada a Firebase
- [ ] Logs verificados
- [ ] Prueba de envío de correo exitosa

## 🎉 ¡Listo!

Una vez completado el despliegue, cada vez que un cliente cree un pedido desde la mini-web, recibirá automáticamente un correo de confirmación sin necesidad de que la app esté abierta.

---

**Desarrollador**: Giorgio Interdonato Palacios  
**GitHub**: @DonGeeo87


