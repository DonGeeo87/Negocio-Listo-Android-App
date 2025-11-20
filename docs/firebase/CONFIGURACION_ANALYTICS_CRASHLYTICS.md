# 📊 Configuración de Analytics, Crashlytics y Performance Monitoring - NegocioListo v1.0.1

**Fecha**: Enero 2025  
**Desarrollador**: Giorgio Interdonato Palacios - GitHub @DonGeeo87

---

## ✅ Configuración Completada

### **1. Firebase Analytics** ✅

#### **Dependencias**
- ✅ `firebase-analytics-ktx` agregado a `app/build.gradle.kts`
- ✅ Incluido en Firebase BOM (versión 32.7.0)

#### **Inicialización**
- ✅ Configurado en `NegocioListoApplication.onCreate()`
- ✅ Habilitado con `setAnalyticsCollectionEnabled(true)`

#### **Helper Creado**
- ✅ `AnalyticsHelper.kt` - Clase singleton para tracking de eventos
- ✅ Métodos para eventos comunes:
  - Inventario: `logProductAdded()`, `logProductUpdated()`, `logProductDeleted()`
  - Ventas: `logSaleCreated()`, `logInvoiceGenerated()`
  - Clientes: `logCustomerAdded()`
  - Colecciones: `logCollectionShared()`, `logOrderCreated()`
  - Autenticación: `logLogin()`, `logSignUp()`
  - Navegación: `logScreenView()`

#### **Uso**
```kotlin
@Inject lateinit var analyticsHelper: AnalyticsHelper

// En un ViewModel o composable
analyticsHelper.logProductAdded("Producto Ejemplo", "Categoría")
analyticsHelper.logSaleCreated(15000.0, 3)
analyticsHelper.setUserId(userId)
```

---

### **2. Firebase Crashlytics** ✅

#### **Plugin**
- ✅ Plugin agregado en `build.gradle.kts` (raíz) con versión 2.9.9
- ✅ Plugin aplicado en `app/build.gradle.kts`

#### **Dependencias**
- ✅ `firebase-crashlytics-ktx` agregado a `app/build.gradle.kts`
- ✅ Incluido en Firebase BOM (versión 32.7.0)

#### **Inicialización**
- ✅ Configurado en `NegocioListoApplication.onCreate()`
- ✅ Habilitado con `setCrashlyticsCollectionEnabled(true)`

#### **ProGuard Rules**
- ✅ Reglas agregadas en `app/proguard-rules.pro`:
  ```proguard
  -keep class com.google.firebase.crashlytics.** { *; }
  -dontwarn com.google.firebase.crashlytics.**
  ```

#### **Helper Creado**
- ✅ `CrashlyticsHelper.kt` - Clase singleton para manejo de crashes
- ✅ Métodos disponibles:
  - `log(message)` - Logs no fatales
  - `recordException(throwable)` - Registrar excepciones
  - `setCustomKey()` - Atributos personalizados
  - `setUserId()` - ID de usuario
  - `setCollectionEnabled()` - Habilitar/deshabilitar

#### **Uso**
```kotlin
@Inject lateinit var crashlyticsHelper: CrashlyticsHelper

// Registrar un log
crashlyticsHelper.log("Usuario inició sesión")

// Registrar una excepción
try {
    // código que puede fallar
} catch (e: Exception) {
    crashlyticsHelper.recordException(e)
}

// Establecer atributos personalizados
crashlyticsHelper.setCustomKey("user_type", "premium")
crashlyticsHelper.setUserId(userId)
```

---

### **3. Firebase Performance Monitoring** ✅

#### **Dependencias**
- ✅ `firebase-perf-ktx` agregado a `app/build.gradle.kts`
- ✅ Incluido en Firebase BOM (versión 32.7.0)

#### **Inicialización**
- ✅ Configurado en `NegocioListoApplication.onCreate()`
- ✅ Habilitado con `isPerformanceCollectionEnabled = true`

#### **ProGuard Rules**
- ✅ Reglas agregadas en `app/proguard-rules.pro`:
  ```proguard
  -keep class com.google.firebase.perf.** { *; }
  -keep class com.google.firebase.perf.metrics.** { *; }
  -dontwarn com.google.firebase.perf.**
  ```

#### **Helper Creado**
- ✅ `PerformanceHelper.kt` - Clase singleton para monitoreo de rendimiento
- ✅ Métodos disponibles:
  - `startTrace(traceName)` - Iniciar trace personalizado (retorna Trace)
  - `stopTrace(trace)` - Detener trace
  - `measureTrace(traceName, block)` - Medir bloque de código
  - Los métodos `putAttribute()` e `incrementMetric()` se usan directamente en la instancia de Trace

#### **Traces Predefinidos**
- ✅ `SCREEN_LOAD` - Carga de pantallas
- ✅ `PRODUCT_LIST_LOAD` - Carga de lista de productos
- ✅ `PRODUCT_DETAIL_LOAD` - Carga de detalle de producto
- ✅ `SALE_CREATION` - Creación de venta
- ✅ `INVOICE_GENERATION` - Generación de factura
- ✅ `FIREBASE_SYNC` - Sincronización con Firebase
- ✅ `BACKUP_OPERATION` - Operaciones de backup
- ✅ `RESTORE_OPERATION` - Operaciones de restauración
- ✅ `IMAGE_UPLOAD` - Subida de imágenes
- ✅ `DATABASE_QUERY` - Queries de base de datos

