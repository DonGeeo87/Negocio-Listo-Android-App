# 📊 Guía de Configuración Firebase Console - NegocioListo

**Desarrollador:** Giorgio Interdonato Palacios  
**GitHub:** @DonGeeo87  
**Versión:** 1.0.1

---

## 📋 Índice

1. [Configurar Conversiones en Analytics](#configurar-conversiones-en-analytics)
2. [Configurar Audiencias](#configurar-audiencias)
3. [Configurar Alertas de Crashlytics](#configurar-alertas-de-crashlytics)
4. [Monitorear Performance](#monitorear-performance)
5. [Verificar Configuración](#verificar-configuración)

---

## 🎯 Configurar Conversiones en Analytics

### 1. Acceder a Firebase Console

1. Ir a [Firebase Console](https://console.firebase.google.com/)
2. Seleccionar proyecto: `app-negocio-listo`
3. Ir a **Analytics** > **Events**

### 2. Crear Conversiones

Las conversiones son eventos que consideras importantes para tu negocio. Configura las siguientes:

#### Conversión: Venta Creada

1. Ir a **Analytics** > **Events**
2. Buscar evento `sale_created`
3. Click en el evento
4. Click en **Mark as conversion**
5. Nombre: "Venta Realizada"
6. Guardar

#### Conversión: Factura Generada

1. Buscar evento `invoice_generated`
2. Click en **Mark as conversion**
3. Nombre: "Factura Generada"
4. Guardar

#### Conversión: Cliente Agregado

1. Buscar evento `customer_added`
2. Click en **Mark as conversion**
3. Nombre: "Nuevo Cliente"
4. Guardar

#### Conversión: Colección Compartida

1. Buscar evento `collection_shared`
2. Click en **Mark as conversion**
3. Nombre: "Colección Compartida"
4. Guardar

#### Conversión: Pedido Creado

1. Buscar evento `order_created`
2. Click en **Mark as conversion**
3. Nombre: "Pedido Recibido"
4. Guardar

### 3. Ver Conversiones

1. Ir a **Analytics** > **Conversions**
2. Ver lista de conversiones configuradas
3. Ver métricas de conversión en tiempo real

---

## 👥 Configurar Audiencias

Las audiencias te permiten segmentar usuarios según su comportamiento.

### 1. Crear Audiencia: Usuarios Activos

1. Ir a **Analytics** > **Audiences**
2. Click en **New audience**
3. Nombre: "Usuarios Activos"
4. Condición:
   - Evento: `screen_view`
   - Parámetro: `screen_name`
   - Condición: "cualquier valor"
   - Período: Últimos 7 días
5. Guardar

### 2. Crear Audiencia: Vendedores Activos

1. Click en **New audience**
2. Nombre: "Vendedores Activos"
3. Condición:
   - Evento: `sale_created`
   - Condición: "al menos 1 evento"
   - Período: Últimos 30 días
4. Guardar

### 3. Crear Audiencia: Usuarios con Colecciones

1. Click en **New audience**
2. Nombre: "Usuarios con Colecciones"
3. Condición:
   - Evento: `collection_shared`
   - Condición: "al menos 1 evento"
   - Período: Últimos 30 días
4. Guardar

### 4. Crear Audiencia: Nuevos Usuarios

1. Click en **New audience**
2. Nombre: "Nuevos Usuarios"
3. Condición:
   - Evento: `sign_up`
   - Condición: "al menos 1 evento"
   - Período: Últimos 7 días
4. Guardar

### 5. Usar Audiencias

Las audiencias creadas se pueden usar para:
- **Notificaciones:** Enviar notificaciones push a audiencias específicas
- **Remote Config:** Personalizar la app según la audiencia
- **Análisis:** Ver métricas segmentadas por audiencia

---

## 🔔 Configurar Alertas de Crashlytics

### 1. Acceder a Crashlytics

1. Ir a **Crashlytics** en Firebase Console
2. Ver dashboard de crashes

### 2. Configurar Alertas por Email

1. Ir a **Crashlytics** > **Settings** (⚙️)
2. Click en **Alert settings**
3. Activar **Email alerts**
4. Configurar:
   - **Threshold:** 10 crashes en 1 hora (o según prefieras)
   - **Email recipients:** Agregar tu email
5. Guardar

### 3. Configurar Alertas para Issues Críticos

1. En **Crashlytics** > **Issues**
2. Click en un issue crítico
3. Click en **Set up alert**
4. Configurar:
   - **Alert type:** Email
   - **Threshold:** 1 crash nuevo
   - **Recipients:** Tu email
5. Guardar

### 4. Configurar Alertas para Nuevos Issues

1. Ir a **Crashlytics** > **Settings**
2. Click en **Alert settings**
3. Activar **New issue alerts**
4. Configurar:
   - **Email recipients:** Tu email
   - **Severity:** High y Critical
5. Guardar

---

## ⚡ Monitorear Performance

### 1. Ver Traces Personalizados

1. Ir a **Performance** en Firebase Console
2. Click en **Traces**
3. Ver traces personalizados:
   - `product_list_load` - Carga de productos
   - `image_upload` - Subida de imágenes
   - `sale_creation` - Creación de ventas
   - `invoice_generation` - Generación de facturas

### 2. Configurar Alertas de Performance

1. Ir a **Performance** > **Settings**
2. Click en **Alert settings**
3. Crear alerta:
   - **Metric:** Tiempo de carga de pantalla
   - **Threshold:** > 3 segundos
   - **Alert type:** Email
   - **Recipients:** Tu email
4. Guardar

### 3. Monitorear Métricas Clave

Revisa regularmente:
- **Tiempo de inicio de app:** Debe ser < 2 segundos
- **Tiempo de carga de pantallas:** Debe ser < 1 segundo
- **Tiempo de queries de Firestore:** Debe ser < 500ms
- **Tiempo de subida de imágenes:** Debe ser < 5 segundos

---

## ✅ Verificar Configuración

### Checklist de Verificación

- [ ] Conversiones configuradas en Analytics
- [ ] Al menos 3 audiencias creadas
- [ ] Alertas de Crashlytics configuradas
- [ ] Alertas de Performance configuradas (opcional)
- [ ] Email de notificaciones configurado
- [ ] Verificación de eventos en tiempo real

### Verificar Eventos en Tiempo Real

1. Ir a **Analytics** > **Events**
2. Click en **View real-time events**
3. Usar la app en un dispositivo
4. Verificar que los eventos aparecen en tiempo real:
   - `screen_view`
   - `sale_created`
   - `product_added`
   - `customer_added`
   - `collection_shared`
   - `order_created`

### Verificar Crashlytics

1. Ir a **Crashlytics**
2. Verificar que está habilitado
3. Probar reporte de crash (solo en desarrollo):
   ```kotlin
   // En código de prueba
   throw RuntimeException("Test crash")
   ```
4. Verificar que el crash aparece en Crashlytics

### Verificar Performance Monitoring

1. Ir a **Performance**
2. Verificar que está habilitado
3. Usar la app normalmente
4. Esperar 24-48 horas para ver datos
5. Verificar que aparecen traces personalizados

---

## 📊 Dashboard Personalizado

### Crear Dashboard en Analytics

1. Ir a **Analytics** > **Dashboard**
2. Click en **Create dashboard**
3. Agregar widgets:
   - **Conversiones diarias**
   - **Eventos más frecuentes**
   - **Usuarios activos**
   - **Retención de usuarios**
4. Guardar dashboard

---

## 🔄 Mantenimiento Regular

### Revisar Semanalmente

- [ ] Revisar crashes nuevos en Crashlytics
- [ ] Revisar métricas de conversión
- [ ] Revisar performance de traces críticos
- [ ] Revisar eventos anómalos

### Revisar Mensualmente

- [ ] Analizar tendencias de conversión
- [ ] Revisar crecimiento de audiencias
- [ ] Optimizar traces de performance
- [ ] Actualizar alertas según necesidades

---

## 📚 Recursos Adicionales

- [Firebase Analytics Documentation](https://firebase.google.com/docs/analytics)
- [Firebase Crashlytics Documentation](https://firebase.google.com/docs/crashlytics)
- [Firebase Performance Documentation](https://firebase.google.com/docs/perf-mon)

---

## ⚠️ Notas Importantes

1. **Privacidad:** Asegúrate de cumplir con las políticas de privacidad al configurar Analytics
2. **GDPR:** Si tienes usuarios en la UE, configura consentimiento de Analytics
3. **Límites:** Revisa los límites de Firebase Analytics y Crashlytics
4. **Costos:** Algunas funciones avanzadas pueden tener costos adicionales

---

**Última actualización:** Enero 2025  
**Versión:** 1.0.1

