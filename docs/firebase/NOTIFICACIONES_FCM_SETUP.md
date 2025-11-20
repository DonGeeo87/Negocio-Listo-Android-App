# 🔔 Configuración de Notificaciones Push (FCM) - NegocioListo

**Desarrollador**: Giorgio Interdonato Palacios  
**GitHub**: @DonGeeo87  
**Fecha**: Enero 2025

## 📋 Descripción

Este documento explica cómo configurar y desplegar las Cloud Functions para que las notificaciones push funcionen correctamente cuando:
- Un cliente envía un mensaje en el chat del portal
- Un cliente crea un pedido desde el portal

## ✅ Cambios Realizados

### 1. Cloud Function `onOrderCreated`
- **Antes**: Solo registraba el evento, no enviaba notificaciones
- **Ahora**: Envía notificación FCM al negocio cuando se crea un pedido

### 2. Cloud Function `onChatMessageCreated`
- **Estado**: Ya estaba implementada correctamente
- **Funcionalidad**: Envía notificación FCM cuando un cliente envía un mensaje

### 3. FCMService (Android)
- **Corregido**: Manejo correcto de datos de notificaciones
- **Mejorado**: Prioriza datos del payload "data" para mayor control

## 🚀 Pasos para Activar las Notificaciones

### Paso 1: Desplegar Cloud Functions

1. **Abrir terminal en la carpeta `functions`**:
```bash
cd functions
```

2. **Instalar dependencias** (si no están instaladas):
```bash
npm install
```

3. **Desplegar las funciones**:
```bash
firebase deploy --only functions
```

O desplegar solo las funciones de notificaciones:
```bash
firebase deploy --only functions:onOrderCreated,functions:onChatMessageCreated
```

### Paso 2: Verificar Token FCM en la App

1. **Abrir la app en el dispositivo**
2. **Iniciar sesión** con tu cuenta
3. **Verificar en Firestore** que el token FCM esté guardado:
   - Ir a Firebase Console → Firestore
   - Buscar la colección `users`
   - Abrir el documento de tu usuario
   - Verificar que exista el campo `fcmToken` con un valor

### Paso 3: Verificar Permisos de Notificaciones

1. **La app solicita automáticamente el permiso** cuando el usuario inicia sesión (Android 13+)
2. **Si no aparece el diálogo**, verificar en configuración del dispositivo:
   - Configuración → Apps → NegocioListo → Notificaciones
   - Asegurar que las notificaciones estén habilitadas
3. **Para Android 12 o inferior**, el permiso se otorga automáticamente al instalar la app

### Paso 4: Probar las Notificaciones

#### Probar Notificación de Pedido:
1. Abrir el portal del cliente en un navegador
2. Crear un pedido desde el portal
3. **Deberías recibir una notificación** en el teléfono con el mensaje:
   - Título: "📦 Nuevo pedido de [Nombre del Cliente]"
   - Cuerpo: "Pedido por $[Monto] - Ver detalles"

#### Probar Notificación de Chat:
1. Abrir el portal del cliente en un navegador
2. Enviar un mensaje en el chat
3. **Deberías recibir una notificación** en el teléfono con el mensaje:
   - Título: "💬 Mensaje de [Nombre del Cliente]"
   - Cuerpo: [Primeros 100 caracteres del mensaje]

## 🔍 Solución de Problemas

### Problema: No recibo notificaciones

**Verificaciones**:

1. **Token FCM guardado**:
   - Firestore → `users/{userId}` → Verificar campo `fcmToken`
   - Si no existe, cerrar sesión y volver a iniciar sesión en la app

2. **Cloud Functions desplegadas**:
   ```bash
   firebase functions:list
   ```
   Deberías ver `onOrderCreated` y `onChatMessageCreated`

3. **Logs de Cloud Functions**:
   ```bash
   firebase functions:log
   ```
   Buscar errores relacionados con FCM

4. **Permisos de notificaciones**:
   - Android 13+: Verificar en Configuración → Apps → NegocioListo → Notificaciones

5. **App en segundo plano**:
   - Las notificaciones funcionan incluso si la app está cerrada
   - Si la app está en primer plano, las notificaciones se muestran automáticamente

### Problema: Token FCM no se guarda

**Solución**:
1. Verificar que Firebase esté correctamente configurado en `google-services.json`
2. Verificar logs de la app:
   ```bash
   adb logcat | grep "Token FCM"
   ```
3. Cerrar sesión y volver a iniciar sesión

### Problema: Cloud Functions no se ejecutan

**Solución**:
1. Verificar que las funciones estén desplegadas:
   ```bash
   firebase functions:list
   ```
2. Verificar logs:
   ```bash
   firebase functions:log --only onOrderCreated
   firebase functions:log --only onChatMessageCreated
   ```
3. Verificar que el proyecto Firebase esté correcto:
   ```bash
   firebase projects:list
   ```

## 📝 Estructura de Notificaciones

### Notificación de Pedido
```json
{
  "type": "order",
  "title": "📦 Nuevo pedido de [Cliente]",
  "body": "Pedido por $[Monto] - Ver detalles",
  "collectionId": "[ID de colección]",
  "responseId": "[ID de pedido]",
  "clientName": "[Nombre del cliente]",
  "orderTotal": "[Monto total]"
}
```

### Notificación de Chat
```json
{
  "type": "chat",
  "title": "💬 Mensaje de [Cliente]",
  "body": "[Mensaje]",
  "customerId": "[ID del cliente]",
  "messageId": "[ID del mensaje]",
  "collectionId": "[ID de colección]"
}
```

## 🎯 Canales de Notificación

Los canales de notificación en Android están configurados así:

- **Chat**: `chat_notifications` (Importancia: ALTA)
- **Pedidos**: `order_notifications` (Importancia: ALTA)
- **Aprobaciones**: `approval_notifications` (Importancia: ALTA)
- **General**: `general_notifications` (Importancia: NORMAL)

## ✅ Checklist de Verificación

Antes de reportar problemas, verifica:

- [ ] Cloud Functions desplegadas (`firebase functions:list`)
- [ ] Token FCM guardado en Firestore (`users/{userId}/fcmToken`)
- [ ] Permisos de notificaciones habilitados en Android
- [ ] App actualizada con los últimos cambios
- [ ] Logs de Cloud Functions sin errores
- [ ] Usuario autenticado en la app

## 📚 Referencias

- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Cloud Functions para Firebase](https://firebase.google.com/docs/functions)
- [Notificaciones en Android](https://developer.android.com/develop/ui/views/notifications)

---

**Desarrollador**: Giorgio Interdonato Palacios  
**GitHub**: @DonGeeo87