#### **Uso**
```kotlin
@Inject lateinit var performanceHelper: PerformanceHelper

// Medir tiempo de una operación
performanceHelper.measureTrace(PerformanceHelper.Traces.SCREEN_LOAD) {
    // Código a medir
    loadScreenData()
}

// Trace manual con atributos
val trace = performanceHelper.startTrace(PerformanceHelper.Traces.PRODUCT_LIST_LOAD)
trace.putAttribute("product_count", "100")
trace.incrementMetric(PerformanceHelper.Metrics.PRODUCT_COUNT, 100L)
// ... operación ...
performanceHelper.stopTrace(trace)
```

---

## 📋 Próximos Pasos

### **Integración en el Código** ⚠️ PENDIENTE

1. **Analytics**:
   - [ ] Integrar `AnalyticsHelper` en ViewModels principales
   - [ ] Agregar tracking de pantallas en navegación
   - [ ] Trackear eventos de negocio importantes (ventas, productos, etc.)

2. **Crashlytics**:
   - [ ] Integrar `CrashlyticsHelper` en manejo de errores
   - [ ] Agregar logs en puntos críticos
   - [ ] Registrar excepciones en catch blocks importantes

3. **Performance Monitoring**:
   - [ ] Integrar `PerformanceHelper` en carga de pantallas
   - [ ] Medir tiempos de queries de Firestore
   - [ ] Medir tiempos de operaciones de base de datos
   - [ ] Medir tiempos de sincronización
   - [ ] Medir tiempos de backup/restauración

### **Configuración en Firebase Console**

1. **Analytics**:
   - [ ] Verificar que Analytics esté habilitado en Firebase Console
   - [ ] Configurar eventos personalizados en Firebase Console
   - [ ] Configurar conversiones (si aplica)

2. **Crashlytics**:
   - [ ] Verificar que Crashlytics esté habilitado en Firebase Console
   - [ ] Configurar alertas de crashes
   - [ ] Revisar símbolos de debug (si es necesario)

---

## 🔧 Configuración Técnica

### **Archivos Modificados**

1. `app/build.gradle.kts`:
   - Plugin de Crashlytics agregado
   - Dependencias de Analytics y Crashlytics agregadas

2. `build.gradle.kts` (raíz):
   - Plugin de Crashlytics agregado con `apply false`

3. `app/proguard-rules.pro`:
   - Reglas de ProGuard para Analytics y Crashlytics

4. `app/src/main/java/com/negociolisto/app/NegocioListoApplication.kt`:
   - Inicialización de Analytics y Crashlytics

### **Archivos Creados**

1. `app/src/main/java/com/negociolisto/app/data/analytics/AnalyticsHelper.kt`
2. `app/src/main/java/com/negociolisto/app/data/analytics/CrashlyticsHelper.kt`
3. `app/src/main/java/com/negociolisto/app/data/analytics/PerformanceHelper.kt`

---

## 📊 Eventos de Analytics Disponibles

### **Eventos Automáticos**
- `screen_view` - Navegación entre pantallas
- `login` - Inicio de sesión
- `sign_up` - Registro de usuario

### **Eventos Personalizados** (via AnalyticsHelper)
- `product_added` - Producto agregado
- `product_updated` - Producto actualizado
- `product_deleted` - Producto eliminado
- `sale_created` - Venta creada
- `invoice_generated` - Factura generada
- `customer_added` - Cliente agregado
- `collection_shared` - Colección compartida
- `order_created` - Pedido creado

---

## 🎯 Beneficios

### **Analytics**
- 📊 Entender cómo los usuarios usan la app
- 📈 Identificar funcionalidades más utilizadas
- 🎯 Optimizar flujos de usuario
- 💡 Tomar decisiones basadas en datos

### **Crashlytics**
- 🔥 Detectar crashes en tiempo real
- 📝 Logs detallados de errores
- 👥 Información de usuario afectado
- 🚀 Priorizar fixes de bugs

### **Performance Monitoring**
- ⚡ Monitorear rendimiento de la app
- 📊 Identificar cuellos de botella
- 🎯 Optimizar tiempos de carga
- 💡 Mejorar experiencia de usuario

---

## ⚠️ Notas Importantes

1. **Privacidad**: 
   - Analytics, Crashlytics y Performance Monitoring recopilan datos de uso
   - Asegúrate de cumplir con políticas de privacidad
   - Considera agregar aviso de privacidad

2. **Rendimiento**:
   - Analytics, Crashlytics y Performance Monitoring tienen impacto mínimo en rendimiento
   - Los eventos se envían en background
   - Performance Monitoring puede tener un pequeño overhead, pero es despreciable

3. **Testing**:
   - En modo debug, los eventos pueden no aparecer inmediatamente
   - Verificar en Firebase Console después de 24 horas
   - Performance Monitoring requiere builds release para datos completos

---

**Última actualización**: Enero 2025  
**Estado**: ✅ Configuración base completada (Analytics, Crashlytics y Performance Monitoring) - Pendiente integración en código

