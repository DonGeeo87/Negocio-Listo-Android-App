# 📱 Guía de Monitoreo con Logcat

Esta guía explica cómo usar el sistema de logging y monitoreo de la app NegocioListo.

## 🎯 Componentes

### 1. AppLogger - Clase de Logging Centralizada

Ubicación: `app/src/main/java/com/negociolisto/app/utils/AppLogger.kt`

Proporciona logging consistente tanto en logcat como en Firebase Crashlytics.

#### Uso Básico

```kotlin
import com.negociolisto.app.utils.AppLogger

// Log de información general
AppLogger.i("MainActivity", "Usuario inició sesión")

// Log de debug (detallado)
AppLogger.d("Repository", "Cargando datos del servidor")

// Log de advertencia
AppLogger.w("ViewModel", "Datos en caché expirados")

// Log de error con excepción
AppLogger.e("Repository", "Error al cargar datos", exception)

// Log de eventos con parámetros
AppLogger.logEvent("Analytics", "purchase_completed", mapOf(
    "amount" to "100.00",
    "currency" to "USD"
))
```

#### Uso con Extension Functions (Recomendado)

```kotlin
class MyViewModel : ViewModel() {
    init {
        logi("ViewModel inicializado")
    }
    
    fun loadData() {
        logd("Cargando datos...")
        try {
            // código
            logi("Datos cargados exitosamente")
        } catch (e: Exception) {
            loge("Error al cargar datos", e)
        }
    }
}
```

### 2. Script de Monitoreo PowerShell

Ubicación: `monitor-logcat.ps1`

Script para monitorear logcat en tiempo real desde la terminal.

#### Uso Básico

```powershell
# Monitorear todos los logs de la app
.\monitor-logcat.ps1

# Solo errores
.\monitor-logcat.ps1 -Filter "ERROR"

# Solo logs de Firebase
.\monitor-logcat.ps1 -Filter "Firebase"

# Guardar logs en archivo
.\monitor-logcat.ps1 -SaveToFile

# No limpiar logs anteriores
.\monitor-logcat.ps1 -ClearLogs:$false
```

#### Requisitos

- Android SDK instalado (ADB debe estar en `%LOCALAPPDATA%\Android\Sdk\platform-tools\`)
- Dispositivo Android conectado o emulador ejecutándose
- PowerShell 5.1 o superior

#### Características

- ✅ Coloreado automático según nivel de log (ERROR=Rojo, WARN=Amarillo, INFO=Cyan, DEBUG=Gris)
- ✅ Filtrado por tags específicos
- ✅ Guardado opcional en archivo
- ✅ Verificación automática de dispositivos conectados
- ✅ Limpieza automática de logs anteriores

## 📊 Niveles de Log

| Nivel | Uso | Ejemplo |
|-------|-----|---------|
| **VERBOSE (V)** | Información muy detallada | `AppLogger.v("Tag", "Detalles internos")` |
| **DEBUG (D)** | Información para desarrollo | `AppLogger.d("Tag", "Estado del proceso")` |
| **INFO (I)** | Información general del flujo | `AppLogger.i("Tag", "Usuario inició sesión")` |
| **WARNING (W)** | Situaciones que requieren atención | `AppLogger.w("Tag", "Caché expirado")` |
| **ERROR (E)** | Errores que deben investigarse | `AppLogger.e("Tag", "Error crítico", exception)` |

## 🔍 Filtros Comunes

### Por Tag de la App

```powershell
# Solo logs de la app NegocioListo
.\monitor-logcat.ps1 -Filter "NegocioListo"
```

### Por Componente

```powershell
# Solo ViewModels
.\monitor-logcat.ps1 -Filter "ViewModel"

# Solo Repositories
.\monitor-logcat.ps1 -Filter "Repository"

# Solo Firebase
.\monitor-logcat.ps1 -Filter "Firebase"
```

### Por Nivel

```powershell
# Solo errores (ya incluido por defecto)
.\monitor-logcat.ps1
```

## 📝 Mejores Prácticas

### 1. Usar Tags Descriptivos

```kotlin
// ✅ Bueno
AppLogger.i("AuthRepository", "Usuario autenticado")

// ❌ Malo
AppLogger.i("Repo", "OK")
```

### 2. Incluir Contexto en los Mensajes

```kotlin
// ✅ Bueno
AppLogger.i("OrderRepository", "Pedido #${orderId} creado exitosamente")

// ❌ Malo
AppLogger.i("OrderRepository", "Creado")
```

### 3. Registrar Excepciones con Contexto

```kotlin
// ✅ Bueno
try {
    // código
} catch (e: Exception) {
    AppLogger.e("OrderRepository", "Error al crear pedido #${orderId}", e)
    throw e
}
```

### 4. No Loggear Información Sensible

```kotlin
// ❌ Malo - No loggear contraseñas o tokens
AppLogger.d("Auth", "Password: $password")

// ✅ Bueno
AppLogger.d("Auth", "Usuario intentando iniciar sesión: $email")
```

## 🐛 Debugging

### Ver Logs en Android Studio

1. Abre la pestaña **Logcat** en la parte inferior
2. Filtra por tag: `NegocioListo`
3. Selecciona el nivel de log deseado

### Ver Logs desde Terminal

```powershell
# Monitoreo en tiempo real
.\monitor-logcat.ps1

# Ver logs guardados
Get-Content logs\logcat-*.log | Select-String "ERROR"
```

### Limpiar Logs del Dispositivo

```powershell
# Desde el script (automático)
.\monitor-logcat.ps1 -ClearLogs

# Manualmente
adb logcat -c
```

## 🔗 Integración con Firebase Crashlytics

Todos los logs de nivel **ERROR** y **WARNING** se envían automáticamente a Firebase Crashlytics para análisis en producción.

## 📚 Referencias

- [Android Log Documentation](https://developer.android.com/reference/android/util/Log)
- [Firebase Crashlytics](https://firebase.google.com/docs/crashlytics)
- [ADB Logcat Guide](https://developer.android.com/studio/command-line/logcat)

---

**Desarrollador:** Giorgio Interdonato Palacios  
**GitHub:** @DonGeeo87

