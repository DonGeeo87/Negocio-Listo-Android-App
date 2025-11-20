# 🔒 Límites de Uso - NegocioListo

**Versión**: 1.0  
**Fecha**: Enero 2025  
**Desarrollador**: Giorgio Interdonato Palacios - GitHub @DonGeeo87

---

## 📊 Capacidades del Plan Spark (Gratuito) de Firebase

### **Cloud Firestore**
- **Almacenamiento**: 1 GB gratuito
- **Escrituras diarias**: 20,000 operaciones/día
- **Lecturas diarias**: 50,000 operaciones/día
- **Eliminaciones diarias**: 20,000 operaciones/día
- **Tamaño máximo por documento**: 1 MB
- **Frecuencia de escritura**: Máximo 1 escritura por segundo por documento

### **Cloud Storage**
- **Almacenamiento**: 5 GB gratuitos
- **Descargas diarias**: 1 GB/día
- **Cargas mensuales**: 20,000 operaciones/mes
- **Descargas mensuales**: 50,000 operaciones/mes

### **Firebase Authentication**
- **Usuarios activos mensuales (MAU)**: 50,000 usuarios
- **Usuarios activos diarios (DAU)**: 3,000 usuarios
- **Autenticación telefónica (SMS)**: No incluida (costo adicional)

---

## 🎯 Límites por Usuario

Basados en las capacidades de Firebase Spark y optimizado para permitir **más usuarios activos**, hemos establecido los siguientes límites conservadores por usuario:

### **Productos**
- **Límite máximo**: **100 productos por usuario**
- **Justificación**: 
  - Considerando actualizaciones de stock y modificaciones frecuentes
  - Cada producto puede generar múltiples escrituras (creación, actualizaciones de stock, cambios de precio)
  - Optimizado para permitir más usuarios en el sistema
  - Permite margen para operaciones adicionales (ventas, movimientos de stock)

### **Clientes**
- **Límite máximo**: **50 clientes por usuario**
- **Justificación**:
  - Similar a productos, los clientes pueden actualizarse frecuentemente
  - Cada cliente puede generar escrituras adicionales (actualización de compras, notas)
  - Optimizado para permitir más usuarios en el sistema

### **Colecciones**
- **Límite máximo**: **50 colecciones por usuario**
- **Justificación**:
  - Las colecciones son menos frecuentes que productos/clientes
  - Cada colección puede contener múltiples productos (subcolecciones)
  - Permite suficiente espacio para catálogos y colecciones especiales

### **Almacenamiento de Imágenes**
- **Límite máximo**: **~30 MB por usuario** (optimizado)
- **Justificación**:
  - Optimizado para permitir más usuarios (hasta ~153 usuarios activos)
  - Considerando compresión automática de imágenes (800×600px, 75-85% calidad)
  - Cada imagen de producto comprimida ≈ 150-200 KB
  - Permite aproximadamente 150-200 imágenes por usuario
- **Límite total del sistema**: **4.5 GB** (90% de 5 GB para prevenir exceder el plan gratuito)

---

## ⚠️ Sistema de Advertencias

El sistema implementa tres niveles de advertencia:

### **Estado OK** (< 80% del límite)
- Uso normal, sin restricciones
- No se muestran advertencias

### **Estado WARNING** (80% - 95% del límite)
- Se muestra advertencia informativa
- Mensaje: "Has usado el X% de tu límite de [tipo] (actual/máximo)"
- El usuario puede seguir agregando elementos

### **Estado CRITICAL** (≥ 95% del límite)
- Se muestra advertencia crítica
- Mensaje: "Estás cerca del límite de X [tipo] (actual/máximo). Considera eliminar items no utilizados."
- El usuario puede seguir agregando hasta alcanzar el límite máximo

### **Límite Alcanzado** (= 100% del límite)
- No se permite agregar más elementos
- Mensaje: "Has alcanzado el límite de X [tipo]. Para agregar más, considera actualizar a un plan superior."
- Se bloquea la creación de nuevos elementos

---

## 🔧 Implementación Técnica

### **Servicio de Límites**
- **Clase**: `UsageLimitsService`
- **Ubicación**: `app/src/main/java/com/negociolisto/app/data/service/UsageLimitsService.kt`
- **Responsabilidades**:
  - Verificar límites antes de crear elementos
  - Calcular porcentajes de uso
  - Determinar estado (OK, WARNING, CRITICAL)
  - Proporcionar mensajes informativos
  - **Verificar capacidad de Storage antes de permitir nuevos usuarios**

