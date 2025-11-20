# 🧪 Guía de Testing - Módulo de Colecciones Extendidas

**Fecha:** 4 de Noviembre 2025  
**Desarrollador:** Giorgio Interdonato Palacios — GitHub @DonGeeo87

---

## 📋 Checklist de Testing Completo

### ✅ Fase 1: Preparación

- [ ] App Android compilada e instalada en dispositivo/emulador
- [ ] Firebase Hosting desplegado (✅ Ya desplegado: https://app-negocio-listo.web.app)
- [ ] Usuario autenticado en la app Android
- [ ] Al menos 3-5 productos creados en Inventario

---

## 🎨 Testing 1: Templates de Mini-Web

### Objetivo
Verificar que cada template se aplique correctamente en la mini-web.

### Pasos:

1. **Crear colecciones con diferentes templates:**
   - Abrir la app Android → Colecciones → Nueva Colección
   - Agregar productos a la colección
   - En la sección "🎨 Template de Mini-Web", seleccionar cada template:
     - ✅ MODERN (por defecto)
     - ✅ CLASSIC
     - ✅ MINIMAL
     - ✅ DARK
     - ✅ COLORFUL
   - Guardar cada colección con un nombre descriptivo (ej: "Test MODERN", "Test CLASSIC")

2. **Generar links públicos:**
   - En la lista de colecciones, tocar el botón de compartir (🔗)
   - Copiar el link generado
   - Verificar que el link incluya el parámetro `template=XXX`

3. **Probar en navegador:**
   - Abrir cada link en un navegador (Chrome recomendado)
   - Verificar que los estilos visuales correspondan al template:
     - **MODERN**: Gradientes azul/morado, diseño contemporáneo
     - **CLASSIC**: Bordes oscuros, diseño tradicional
     - **MINIMAL**: Diseño limpio, fondo blanco
     - **DARK**: Fondo oscuro, texto claro
     - **COLORFUL**: Colores vibrantes, animaciones

4. **Verificar funcionalidad:**
   - Confirmar que todos los templates muestran productos correctamente
   - Verificar que el formulario de pedido funcione en todos los templates
   - Probar en móvil y desktop

---

## 💬 Testing 2: Chat en Tiempo Real

### Objetivo
Verificar comunicación bidireccional entre cliente (mini-web) y negocio (app Android).

### Pasos:

1. **Desde la Mini-Web (Cliente):**
   - Abrir una colección con chat habilitado
   - Enviar un mensaje desde el chat
   - Verificar que el mensaje aparezca inmediatamente

2. **Desde la App Android (Negocio):**
   - Ir a Colecciones → Seleccionar la colección
   - Tocar el botón "Chat" o ir a "Ver Pedidos" → Seleccionar pedido → Chat
   - Verificar que el mensaje del cliente aparezca
   - Responder desde la app
   - Verificar en tiempo real que aparezca en la mini-web

3. **Verificar características:**
   - ✅ Mensajes en tiempo real sin recargar
   - ✅ Historial de mensajes persistente
   - ✅ Identificación de remitente (CLIENT vs BUSINESS)

---

## 📝 Testing 3: Sistema de Aprobaciones

### Objetivo
Verificar el flujo completo de doble aprobación (cliente + negocio).

### Pasos:

1. **Crear pedido desde Mini-Web:**
   - Abrir una colección en la mini-web
   - Agregar productos al carrito
   - Completar formulario de pedido:
     - ✅ Nombre del cliente
     - ✅ Email (obligatorio)
     - ✅ Teléfono
     - ✅ Método de entrega
     - ✅ Dirección (si es despacho)
     - ✅ Método de pago
   - Enviar pedido

2. **Aprobación del Cliente:**
   - En la mini-web, después de enviar el pedido
   - Verificar que aparezca la sección "Aprobación de Producción"
   - Marcar el checkbox "Doy mi visto bueno para iniciar la producción"
   - Tocar "Aprobar Pedido"
   - Verificar que aparezca el badge "✅ Aprobado"
   - Verificar que se envíe mensaje automático en el chat

3. **Aprobación del Negocio (App Android):**
   - Abrir la app → Colecciones → Seleccionar colección → "Pedidos"
   - Seleccionar el pedido recién creado
   - Verificar que aparezca el estado "Pendiente Aprobación Negocio"
   - Verificar que aparezca el checkbox de aprobación del cliente marcado
   - Marcar el checkbox "Aprobar condiciones de entrega, pago y términos"
   - Tocar "Aprobar Pedido"
   - Verificar que el estado cambie a "APROVED"
   - Verificar que se envíe mensaje automático en el chat

4. **Verificar sincronización:**
   - Refrescar la mini-web
   - Verificar que el estado se actualice en tiempo real
   - Verificar que aparezca "✅ Producción puede iniciar"

---

## 👤 Testing 4: Gestión Automática de Clientes

### Objetivo
Verificar que los clientes se crean/actualizan automáticamente.

### Pasos:

1. **Crear cliente nuevo desde Mini-Web:**
   - Hacer un pedido con un email nuevo
   - Enviar el pedido
   - Verificar en la app Android → Clientes
   - Confirmar que el cliente aparece con el email y datos del pedido

2. **Actualizar cliente existente:**
   - Hacer otro pedido desde la mini-web con el mismo email
   - Completar con datos diferentes (ej: teléfono nuevo)
   - Verificar en la app que el cliente se actualice (no crear duplicado)

3. **Verificar datos:**
   - ✅ Email usado como identificador único
   - ✅ Datos actualizados correctamente
   - ✅ Historial de pedidos asociado

---

## 🔔 Testing 5: Notificaciones Push (FCM)

### Objetivo
Verificar notificaciones cuando hay actividad en colecciones.

### Pasos:

1. **Configurar notificaciones:**
   - Abrir la app Android
   - Ir a Configuración → Notificaciones
   - Verificar que las notificaciones estén habilitadas
   - Verificar que el token FCM esté registrado (logs)

2. **Probar notificaciones:**
   - Desde la mini-web, enviar un mensaje en el chat
   - Verificar que llegue notificación push en la app Android
   - Tocar la notificación y verificar que abra el chat correcto

---

## 📧 Testing 6: Email Automático Post-Pedido

### Objetivo
Verificar envío de email de confirmación (si está configurado).

### Pasos:

1. **Crear pedido desde Mini-Web:**
   - Completar formulario con email válido
   - Enviar pedido

2. **Verificar email (Android App):**
   - El email se envía desde la app cuando el negocio revisa el pedido
   - Verificar logs de la app para confirmar envío
   - (Nota: Para mini-web, requiere Cloud Functions)

---

## 🎯 Testing 7: Flujo Completo End-to-End

### Escenario Completo:

1. **Negocio (App Android):**
   - ✅ Crear colección nueva
   - ✅ Agregar productos
   - ✅ Seleccionar template (ej: COLORFUL)
   - ✅ Habilitar chat
   - ✅ Cambiar estado a "Compartida"
   - ✅ Generar link público
   - ✅ Compartir link por WhatsApp

2. **Cliente (Mini-Web):**
   - ✅ Abrir link recibido
   - ✅ Verificar template aplicado (COLORFUL)
   - ✅ Ver productos con imágenes y precios
   - ✅ Agregar productos al carrito
   - ✅ Completar formulario de pedido
   - ✅ Enviar pedido
   - ✅ Aprobar pedido para producción
   - ✅ Enviar mensaje en chat

3. **Negocio (App Android):**
   - ✅ Recibir notificación push
   - ✅ Ver pedido en lista de pedidos
   - ✅ Abrir pedido y revisar detalles
   - ✅ Abrir chat y responder
   - ✅ Aprobar pedido
   - ✅ Cambiar estado a "En Producción"

4. **Cliente (Mini-Web):**
   - ✅ Refrescar página
   - ✅ Ver actualización de estado en tiempo real
   - ✅ Ver respuesta en chat

---

## 🐛 Problemas Comunes y Soluciones

### ❌ Template no se aplica en mini-web
- **Solución:** Verificar que el link incluya `&template=XXX`
- **Solución:** Limpiar caché del navegador
- **Solución:** Verificar que `collection.html` esté actualizado en Firebase Hosting

### ❌ Chat no funciona
- **Solución:** Verificar que `enableChat = true` en la colección
- **Solución:** Verificar reglas de Firestore para `/messages`
- **Solución:** Verificar conexión a Firebase

### ❌ Pedido no se crea
- **Solución:** Verificar que la colección esté en estado "Compartida" o "Activa"
- **Solución:** Verificar reglas de Firestore para `/responses`
- **Solución:** Revisar logs de consola en navegador

### ❌ Notificaciones no llegan
- **Solución:** Verificar permisos de notificaciones en Android
- **Solución:** Verificar que el token FCM esté registrado
- **Solución:** Verificar que el usuario esté autenticado

---

## ✅ Criterios de Éxito

### Funcionalidades Core:
- ✅ Los 5 templates se muestran correctamente
- ✅ Chat funciona bidireccionalmente en tiempo real
- ✅ Sistema de aprobaciones funciona completo
- ✅ Clientes se crean/actualizan automáticamente
- ✅ Links públicos funcionan con templates
- ✅ Sincronización en tiempo real funciona

### Experiencia de Usuario:
- ✅ Flujo intuitivo y fácil de seguir
- ✅ Feedback visual claro en cada paso
- ✅ Mensajes de error descriptivos
- ✅ Persistencia de datos funciona

---

## 📊 Resultados de Testing

**Fecha de Testing:** ___________  
**Tester:** ___________  
**Versión App:** ___________  
**Versión Mini-Web:** ___________  

### Templates:
- [ ] MODERN funcionando
- [ ] CLASSIC funcionando
- [ ] MINIMAL funcionando
- [ ] DARK funcionando
- [ ] COLORFUL funcionando

### Funcionalidades:
- [ ] Chat en tiempo real
- [ ] Sistema de aprobaciones
- [ ] Gestión de clientes
- [ ] Notificaciones push
- [ ] Email automático

### Flujo End-to-End:
- [ ] Creación de colección → Compartir → Pedido → Aprobación → Producción

---

**Desarrollador:** Giorgio Interdonato Palacios — GitHub @DonGeeo87
