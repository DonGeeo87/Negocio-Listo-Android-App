# 🔍 Análisis de Valores Hardcodeados en el Código

**Fecha**: Enero 2025  
**Estado**: ⚠️ Valores Hardcodeados Encontrados

---

## 🚨 Valores Hardcodeados Críticos

### 1. **BackupService.kt** - Línea 862
**Problema**: UID hardcodeado como fallback al parsear categorías
```kotlin
userId = categoryObj.optString("userId", "user_123"),
```
**Ubicación**: `app/src/main/java/com/negociolisto/app/data/service/BackupService.kt:862`
**Impacto**: ⚠️ MEDIO - Si falta userId en el JSON, usa un ID falso
**Solución**: Usar el userId del usuario autenticado actual o lanzar error

### 2. **BackupService.kt** - Línea 879
**Problema**: UID hardcodeado como fallback al parsear usuario
```kotlin
id = userObj.optString("id", "user_123"),
```
**Ubicación**: `app/src/main/java/com/negociolisto/app/data/service/BackupService.kt:879`
**Impacto**: ⚠️ MEDIO - Si falta id en el JSON, usa un ID falso
**Solución**: Usar el userId del usuario autenticado actual o lanzar error

### 3. **AuthRepositoryImpl.kt** - Línea 160
**Problema**: ID de usuario mock hardcodeado en login
```kotlin
id = "user_mock",
```
**Ubicación**: `app/src/main/java/com/negociolisto/app/data/repository/AuthRepositoryImpl.kt:160`
**Impacto**: ⚠️ BAJO - Solo se usa en modo mock/demo (AuthRepositoryImpl es para desarrollo)
**Solución**: Este es aceptable si AuthRepositoryImpl es solo para desarrollo. Si se usa en producción, debe generar un ID único o usar Firebase Auth.

---

## ⚠️ Valores Hardcodeados Aceptables (Configuración)

### 1. **UserPreferences** - Moneda por defecto
```kotlin
val preferredCurrency: String = "COP", // Pesos colombianos por defecto
```
**Ubicación**: `app/src/main/java/com/negociolisto/app/domain/model/User.kt:382`
**Estado**: ✅ ACEPTABLE - Es un valor por defecto configurable por el usuario

### 2. **UserPreferences** - Idioma por defecto
```kotlin
val preferredLanguage: String = "es", // Español por defecto
```
**Ubicación**: `app/src/main/java/com/negociolisto/app/domain/model/User.kt:387`
**Estado**: ✅ ACEPTABLE - Es un valor por defecto configurable por el usuario

### 3. **AuthRepositoryImpl** - Generación de ID temporal
```kotlin
id = prefs.getString("user_id", "user_${System.currentTimeMillis()}") ?: "user_${System.currentTimeMillis()}",
```
**Ubicación**: `app/src/main/java/com/negociolisto/app/data/repository/AuthRepositoryImpl.kt:61`
**Estado**: ✅ ACEPTABLE - Genera un ID único basado en timestamp si no hay uno guardado

### 4. **BackupService** - Strings de configuración
```kotlin
metadata["userEmail"] = jsonObject.optString("userEmail", "unknown")
```
**Ubicación**: `app/src/main/java/com/negociolisto/app/data/service/BackupService.kt:1073`
**Estado**: ✅ ACEPTABLE - Valor por defecto para metadata faltante

---

## 📋 Resumen de Acciones Recomendadas

### Alta Prioridad
1. **BackupService.kt línea 862**: Reemplazar `"user_123"` con userId del usuario autenticado
2. **BackupService.kt línea 879**: Reemplazar `"user_123"` con userId del usuario autenticado o lanzar error si falta

### Baja Prioridad
3. **AuthRepositoryImpl.kt línea 160**: Revisar si `"user_mock"` es aceptable para desarrollo o debe generarse dinámicamente

---

## 🔧 Código de Ejemplo para Corrección

### Antes (❌)
```kotlin
userId = categoryObj.optString("userId", "user_123"),
```

### Después (✅)
```kotlin
val currentUserId = authRepository.currentUser.first()?.id
    ?: throw IllegalStateException("Usuario no autenticado")
userId = categoryObj.optString("userId", currentUserId),
```

---

**Desarrollador**: Giorgio Interdonato Palacios  
**GitHub**: @DonGeeo87  
**Fecha**: Enero 2025

