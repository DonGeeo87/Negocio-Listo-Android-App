# 🔥 Guía de Despliegue Firebase - NegocioListo

**Desarrollador:** Giorgio Interdonato Palacios  
**GitHub:** @DonGeeo87  
**Versión:** 1.0.1

---

## 📋 Índice

1. [Prerrequisitos](#prerrequisitos)
2. [Desplegar Cloud Functions](#desplegar-cloud-functions)
3. [Desplegar Firebase Hosting](#desplegar-firebase-hosting)
4. [Configurar Variables de Entorno](#configurar-variables-de-entorno)
5. [Verificación](#verificación)

---

## 🔧 Prerrequisitos

### 1. Instalar Firebase CLI

```bash
npm install -g firebase-tools
```

### 2. Iniciar Sesión en Firebase

```bash
firebase login
```

### 3. Verificar Proyecto

```bash
firebase projects:list
```

Debe aparecer `app-negocio-listo` en la lista.

---

## ⚡ Desplegar Cloud Functions

### 1. Instalar Dependencias

```bash
cd functions
npm install
```

### 2. Configurar Variables de Entorno

#### Opción A: Usar Gmail (Desarrollo)

```bash
firebase functions:config:set gmail.email="tu-email@gmail.com" gmail.password="tu-app-password"
```

**Nota:** Para obtener App Password de Gmail:
1. Ir a [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
2. Generar una contraseña de aplicación
3. Usar esa contraseña (no tu contraseña normal)

#### Opción B: Usar SendGrid (Producción - Recomendado)

```bash
firebase functions:config:set sendgrid.api_key="SG.tu-api-key-aqui"
```

**Para obtener API Key de SendGrid:**
1. Crear cuenta en [https://sendgrid.com](https://sendgrid.com)
2. Ir a Settings > API Keys
3. Crear nueva API Key con permisos de "Mail Send"
4. Copiar la API Key

### 3. Desplegar Functions

```bash
# Desde la raíz del proyecto
firebase deploy --only functions
```

O desde la carpeta `functions`:

```bash
cd functions
npm run deploy
```

### 4. Verificar Despliegue

```bash
firebase functions:list
```

Debe aparecer la función `onOrderCreated`.

### 5. Ver Logs

```bash
firebase functions:log
```

---

## 🌐 Desplegar Firebase Hosting

### 1. Verificar Archivos

Asegúrate de que los siguientes archivos existan en `public/`:
- `customer-portal.html`
- `collection.html`
- `index.html` (opcional)

### 2. Desplegar Hosting

```bash
# Desde la raíz del proyecto
firebase deploy --only hosting
```

### 3. Verificar Despliegue

Abre en el navegador:
- Portal del cliente: `https://app-negocio-listo.web.app/customer-portal.html`
- Colección pública: `https://app-negocio-listo.web.app/collection.html`

### 4. Configurar Dominio Personalizado (Opcional)

```bash
firebase hosting:channel:deploy production
```

O desde Firebase Console:
1. Ir a Firebase Console > Hosting
2. Agregar dominio personalizado
3. Seguir las instrucciones de verificación DNS

---

## 🔐 Configurar Variables de Entorno

### Ver Variables Actuales

```bash
firebase functions:config:get
```

### Configurar Variables

```bash
# Gmail
firebase functions:config:set gmail.email="tu-email@gmail.com" gmail.password="tu-app-password"

# SendGrid
firebase functions:config:set sendgrid.api_key="SG.tu-api-key"

# Múltiples variables a la vez
firebase functions:config:set gmail.email="email@gmail.com" gmail.password="password" sendgrid.api_key="SG.key"
```

### Eliminar Variables

```bash
firebase functions:config:unset gmail.email gmail.password
```

---

## ✅ Verificación

### 1. Verificar Cloud Functions

```bash
# Listar funciones desplegadas
firebase functions:list

# Ver logs en tiempo real
firebase functions:log --only onOrderCreated

# Probar función localmente (requiere emulador)
firebase emulators:start --only functions
```

### 2. Verificar Hosting

```bash
# Ver estado del hosting
firebase hosting:sites:list

# Ver historial de despliegues
firebase hosting:channel:list
```

### 3. Probar Funcionalidad

1. **Probar Cloud Function:**
   - Crear un pedido desde una colección compartida
   - Verificar que se recibe el correo de confirmación
   - Revisar logs: `firebase functions:log`

2. **Probar Hosting:**
   - Abrir `https://app-negocio-listo.web.app/customer-portal.html`
   - Verificar que carga correctamente
   - Probar funcionalidad de pedidos

---

## 🐛 Troubleshooting

### Error: "Functions did not deploy"

**Solución:**
```bash
# Verificar que Node.js está instalado (versión 18)
node --version

# Reinstalar dependencias
cd functions
rm -rf node_modules
npm install

# Intentar desplegar nuevamente
firebase deploy --only functions
```

### Error: "Hosting deploy failed"

**Solución:**
```bash
# Verificar que los archivos existen
ls -la public/

# Verificar firebase.json
cat firebase.json

# Limpiar cache y reintentar
firebase deploy --only hosting --force
```

### Error: "Config not found"

**Solución:**
```bash
# Verificar configuración
firebase functions:config:get

# Si está vacío, configurar variables
firebase functions:config:set gmail.email="email@gmail.com" gmail.password="password"
```

---

## 📊 Monitoreo

### Ver Métricas de Functions

1. Ir a Firebase Console > Functions
2. Ver métricas de ejecución, errores, latencia

### Ver Métricas de Hosting

1. Ir a Firebase Console > Hosting
2. Ver estadísticas de tráfico, errores 404, etc.

---

## 🔄 Actualizar Despliegue

### Actualizar Functions

```bash
# Después de modificar functions/index.js
cd functions
npm install  # Si agregaste nuevas dependencias
firebase deploy --only functions
```

### Actualizar Hosting

```bash
# Después de modificar archivos en public/
firebase deploy --only hosting
```

### Actualizar Todo

```bash
firebase deploy
```

---

## 📝 Notas Importantes

1. **Variables de Entorno:** Las variables configuradas con `firebase functions:config:set` están disponibles en producción. Para desarrollo local, usar `.env` o emulador.

2. **Costos:** Firebase Functions tiene un plan gratuito generoso, pero revisa los límites en [Firebase Pricing](https://firebase.google.com/pricing).

3. **Seguridad:** Nunca commitees variables de entorno o API keys en el repositorio. Usa `firebase functions:config:set` para producción.

4. **Backup:** Antes de desplegar, asegúrate de tener backup de tus archivos locales.

---

## 📚 Recursos Adicionales

- [Documentación de Firebase Functions](https://firebase.google.com/docs/functions)
- [Documentación de Firebase Hosting](https://firebase.google.com/docs/hosting)
- [Firebase CLI Reference](https://firebase.google.com/docs/cli)

---

**Última actualización:** Enero 2025  
**Versión:** 1.0.1

