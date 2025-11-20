# 🔍 Solución: Eventos Personalizados No Aparecen en Firebase Analytics

**Problema:** Los eventos personalizados (`sale_created`, `product_added`, etc.) no aparecen en Firebase Analytics Console.

**Desarrollador:** Giorgio Interdonato Palacios  
**GitHub:** @DonGeeo87

---

## 🔍 Causas Posibles

### 1. **Tiempo de Procesamiento**
- Los eventos personalizados pueden tardar **24-48 horas** en aparecer en Firebase Analytics
- Los eventos automáticos (`screen_view`, `user_engagement`) aparecen más rápido
- **Solución:** Esperar 24-48 horas después de generar eventos

### 2. **Modo Debug**
- En modo debug, algunos eventos pueden no enviarse correctamente
- Firebase Analytics puede estar deshabilitado en builds debug
- **Solución:** Probar con APK release o verificar configuración

### 3. **Eventos No Se Están Enviando**
- El código puede no estar ejecutándose
- Errores silenciosos en el envío
- **Solución:** Agregar logging y verificar

---

## ✅ Verificación Paso a Paso

### Paso 1: Verificar que Analytics Está Habilitado

**En `NegocioListoApplication.kt`:**
```kotlin
val analytics = FirebaseAnalytics.getInstance(this)
analytics.setAnalyticsCollectionEnabled(true) // ✅ Debe estar en true
```

### Paso 2: Verificar que los Eventos Se Están Llamando

Agregar logging temporal para verificar:

```kotlin
fun logSaleCreated(total: Double, itemCount: Int) {
    println("📊 Analytics: Enviando evento sale_created - Total: $total, Items: $itemCount")
    logEvent("sale_created", mapOf(
        "total" to total,
        "item_count" to itemCount
    ))
    println("✅ Analytics: Evento sale_created enviado")
}
```

### Paso 3: Verificar en Tiempo Real

1. Ir a Firebase Console > Analytics > Events
2. Click en "View real-time events"
3. Usar la app y realizar acciones (agregar producto, crear venta, etc.)
4. Verificar que aparecen eventos en tiempo real

**Nota:** Los eventos en tiempo real pueden tardar 1-2 minutos en aparecer.

### Paso 4: Verificar con ADB (Android Debug Bridge)

```bash
# Ver logs de Firebase Analytics
adb logcat | grep -i "firebase\|analytics"

# O filtrar por tag específico
adb logcat FirebaseAnalytics:* *:S
```

---

## 🛠️ Soluciones

### Solución 1: Agregar Logging para Debug

Modificar `AnalyticsHelper.kt` para agregar logging:

```kotlin
fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
    // Logging para debug
    android.util.Log.d("Analytics", "📊 Enviando evento: $eventName con params: $params")
    
    try {
        analytics.logEvent(eventName) {
            params.forEach { (key, value) ->
                param(key, value.toString())
            }
        }
        android.util.Log.d("Analytics", "✅ Evento $eventName enviado exitosamente")
    } catch (e: Exception) {
        android.util.Log.e("Analytics", "❌ Error enviando evento $eventName: ${e.message}")
    }
}
```

### Solución 2: Verificar en Modo Release

Los eventos pueden funcionar diferente en release vs debug:

```bash
# Generar APK release
.\gradlew assembleRelease

# Instalar en dispositivo
.\gradlew installRelease

# Probar funcionalidades y verificar eventos
```

### Solución 3: Verificar Configuración de Firebase

1. Verificar que `google-services.json` está en `app/`
2. Verificar que el `applicationId` coincide con Firebase Console
3. Verificar que Analytics está habilitado en Firebase Console

---

## 📊 Eventos Configurados

Los siguientes eventos deberían aparecer en Analytics:

### Eventos de Inventario
- `product_added` - Cuando se agrega un producto
- `product_updated` - Cuando se actualiza un producto
- `product_deleted` - Cuando se elimina un producto

### Eventos de Ventas
- `sale_created` - Cuando se crea una venta
- `invoice_generated` - Cuando se genera una factura

### Eventos de Clientes
- `customer_added` - Cuando se agrega un cliente

### Eventos de Colecciones
- `collection_shared` - Cuando se comparte una colección
- `order_created` - Cuando se crea un pedido desde una colección

### Eventos de Autenticación
- `login` - Cuando un usuario inicia sesión
- `sign_up` - Cuando un usuario se registra

### Eventos de Navegación
- `screen_view` - Cuando se visita una pantalla (ya aparece)

---

## 🔍 Verificación en Firebase Console

### Ver Eventos en Tiempo Real

1. Ir a: https://console.firebase.google.com/project/app-negocio-listo/analytics/events
2. Click en "View real-time events"
3. Usar la app y realizar acciones
4. Verificar que aparecen eventos

### Ver Todos los Eventos

1. Ir a: https://console.firebase.google.com/project/app-negocio-listo/analytics/events
2. Ver lista completa de eventos
3. Buscar eventos personalizados (pueden tardar 24-48 horas)

---

## ⚠️ Notas Importantes

1. **Tiempo de procesamiento:** Los eventos personalizados pueden tardar hasta 48 horas en aparecer
2. **Modo debug:** Algunos eventos pueden no enviarse en modo debug
3. **Límites:** Firebase Analytics tiene límites en el número de eventos personalizados
4. **Nombres de eventos:** Deben seguir convenciones (máximo 40 caracteres, sin espacios)

---

## 🧪 Prueba Rápida

Para verificar que los eventos se están enviando:

1. **Agregar logging temporal:**
   ```kotlin
   fun logSaleCreated(total: Double, itemCount: Int) {
       android.util.Log.d("Analytics", "📊 Enviando sale_created: total=$total, items=$itemCount")
       logEvent("sale_created", mapOf(
           "total" to total,
           "item_count" to itemCount
       ))
   }
   ```

2. **Crear una venta en la app**

3. **Verificar logs:**
   ```bash
   adb logcat | grep "Analytics"
   ```

4. **Verificar en tiempo real:**
   - Firebase Console > Analytics > Events > View real-time events
   - Debe aparecer `sale_created` en 1-2 minutos

---

## 📝 Checklist de Verificación

- [ ] Analytics habilitado en `NegocioListoApplication`
- [ ] `google-services.json` presente y correcto
- [ ] Eventos se están llamando (verificar con logging)
- [ ] Probar en modo release (no solo debug)
- [ ] Verificar eventos en tiempo real en Firebase Console
- [ ] Esperar 24-48 horas para ver eventos históricos

---

**Última actualización:** Enero 2025  
**Versión:** 1.0.1

