# 📱 NegocioListo

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.2-blue.svg)
![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-0095D5?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.10-4285F4?style=flat-square&logo=jetpack-compose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-32.7.0-FFCA28?style=flat-square&logo=firebase&logoColor=black)
![License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)
![Status](https://img.shields.io/badge/status-active-success.svg?style=flat-square)

**Una aplicación completa de gestión empresarial para emprendedores**

[Características](#-características-principales) • [Instalación](#-instalación) • [Desarrollo](#️-desarrollo) • [Documentación](#-documentación) • [Soporte](#-soporte)

</div>

---

## 📋 Tabla de Contenidos

- [Descripción](#-descripción)
- [Características Principales](#-características-principales)
- [Stack Tecnológico](#-stack-tecnológico)
- [Arquitectura](#️-arquitectura)
- [Instalación](#-instalación)
- [Desarrollo](#️-desarrollo)
- [Estado del Proyecto](#-estado-del-proyecto)
- [Roadmap](#-roadmap)
- [Contribuciones](#-contribuciones)
- [Documentación](#-documentación)
- [Límites de Uso](#-límites-de-uso)
- [Seguridad](#-seguridad)
- [Rendimiento](#-rendimiento)
- [Licencia](#-licencia)
- [Autor](#-autor)
- [Soporte](#-soporte)

---

## 🎯 Descripción

**NegocioListo** es una aplicación móvil desarrollada en Android que permite a emprendedores y pequeños negocios gestionar de manera integral sus operaciones comerciales. La aplicación ofrece un conjunto completo de herramientas para administrar inventario, ventas, clientes, gastos, facturas y colecciones, todo desde un dispositivo móvil con soporte offline y sincronización en la nube.

## 🌐 Presentación pública

La información de este repositorio se complementa con dos experiencias web públicas que muestran el producto en vivo:

- **Presentación técnica** — `https://app-negocio-listo.web.app/presentacion-app` describe el stack, la arquitectura y las funcionalidades clave, con énfasis en el modo offline-first y el Portal del Cliente como core feature.
- **Landing comercial** — `https://app-negocio-listo.web.app/landing` es la página de marketing que resume los módulos principales (inventario, ventas, clientes, gastos, facturación) y enlaza al Portal del Cliente y la descarga de la app.

### ✨ ¿Por qué NegocioListo?

- 🚀 **Offline-First**: Funciona completamente sin conexión a internet
- 🔄 **Sincronización Automática**: Datos siempre actualizados en la nube
- 📱 **Multi-Dispositivo**: Accede a tus datos desde cualquier dispositivo
- 🎨 **Interfaz Moderna**: Diseño intuitivo con Material Design 3
- 🔒 **Seguro**: Encriptación de datos y autenticación segura
- 🌐 **Portal del Cliente - Core Feature**: Portal web completo y funcional para comunicación profesional directa
- 📊 **Límites Transparentes**: Sistema de límites claro y verificable para mantener el servicio gratuito

### 🚀 Inicio Rápido

**Para Usuarios:**
1. Descarga el APK desde [Releases](https://github.com/DonGeeo87/NegocioListoApp/releases)
2. Instala la aplicación en tu dispositivo Android
3. Crea una cuenta y comienza a gestionar tu negocio

**Para Desarrolladores:**
1. Clona el repositorio: `git clone https://github.com/DonGeeo87/NegocioListoApp.git`
2. Configura Firebase (ver [Instalación](#-instalación))
3. Abre el proyecto en Android Studio
4. Ejecuta la aplicación: `./gradlew installDebug`

---

## ✨ Características Principales

### 📦 Gestión de Inventario
- ✅ Catálogo completo de productos con imágenes optimizadas
- ✅ Control de stock y precios en CLP (Pesos Chilenos)
- ✅ Categorización avanzada de productos
- ✅ Escaneo de códigos de barras integrado
- ✅ Alertas automáticas de stock bajo
- ✅ Búsqueda y filtros avanzados en tiempo real
- ✅ Compresión automática de imágenes
- ✅ Vista de estadísticas de inventario

### 💰 Sistema de Ventas
- ✅ Registro rápido de ventas con carrito
- ✅ Múltiples métodos de pago
- ✅ Generación automática de facturas
- ✅ Historial completo de transacciones
- ✅ Estadísticas de ventas en tiempo real
- ✅ Integración con clientes y productos
- ✅ Búsqueda de productos en tiempo real

### 👥 Gestión de Clientes
- ✅ Base de datos de clientes completa
- ✅ Historial de compras detallado por cliente
- ✅ Información de contacto completa
- ✅ Importación de contactos desde el dispositivo
- ✅ Segmentación de clientes
- ✅ Búsqueda y filtros avanzados
- ✅ Vista de detalle con estadísticas personalizadas

### 💸 Control de Gastos
- ✅ Categorización inteligente de gastos
- ✅ Seguimiento de proveedores
- ✅ Reportes financieros mensuales automáticos
- ✅ Integración con ventas para análisis de ganancias
- ✅ Filtros por fecha y categoría
- ✅ Resúmenes automáticos con visualización clara

### 📄 Sistema de Facturación
- ✅ Múltiples plantillas de factura (3 tipos diferentes)
- ✅ Personalización completa de datos empresariales
- ✅ Exportación en PDF de alta calidad
- ✅ Numeración automática (F001-xxxx)
- ✅ Compartir por email y WhatsApp
- ✅ Vista previa antes de exportar

### 📚 Colecciones de Productos (Extendidas)
- ✅ Agrupación inteligente de productos relacionados
- ✅ Precios especiales por colección
- ✅ **🌐 Portal del Cliente - Core Feature**: Portal web completo y funcional
- ✅ **💬 Chat en Tiempo Real**: Comunicación bidireccional cliente-negocio
- ✅ **🎨 Templates Visuales**: 5 diseños personalizables (MODERN, CLASSIC, MINIMAL, DARK, COLORFUL)
- ✅ **👤 Gestión Automática de Clientes**: Creación desde pedidos web
- ✅ **🔔 Notificaciones Push (FCM)**: Alertas en tiempo real
- ✅ **🔗 Links Públicos**: Compartir por WhatsApp, email o copiar de forma directa.
- ✅ **📊 Seguimiento de Pedidos**: Estados en tiempo real
- ✅ **🚀 Canal de Comunicación Profesional**: Sin intermediarios ni grandes equipos

### 📊 Dashboard y Reportes
- ✅ KPIs en tiempo real en CLP
- ✅ Alertas de stock bajo
- ✅ Top productos más vendidos
- ✅ Top clientes más activos
- ✅ Métricas de ventas y gastos

### 🆓 Herramientas Gratuitas
- ✅ Calculadora de Precios
- ✅ Punto de Equilibrio
- ✅ Recuperación de Inversión
- ✅ Estimador de Stock
---

## 🛠️ Stack Tecnológico

### **Frontend y UI**
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpack-compose&logoColor=white)
![Material Design 3](https://img.shields.io/badge/Material%20Design%203-757575?style=flat-square&logo=material-design&logoColor=white)
![Coil](https://img.shields.io/badge/Coil-2.5.0-FF6B6B?style=flat-square)

- **Jetpack Compose** - UI moderna declarativa
- **Material Design 3** - Sistema de diseño moderno
- **Navigation Component** - Navegación tipo-safe
- **Coil** - Carga eficiente de imágenes
- **Animaciones** - Transiciones suaves y fluidas

### **Backend y Datos**
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black)
![Room](https://img.shields.io/badge/Room-2.6.1-4285F4?style=flat-square&logo=android&logoColor=white)
![DataStore](https://img.shields.io/badge/DataStore-1.1.1-4285F4?style=flat-square)

- **Room Database** - Base de datos local con SQLite
- **Firebase Firestore** - Base de datos en la nube
- **Firebase Storage** - Almacenamiento de imágenes
- **Firebase Authentication** - Autenticación segura
- **Firebase Cloud Messaging** - Notificaciones push
- **Firebase Hosting** - Mini-web pública
- **DataStore** - Preferencias del usuario
- **Security Crypto** - Encriptación de datos sensibles

### **Arquitectura y DI**
![Hilt](https://img.shields.io/badge/Hilt-2.51-4285F4?style=flat-square&logo=android&logoColor=white)
![Clean Architecture](https://img.shields.io/badge/Clean%20Architecture-000000?style=flat-square)
![MVVM](https://img.shields.io/badge/MVVM-000000?style=flat-square)

- **Hilt** - Inyección de dependencias moderna
- **Clean Architecture** - Separación de capas
- **MVVM** - Model-View-ViewModel
- **Repository Pattern** - Abstracción de datos
- **Use Cases** - Lógica de negocio

### **Utilidades**
![Kotlin Coroutines](https://img.shields.io/badge/Kotlin%20Coroutines-1.7.3-0095D5?style=flat-square&logo=kotlin&logoColor=white)
![Kotlinx DateTime](https://img.shields.io/badge/Kotlinx%20DateTime-0.5.0-0095D5?style=flat-square)
![WorkManager](https://img.shields.io/badge/WorkManager-2.9.0-4285F4?style=flat-square)

- **Kotlin Coroutines** - Programación asíncrona
- **Kotlinx DateTime** - Manejo moderno de fechas
- **Flow** - Streams reactivos de datos
- **WorkManager** - Tareas en background

### **Versiones Principales**
```
Kotlin: 1.9.22
Compose BOM: 2024.10.00
Compose Compiler: 1.5.10
Hilt: 2.51
Room: 2.6.1
Firebase BOM: 32.7.0
DataStore: 1.1.1
KSP: 1.9.22-1.0.17
Min SDK: 24 (Android 7.0)
Target SDK: 34 (Android 14)
```

---

## 🏗️ Arquitectura

### **Clean Architecture + MVVM**

La aplicación sigue los principios de Clean Architecture con una separación clara de responsabilidades:

```
┌─────────────────────────────────────┐
│     Presentation Layer              │
│  (Jetpack Compose + ViewModels)     │
├─────────────────────────────────────┤
│       Domain Layer                  │
│  (Use Cases + Business Models)      │
├─────────────────────────────────────┤
│        Data Layer                   │
│  (Repositories + Data Sources)      │
└─────────────────────────────────────┘
```

### **Capas de la Aplicación**

#### **Presentation Layer**
- **UI Components**: Pantallas y componentes reutilizables
- **ViewModels**: Gestión de estado y lógica de presentación
- **Navigation**: Navegación tipo-safe entre pantallas

#### **Domain Layer**
- **Use Cases**: Lógica de negocio pura
- **Models**: Modelos de dominio
- **Interfaces**: Contratos para repositorios

#### **Data Layer**
- **Repositories**: Implementación de lógica de datos
- **Local Data Sources**: Room Database
- **Remote Data Sources**: Firebase Firestore
- **Data Mappers**: Conversión entre modelos

### **Patrones de Diseño Implementados**

- ✅ **MVVM** (Model-View-ViewModel)
- ✅ **Repository Pattern**
- ✅ **Use Cases** (Clean Architecture)
- ✅ **Dependency Injection** (Hilt)
- ✅ **Offline-First** con sincronización automática
- ✅ **Single Source of Truth**

### **Sistema de Diseño Unificado**

- **Componentes Unificados**: `UnifiedButtons`, `UnifiedCards`, `UnifiedTopAppBar`
- **Design Tokens**: Colores, tipografía, espaciado centralizados
- **Gradientes Corporativos**: Paleta basada en la marca (#009FE3, #312783)
- **Modo Oscuro**: Soporte completo con transiciones suaves
- **Animaciones**: Transiciones fluidas de 300ms

---

## 📥 Instalación

### **Requisitos del Sistema**

- Android 7.0 (API 24) o superior
- 150 MB de espacio libre
- Conexión a internet (para sincronización inicial)
- Cámara (opcional, para escaneo de códigos de barras)

### **Instalación desde APK**

1. Descarga el archivo `app-debug.apk` desde [Releases](https://github.com/DonGeeo87/NegocioListoApp/releases)
2. Habilita "Fuentes desconocidas" en tu dispositivo Android
3. Instala el APK
4. Abre la aplicación y crea tu cuenta

### **Instalación desde Código Fuente**

#### **Prerrequisitos**

- Android Studio Hedgehog 2023.1.1 o superior
- JDK 17 o superior
- Gradle 8.4 o superior
- Git

#### **Pasos de Instalación**

```bash
# 1. Clonar el repositorio
git clone https://github.com/DonGeeo87/NegocioListoApp.git
cd NegocioListoApp

# 2. Configurar Firebase
# - Crear proyecto en Firebase Console
# - Descargar google-services.json
# - Colocarlo en app/google-services.json

# 3. Configurar local.properties (si es necesario)
# sdk.dir=C:\\Users\\TuUsuario\\AppData\\Local\\Android\\Sdk

# 4. Compilar y ejecutar
./gradlew assembleDebug
./gradlew installDebug
```

#### **Configuración de Firebase**

1. Crear proyecto en [Firebase Console](https://console.firebase.google.com/)
2. Agregar app Android con package `com.negociolisto.app`
3. Descargar `google-services.json`
4. Colocar en `app/google-services.json`
5. Habilitar los siguientes servicios:
   - Authentication (Email/Password, Google Sign-In)
   - Firestore Database
   - Storage
   - Cloud Messaging (FCM)
   - Hosting

---

## 💻 Desarrollo

### **Requisitos de Desarrollo**

- **Android Studio**: Hedgehog 2023.1.1+
- **Kotlin**: 1.9.22
- **Compose BOM**: 2024.10.00
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Gradle**: 8.4
- **AGP**: 8.2.2

### **Configuración del Proyecto**

El proyecto está configurado con:
- ✅ Hilt para inyección de dependencias
- ✅ KSP para procesamiento de anotaciones
- ✅ Room con migraciones
- ✅ Compose Compiler configurado
- ✅ ProGuard para release builds

### **Comandos Útiles**

```bash
# Build debug
./gradlew assembleDebug

# Instalar en dispositivo
./gradlew installDebug

# Ejecutar tests
./gradlew test

# Limpiar proyecto
./gradlew clean

# Build sin tests (más rápido)
./gradlew assembleDebug -x test

# PowerShell (Windows) - Build con script
./clean-and-build.ps1
```

### **Estructura del Proyecto**

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/negociolisto/app/
│   │   │   ├── data/           # Capa de datos
│   │   │   ├── domain/         # Capa de dominio
│   │   │   ├── presentation/   # Capa de presentación
│   │   │   ├── di/             # Inyección de dependencias
│   │   │   └── ui/             # Componentes UI
│   │   └── res/                # Recursos (imágenes, strings, etc.)
│   ├── test/                   # Tests unitarios
│   └── androidTest/            # Tests instrumentados
└── build.gradle.kts            # Configuración del módulo
```

### **Scripts de Desarrollo**

- **`clean-and-build.ps1`**: Script PowerShell para limpiar y compilar
- **`scripts/deploy_firebase.ps1`**: Desplegar reglas y hosting de Firebase
- **`scripts/deploy_functions.ps1`**: Desplegar Cloud Functions

---

## 📊 Estado del Proyecto

### **Versión Actual**

- **Versión**: 1.0.2
- **Version Code**: 3
- **Estado**: ✅ Funcional - Portal del Cliente como Core Feature
- **Última Actualización**: 17 de Noviembre de 2025

### **Changelog v1.0.2** 🚀

#### **🌟 Mejoras Principales - Portal del Cliente como Core Feature**
- ✅ **Portal del Cliente completamente funcional** - El portal web ahora es el corazón de NegocioListo
- ✅ **Portal integral del cliente** con gestión completa de colecciones, pedidos y chat
- ✅ **5 Templates Visuales personalizables** (MODERN, CLASSIC, MINIMAL, DARK, COLORFUL)
- ✅ **Chat en tiempo real bidireccional** con historial completo y mensajes del sistema
- ✅ **Sistema de pedidos web completo** con seguimiento de estados en tiempo real
- ✅ **Gestión automática de clientes** desde pedidos web sin intervención manual
- ✅ **Canal de comunicación profesional** directo sin depender de grandes equipos
- ✅ **Ahorro de tiempo y dinero** al eliminar intermediarios y automatizar procesos
- ✅ **Experiencia web personalizada** para cada negocio sin necesidad de desarrollo complejo

#### **Mejoras y Correcciones**
- ✅ Optimizaciones en rendimiento del portal web
- ✅ Mejoras en sincronización de datos entre app y portal
- ✅ Correcciones en sistema de notificaciones push
- ✅ Mejoras en UI de colecciones y pedidos
- ✅ Actualización de componentes del dashboard
- ✅ Optimizaciones en carga de imágenes del portal

### **Changelog v1.0.1**

#### **Mejoras y Correcciones**
- ✅ Mejoras en repositorios de datos (Firebase y Room)
- ✅ Optimizaciones en sincronización de datos
- ✅ Correcciones en gestión de clientes y ventas
- ✅ Mejoras en UI de colecciones y pedidos
- ✅ Actualización de componentes de inventario
- ✅ Mejoras en dashboard y métricas
- ✅ Optimizaciones en importación de contactos
- ✅ Correcciones en flujo de onboarding y setup inicial
- ✅ **Sistema de límites de uso implementado** (100 productos, 50 clientes, 50 colecciones)
- ✅ **Verificación de capacidad de Storage antes de crear cuentas** (90% de 5 GB)
- ✅ **Pantalla de Límites de Uso** en Configuración para monitorear consumo
- ✅ **Optimización de compresión de imágenes** (30 MB por usuario)

### **Funcionalidades Completadas**

#### **🏗️ Arquitectura y Fundación**
- [x] Clean Architecture + MVVM implementada completamente
- [x] Inyección de Dependencias con Hilt (sin ciclos)
- [x] Base de Datos Room con migraciones
- [x] Sistema de Errores robusto y centralizado
- [x] Firebase Integration completa (Auth, Firestore, Storage, FCM, Hosting)
- [x] Offline-First implementado

#### **🎨 UI/UX Moderna**
- [x] Material Design 3 completo
- [x] Sistema de diseño unificado (Unified Components)
- [x] Animaciones suaves y transiciones fluidas
- [x] Dark Mode completo
- [x] Responsive Design adaptable
- [x] Estados vacíos modernos e informativos

#### **📱 Módulos Principales**
- [x] **Inventario** - CRUD completo con imágenes optimizadas
- [x] **Ventas** - Sistema completo de registro y facturación
- [x] **Clientes** - Base de datos completa con importación
- [x] **Gastos** - Control financiero con reportes
- [x] **Colecciones** - Agrupación de productos con funcionalidades extendidas
- [x] **🌐 Portal del Cliente** - Portal web completo como core feature con chat, pedidos y templates
- [x] **Facturas** - Generación y exportación PDF
- [x] **Dashboard** - Métricas y KPIs en tiempo real
- [x] **Configuración** - Perfil, empresa, backup, exportación

#### **⚡ Optimizaciones**
- [x] Eliminación de dependencias duplicadas
- [x] Optimización de imports
- [x] Compresión automática de imágenes
- [x] Lazy loading en listas
- [x] Cache inteligente de imágenes
- [x] Build paralelo configurado

---

## 🗺️ Roadmap

### **Próximas Mejoras (Corto Plazo)**
- [ ] Agregar más tests unitarios para ViewModels
- [ ] Optimizar queries de Room para mejor rendimiento
- [x] Mensajes del sistema automáticos en chat ✅ **Implementado en v1.0.2**
- [ ] Analytics y métricas de uso de colecciones
- [ ] Mejorar documentación de API

### **Mejoras Futuras (Mediano Plazo)**
- [ ] Integración de pagos (MercadoPago, Getnet, Flow)
- [ ] Subida de imágenes de referencia por cliente
- [ ] Google Maps para ubicación de entrega
- [ ] Sugerencias automáticas de combos o upsells
- [ ] Exportación de reportes en múltiples formatos
- [ ] Integración con sistemas de punto de venta

### **Visión a Largo Plazo**
- [ ] Soporte para múltiples idiomas
- [ ] Versión web completa (PWA)
- [ ] Integración con APIs de contabilidad
- [ ] Sistema de reportes avanzados con gráficos
- [ ] IA para sugerencias de productos
- [ ] **🤖 IA para Reportes Semanales Inteligentes**: Generación automática de reportes semanales con análisis de cómo ha estado el negocio, estrategias recomendadas y áreas de atención para mejorar la gestión del negocio

---

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'feat: Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

### **Guías de Contribución**

- Seguir [Conventional Commits](https://www.conventionalcommits.org/)
- Mantener código limpio y documentado
- Agregar tests para nuevas funcionalidades
- Revisar [CONTRIBUTING.md](CONTRIBUTING.md) para más detalles

### **Estándares de Código**

- Usar ktlint para formateo
- Seguir las convenciones de Kotlin
- Escribir código autodocumentado
- Agregar comentarios cuando sea necesario

---

## 📚 Documentación

### **Documentación Disponible**

- [Guía de Usuario](docs/guias/USER_GUIDE.md) - Manual completo de uso
- [Guía de Desarrollo](docs/guias/DEVELOPER.md) - Información técnica detallada
- [Guía de Contribución](CONTRIBUTING.md) - Cómo contribuir al proyecto
- [Guía de Backup](docs/guias/BACKUP_ACCESS_GUIDE.md) - Configuración de backups
- [Límites de Uso](docs/firebase/LIMITES_USO_FIREBASE.md) - Límites y capacidades del sistema
- [Colecciones Extendidas](docs/otros/collections_extended_features.md) - Funcionalidades de colecciones
- [Guía de Testing](docs/guias/GUIA_TESTING_COLECCIONES.md) - Testing de colecciones

### **Documentación Técnica**

- [Arquitectura del Proyecto](docs/modules/app.md)
- [Auditoría del Proyecto](docs/auditoria/)
- [Optimizaciones Implementadas](docs/mejoras/OPTIMIZACIONES_IMPLEMENTADAS.md)

---

## 🔒 Límites de Uso

NegocioListo tiene límites establecidos para garantizar un rendimiento óptimo y una experiencia fluida para todos los usuarios. Estos límites están diseñados para la mayoría de pequeños negocios y emprendimientos, y estamos trabajando continuamente en mejoras para futuras versiones.

### **📊 Límites por Usuario**

- **Productos**: 100 productos por usuario
- **Clientes**: 50 clientes por usuario
- **Colecciones**: 50 colecciones por usuario
- **Almacenamiento**: ~30 MB por usuario (optimizado)

### **📈 Sistema de Advertencias**

El sistema implementa tres niveles de advertencia:
- **Normal** (< 80%): Uso normal, sin restricciones
- **Advertencia** (80% - 95%): Mensaje informativo, puedes seguir agregando
- **Crítico** (≥ 95%): Alerta importante, se recomienda optimizar

### **💡 Buen Uso del Espacio**

Para aprovechar al máximo tu espacio disponible:
- Organiza tu inventario eliminando productos no utilizados
- Mantén tu base de clientes actualizada
- Optimiza tus colecciones agrupando productos relacionados
- Gestiona tus imágenes eficientemente
- Revisa regularmente tu uso en la pantalla de Límites de Uso

### **🚀 Mejoras Futuras**

Estamos trabajando continuamente en mejorar la aplicación. En futuras versiones esperamos aumentar los límites disponibles y optimizar aún más el uso de almacenamiento.

### **📖 Más Información**

Para más detalles sobre límites y cómo verificar tu uso, consulta:
- [Guía de Usuario - Límites de Uso](USER_GUIDE.md#-límites-de-uso)
- [Documentación Técnica de Límites](LIMITES_USO_FIREBASE.md)

---

## 🧭 Despliegue manual de la landing

1. Desde la raíz del proyecto, copia los archivos actualizados de `landing/` a `public/landing/`:
   ```powershell
   cd C:/Users/DonGeeo87/AndroidStudioProjects/NegocioListo2
   Copy-Item landing\* public\landing -Recurse -Force
   ```
2. Verifica que la CLI de Firebase esté disponible:
   ```powershell
   firebase --version
   ```
   Si no aparece un número de versión, instala `firebase-tools` (`npm install -g firebase-tools`).
3. Ejecuta el deploy:
   ```powershell
   firebase deploy --only hosting
   ```
4. Revisa la URL que muestra Firebase (ej.: `https://app-negocio-listo.web.app`) y comprueba que la landing pública refleja los enlaces actualizados al repositorio.

---

## 🔒 Seguridad

### **Implementaciones de Seguridad**

- ✅ Autenticación segura con Firebase
- ✅ Encriptación de datos sensibles (Security Crypto)
- ✅ Validación robusta de entrada de datos
- ✅ Backup automático en la nube con Firebase
- ✅ Sesiones seguras con encriptación
- ✅ Almacenamiento seguro de credenciales
- ✅ Manejo seguro de imágenes y archivos
- ✅ Verificación de capacidad de Storage antes de crear cuentas

### **Buenas Prácticas**

- No exponer secretos en el código
- Usar variables de entorno para configuración sensible
- Validar todas las entradas del usuario
- Implementar rate limiting donde sea necesario
- Mantener dependencias actualizadas
- Monitorear uso de recursos para prevenir exceder límites

---

## ⚡ Rendimiento

### **Optimizaciones Implementadas**

- ⚡ **Tiempo de Build**: 50% más rápido (4min → 2min)
- 📦 **Tamaño de APK**: Reducción de ~1MB
- 🚀 **Carga Inicial**: 20-30% más rápido
- 💾 **Uso de Memoria**: 10-15% más eficiente
- 🎯 **Respuesta de UI**: 20-25% más rápida
- 📱 **Navegación**: 15-20% más fluida

### **Métricas de Rendimiento**

- Carga de pantallas: < 2 segundos
- Navegación fluida entre secciones
- Optimización de memoria con lazy loading
- Sincronización eficiente en background
- Cache inteligente de imágenes

---

## 🌐 Portal del Cliente - Core Feature de NegocioListo

### **¿Qué es el Portal del Cliente?**

El **Portal del Cliente** es el **corazón y la funcionalidad principal** de NegocioListo. No es solo una mini-web, sino un **portal completo y funcional** que transforma cómo los negocios se comunican con sus clientes. 

Es un **canal de comunicación profesional directo** que permite a los clientes acceder a colecciones compartidas, realizar pedidos, chatear con el negocio y dar seguimiento completo a sus pedidos, todo sin necesidad de instalar ninguna aplicación. Toda la información está integrada para que las colecciones se luzcan y entreguen un canal directo de comunicación profesional a través de una web personalizada **sin depender de grandes equipos y ahorrando tiempo y dinero**.

### **¿Por qué el Portal del Cliente es el Core de NegocioListo?**

- 🎯 **Canal de Comunicación Profesional**: Comunicación directa cliente-negocio sin intermediarios
- ⚡ **Ahorro de Tiempo y Dinero**: Automatización completa que elimina la necesidad de grandes equipos
- 🚀 **Sin Dependencias Externas**: Web personalizada lista para usar sin desarrollo complejo
- 💼 **Experiencia Integrada**: Toda la información del negocio integrada en un solo portal
- 🌐 **Accesibilidad Universal**: Funciona en cualquier dispositivo sin instalación

### **Características del Portal del Cliente**

- 🎨 **Templates Visuales**: 5 diseños personalizables (MODERN, CLASSIC, MINIMAL, DARK, COLORFUL)
- 💬 **Chat en Tiempo Real**: Comunicación bidireccional cliente-negocio con historial completo
- 📦 **Sistema de Pedidos Completo**: Gestión completa de pedidos con seguimiento de estados en tiempo real
- 👤 **Gestión Automática de Clientes**: Creación automática desde pedidos web sin intervención manual
- 🔔 **Notificaciones Push (FCM)**: Alertas en tiempo real para cliente y negocio
- 📧 **Email Automático**: Confirmación automática de pedidos
- 🔗 **Links Públicos**: Compartir por WhatsApp, email o SMS con un solo clic
- 📊 **Seguimiento de Pedidos**: Estados en tiempo real sincronizados entre app y portal
- 🖼️ **Gestión de Imágenes**: Carga optimizada de imágenes desde Firebase Storage
- 💾 **Persistencia Local**: Guardado automático del formulario de pedido

### **URL de Acceso**

```
https://TU_PROYECTO.web.app/collection.html?id=COLLECTION_ID&template=TEMPLATE_NAME
```

### **Beneficios Clave**

- ✅ **Comunicación Profesional**: Canal directo con tus clientes sin depender de plataformas externas
- ✅ **Automatización Completa**: Desde pedido hasta seguimiento, todo automatizado
- ✅ **Experiencia Personalizada**: Cada negocio tiene su propio portal único
- ✅ **Ahorro de Recursos**: Sin necesidad de equipos grandes ni desarrollo complejo
- ✅ **Escalabilidad**: Crece con tu negocio sin limitaciones técnicas

### **Más Información**

- [Guía Completa de Colecciones Extendidas](docs/otros/collections_extended_features.md)
- [Guía de Testing de Colecciones](docs/guias/GUIA_TESTING_COLECCIONES.md)
- [Documentación del Portal del Cliente](public/README.md)

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver [LICENSE](LICENSE) para más detalles.

---

## 👨‍💻 Autor

**Desarrollado con ❤️ por un Emprendedor para Emprendedores**

- **Desarrollador**: Giorgio Interdonato Palacios
- **GitHub**: [@DonGeeo87](https://github.com/DonGeeo87)

---

## 📞 Soporte

Para soporte técnico o consultas:

- **GitHub Issues**: [Crear Issue](https://github.com/DonGeeo87/NegocioListoApp/issues)
- **Documentación**: [Wiki del Proyecto](https://github.com/DonGeeo87/NegocioListoApp/wiki)

---

## 🎉 Agradecimientos

Gracias a toda la comunidad de desarrolladores Android que hacen posible proyectos como este.

---

<div align="center">

**¡Gracias por usar NegocioListo! 🚀**

*Una aplicación desarrollada con pasión para ayudar a los emprendedores a gestionar sus negocios de manera eficiente y moderna.*

---

**Última actualización**: 17 de Noviembre de 2025  
**Versión**: 1.0.2  
**Estado**: ✅ Funcional - Portal del Cliente como Core Feature

</div>