### **Validaciones Integradas**
Los siguientes ViewModels verifican límites antes de agregar elementos:
- `InventoryViewModel` - Verifica límite de productos
- `CustomerViewModel` - Verifica límite de clientes
- `CollectionViewModel` - Verifica límite de colecciones
- `FirebaseAuthRepository` - Verifica capacidad de Storage antes de crear cuenta

### **Métodos de Conteo**
Se agregaron métodos a los repositorios para contar elementos:
- `InventoryRepository.getTotalProductCount()`
- `CustomerRepository.getTotalCustomerCount()`
- `CollectionRepository.getTotalCollectionCount()`
- `AuthRepository.getTotalUserCount()` - Para verificar capacidad de Storage

### **Verificación de Capacidad de Storage**
- **Método**: `checkStorageCapacityForNewUser()`
- **Funcionamiento**: 
  - Calcula el uso estimado de Storage basándose en el número de usuarios existentes
  - Verifica si agregar un nuevo usuario excedería el 90% de capacidad (4.5 GB)
  - Bloquea el registro si no hay capacidad disponible
  - Muestra mensaje informativo al usuario cuando no se puede crear cuenta

---

## 📈 Monitoreo y Ajustes

### **Recomendaciones**
1. **Monitorear uso real**: Revisar Firebase Console regularmente para verificar uso real de recursos
2. **Ajustar límites**: Si el número de usuarios activos cambia significativamente, ajustar los límites en `UsageLimitsService`
3. **Optimizar operaciones**: Minimizar escrituras innecesarias (usar batch operations cuando sea posible)
4. **Comprimir imágenes**: Ya implementado - las imágenes se comprimen automáticamente antes de subir

### **Escalabilidad Futura**
Si el proyecto crece más allá de las capacidades del plan Spark:
- **Plan Blaze (Pay-as-you-go)**: Permite escalar sin límites estrictos
- **Costos estimados**: 
  - Firestore: $0.18 por 100,000 lecturas, $0.18 por 100,000 escrituras
  - Storage: $0.026 por GB/mes
  - Con uso moderado, los costos pueden ser muy bajos

---

## 📝 Notas Importantes

1. **Límites conservadores**: Los límites establecidos son conservadores para evitar exceder las cuotas de Firebase
2. **Flexibilidad**: Los límites pueden ajustarse fácilmente modificando las constantes en `UsageLimitsService`
3. **Experiencia del usuario**: Los mensajes son informativos y no bloquean la funcionalidad hasta alcanzar el límite máximo
4. **Offline-first**: La app funciona offline, pero las validaciones de límites se realizan antes de sincronizar con Firebase

---

## 🔄 Actualización de Límites

Para ajustar los límites, modificar las constantes en `UsageLimitsService.kt`:

```kotlin
companion object {
    // Límites por usuario
    const val MAX_PRODUCTS_PER_USER = 100
    const val MAX_CUSTOMERS_PER_USER = 50
    const val MAX_COLLECTIONS_PER_USER = 50
    
    // Límites de almacenamiento
    const val MAX_STORAGE_PER_USER_MB = 30  // Optimizado para permitir más usuarios
    const val MAX_STORAGE_TOTAL_MB = 5120   // 5 GB en MB
    const val STORAGE_LIMIT_PERCENT = 90     // 90% de capacidad máxima
    
    // Porcentajes de advertencia
    const val WARNING_THRESHOLD_PERCENT = 80
    const val CRITICAL_THRESHOLD_PERCENT = 95
}
```

---

## 📊 Capacidad Máxima del Sistema

Con los límites actuales optimizados:
- **Espacio por usuario**: 30 MB
- **Límite total**: 5 GB (5,120 MB)
- **Límite al 90%**: 4,608 MB (para prevenir exceder el plan gratuito)
- **Máximo teórico de usuarios**: **~153 usuarios activos simultáneos**

### **Cálculo de Capacidad**
```
Límite al 90% = 4,608 MB
Espacio por usuario = 30 MB
Máximo de usuarios = 4,608 MB ÷ 30 MB = ~153 usuarios
```

### **Prevención de Exceder Límites**
- El sistema verifica automáticamente la capacidad de Storage antes de permitir crear nuevas cuentas
- Si se alcanza el 90% de capacidad, se bloquea el registro de nuevos usuarios
- Los usuarios existentes pueden seguir usando la aplicación normalmente
- Se muestra un mensaje claro cuando no hay capacidad para nuevos usuarios

---

**Última actualización**: Enero 2025  
**Versión del documento**: 2.0

