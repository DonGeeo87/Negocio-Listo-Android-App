# 📊 Resumen: Eventos de Analytics

**Desarrollador:** Giorgio Interdonato Palacios  
**GitHub:** @DonGeeo87  
**Fecha:** Enero 2025

---

## ✅ Estado Actual

### Eventos Configurados en el Código

Los siguientes eventos están implementados y se envían cuando ocurren las acciones correspondientes:

1. **`sale_created`** - Se envía cuando se crea una venta
   - Parámetros: `total` (Double), `item_count` (Int)
   - Ubicaciones: `SalesViewModel.recordSale()`, `OrderDetailViewModel.createSaleFromOrder()`

2. **`product_added`** - Se envía cuando se agrega un producto
   - Parámetros: `product_name` (String), `category` (String)
   - Ubicación: `InventoryViewModel.addProduct()`

3. **`product_updated`** - Se envía cuando se actualiza un producto
   - Parámetros: `product_name` (String)
   - Ubicación: `InventoryViewModel.updateProduct()`

4. **`product_deleted`** - Se envía cuando se elimina un producto
   - Parámetros: `product_name` (String)
   - Ubicación: `InventoryViewModel.deleteProduct()`

5. **`customer_added`** - Se envía cuando se agrega un cliente
   - Parámetros: `customer_name` (String)
   - Ubicación: `CustomerViewModel.addCustomer()`

6. **`collection_shared`** - Se envía cuando se comparte una colección
   - Parámetros: `collection_id` (String), `template` (String)
   - Ubicación: `CollectionViewModel.shareCollection()`

7. **`order_created`** - Se envía cuando se crea un pedido desde una colección
   - Parámetros: `collection_id` (String), `order_value` (Double)
   - Ubicación: `OrderDetailViewModel.createSaleFromOrder()`

8. **`invoice_generated`** - Se envía cuando se genera una factura
   - Parámetros: `invoice_number` (String)
   - Ubicación: `SalesViewModel.generateInvoice()`

9. **`login`** - Se envía cuando un usuario inicia sesión
   - Parámetros: `method` (String: "email" o "google")
   - Ubicación: `AuthViewModel.login()`, `AuthViewModel.signInWithGoogle()`

10. **`sign_up`** - Se envía cuando un usuario se registra
    - Parámetros: `method` (String: "email" o "google")
    - Ubicación: `AuthViewModel.register()`, `AuthViewModel.signInWithGoogle()`

11. **`screen_view`** - Se envía automáticamente cuando cambia la pantalla
    - Parámetros: `screen_name` (String)
    - Ubicación: `MainScreen` (NavigationTrackingViewModel)

---

## ⚠️ Por Qué No Aparecen Inmediatamente

### 1. Tiempo de Procesamiento
- Los eventos personalizados pueden tardar **24-48 horas** en aparecer en Firebase Analytics
- Los eventos automáticos (`screen_view`, `user_engagement`) aparecen más rápido
- **Solución:** Esperar 24-48 horas después de generar eventos

### 2. Verificación en Tiempo Real
- Los eventos pueden verse en tiempo real en Firebase Console
- Ir a: Analytics > Events > View real-time events
- Debe aparecer en 1-2 minutos después de la acción

### 3. Modo Debug vs Release
- Algunos eventos pueden comportarse diferente en modo debug
- Probar con APK release para verificar comportamiento completo

---

## 🔍 Cómo Verificar que los Eventos Se Están Enviando

### Método 1: Logs de Android (ADB)

```bash
# Ver logs de Analytics
adb logcat | grep "Analytics"

# O filtrar por tag específico
adb logcat Analytics:* *:S
```

Deberías ver logs como:
```
Analytics: 📊 Enviando evento: sale_created con params: {total=1500.0, item_count=3}
Analytics: ✅ Evento sale_created enviado exitosamente
```

### Método 2: Firebase Console - Tiempo Real

1. Ir a: https://console.firebase.google.com/project/app-negocio-listo/analytics/events
2. Click en "View real-time events"
3. Usar la app y realizar acciones (crear venta, agregar producto, etc.)
4. Verificar que aparecen eventos en tiempo real (1-2 minutos de delay)

### Método 3: Firebase Console - Eventos Históricos

1. Ir a: https://console.firebase.google.com/project/app-negocio-listo/analytics/events
2. Ver lista completa de eventos
3. Buscar eventos personalizados (pueden tardar 24-48 horas)

---

## 📝 Nota sobre "sales_record"

El usuario mencionó que no aparece "sales_record", pero en el código estamos usando **`sale_created`** (no "sales_record"). 

Los eventos que deberían aparecer son:
- ✅ `sale_created` (no "sales_record")
- ✅ `product_added`
- ✅ `customer_added`
- ✅ `collection_shared`
- ✅ `order_created`
- ✅ `invoice_generated`
- ✅ `login`
- ✅ `sign_up`
- ✅ `screen_view` (ya aparece según la imagen)

---

## 🛠️ Cambios Realizados

1. ✅ Agregado logging en `AnalyticsHelper.logEvent()` para debug
2. ✅ Creado documento `SOLUCION_EVENTOS_ANALYTICS.md` con guía completa
3. ✅ Verificado que todos los eventos están correctamente implementados

---

## 📋 Próximos Pasos

1. **Probar en dispositivo real:**
   - Instalar APK release
   - Realizar acciones (crear venta, agregar producto, etc.)
   - Verificar logs con ADB

2. **Verificar en tiempo real:**
   - Abrir Firebase Console > Analytics > Events > View real-time events
   - Realizar acciones en la app
   - Verificar que aparecen eventos

3. **Esperar 24-48 horas:**
   - Los eventos históricos pueden tardar en aparecer
   - Revisar después de 24-48 horas

---

**Última actualización:** Enero 2025  
**Versión:** 1.0.1

