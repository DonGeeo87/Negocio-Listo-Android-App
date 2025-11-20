# 📋 Tareas Pendientes - NegocioListo2

**Fecha**: Enero 2025  
**Desarrollador**: Giorgio Interdonato Palacios - GitHub @DonGeeo87

---

## ✅ Completado Recientemente

### UI Unification
- ✅ Migración de componentes Modern/NL → Unified (6 pantallas)
- ✅ Eliminación de componentes legacy no usados (4 archivos, ~780 líneas)
- ✅ Migración de Empty States a Unified
- ✅ 5 builds verificados exitosamente

### Correcciones de Mapeos
- ✅ StockMovementEntity timestamp corregido

---

## 🔴 Alta Prioridad

### 1. Servicios Deprecados

#### **ExportService.kt** ⚠️ DEPRECADO
- **Estado**: Marcado como `@deprecated`, pero aún en uso
- **Usado en**:
  - `SettingsViewModel.kt`
  - `DataExportViewModel.kt`
- **Reemplazo**: `DataExportService.kt` (API basada en Flow)
- **Acción**: Migrar ViewModels a usar `DataExportService`
- **Dificultad**: Media (requiere refactor de API)

#### **GoogleSignInService.kt** ⚠️ DEPRECADO
- **Estado**: Marcado como `@deprecated`
- **Usado en**: `AuthViewModel.kt`, `ServiceModule.kt`
- **Reemplazo**: `GoogleAuthService.kt` (verificar si existe)
- **Acción**: Verificar reemplazo y migrar

#### **SimpleBackupService.kt** ⚠️ DEPRECADO
- **Estado**: Marcado como `@deprecated`
- **Reemplazo**: `BackupService.kt` con Firebase
- **Acción**: Verificar si se usa y eliminar o migrar

### 2. Código Legacy Restante

#### **EmptyStates.kt**
- **Estado**: Marcado como `@deprecated`
- **Usado en**: `SalesScreens.kt` (algunos componentes)
- **Acción**: Migrar componentes restantes a `UnifiedStates.kt`

#### **Buttons.kt**
- **Estado**: Marcado como `@deprecated`
- **Usado**: Verificar si `PrimaryButton`/`SecondaryButton` se usan realmente
- **Acción**: Si no se usa, eliminar

### 3. Supabase - Eliminación Completa

#### **SupabaseClient.kt**
- **Estado**: Stub sin funcionalidad real
- **Acción**: Eliminar completamente si no se usará
- **Verificar**: Dependencias en `build.gradle.kts` y módulos DI

---

## 🟡 Prioridad Media

### 4. Consolidación de Librerías

#### **Calendarios**
- Identificar todas las librerías de calendario/fecha
- Consolidar en una sola (probablemente `kotlinx-datetime`)
- Eliminar duplicados

### 5. Contenedores Hilt

#### **ToastViewModel**
- Mover fuera de `@Singleton` scope
- Usar scope apropiado (Activity o ViewModel)

### 6. Limpieza de Imports

#### **Imports No Usados**
- Ejecutar análisis estático
- Eliminar imports innecesarios
- Optimizar dependencias

---

## 🟢 Prioridad Baja (Mejoras Futuras)

### 7. Testing

#### **Tests Unitarios**
- ViewModels críticos
- Servicios de exportación
- Repositorios

#### **Tests de Integración**
- Flujos completos (login → venta → factura)
- Sincronización Firebase

### 8. Optimizaciones de Rendimiento

#### **Carga de Imágenes**
- Optimizar compresión
- Lazy loading mejorado

#### **Queries de Room**
- Optimizar índices
- Mejorar queries complejas

### 9. Documentación

#### **Código**
- Comentarios en funciones complejas
- Documentación de APIs públicas

#### **Arquitectura**
- Diagramas actualizados
- Guías de contribución

---

## 📊 Resumen de Prioridades

| Prioridad | Tarea | Estado | Esfuerzo |
|-----------|-------|--------|----------|
| 🔴 Alta | Migrar ExportService → DataExportService | Pendiente | Medio |
| 🔴 Alta | Eliminar Supabase completamente | Pendiente | Bajo |
| 🔴 Alta | Migrar EmptyStates restantes | Pendiente | Bajo |
| 🟡 Media | Consolidar librerías de calendario | Pendiente | Medio |
| 🟡 Media | Mover ToastViewModel de Singleton | Pendiente | Bajo |
| 🟡 Media | Limpieza de imports no usados | Pendiente | Bajo |
| 🟢 Baja | Tests unitarios | Pendiente | Alto |
| 🟢 Baja | Optimizaciones de rendimiento | Pendiente | Medio |

---

## 🎯 Recomendación de Orden

1. **Primero**: Eliminar Supabase (rápido, bajo riesgo)
2. **Segundo**: Migrar EmptyStates restantes (rápido, bajo riesgo)
3. **Tercero**: Limpieza de imports (rápido, bajo riesgo)
4. **Cuarto**: Migrar ExportService (requiere más cuidado)
5. **Quinto**: Revisar y migrar GoogleSignInService
6. **Sexto**: Consolidar librerías de calendario
7. **Séptimo**: Mover ToastViewModel

---

**Desarrollador**: Giorgio Interdonato Palacios  
**GitHub**: @DonGeeo87  
**Fecha**: Enero 2025

