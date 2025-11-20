# 📋 Plan de Unificación de Componentes UI

**Fecha**: Enero 2025  
**Estado**: En progreso

## 📊 Análisis de Uso Actual

### Componentes TopAppBar
- **ModernTopAppBar**: 23 usos (ya es wrapper de Unified) ✅
- **UnifiedTopAppBar**: 13 usos (sistema moderno) ✅
- **NLComponents**: 3 usos (sistema legacy)

**Decisión**: Mantener `UnifiedTopAppBar` como sistema principal. `ModernTopAppBar` ya es wrapper, mantenerlo para compatibilidad.

### Componentes Buttons
- **ModernButton**: 8 usos (setup screens)
- **UnifiedButtons**: 14 usos (más usado)
- **NLComponents**: 2 usos (settings)

**Decisión**: Migrar todo a `UnifiedButtons` (usa DesignTokens moderno).

### Componentes Cards
- **ModernCard**: 13 usos (setup screens)
- **UnifiedCard**: 95 usos (más usado)
- **NLComponents**: 3 usos (settings)

**Decisión**: Migrar todo a `UnifiedCard` (usa DesignTokens moderno).

## 🎯 Estrategia de Migración

### Fase 1: Setup Screens (Modern → Unified)
1. ProfileSetupScreen.kt
2. EmailVerificationScreen.kt

### Fase 2: Settings Screens (NL → Unified)
1. BackupRestoreScreen.kt
2. SettingsScreen.kt (si usa NL)

### Fase 3: Limpieza
1. Eliminar componentes no usados
2. Documentar sistema unificado

## ✅ Progreso

- [x] Análisis de uso completado
- [ ] Migración de setup screens
- [ ] Migración de settings screens
- [ ] Eliminación de componentes legacy

