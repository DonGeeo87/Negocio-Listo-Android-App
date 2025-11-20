# 🛠️ Guía de Desarrollador - NegocioListo2

**Documentación técnica completa para desarrolladores**

## 📋 Índice

- [Arquitectura del Proyecto](#arquitectura-del-proyecto)
- [Configuración del Entorno](#configuración-del-entorno)
- [Estructura del Código](#estructura-del-código)
- [Patrones de Diseño](#patrones-de-diseño)
- [Base de Datos](#base-de-datos)
- [UI/UX Guidelines](#uiux-guidelines)
- [Testing](#testing)
- [Despliegue](#despliegue)
- [Contribución](#contribución)

## 🏗️ Arquitectura del Proyecto

### **Clean Architecture**

```
app/
├── src/main/java/com/negociolisto/app/
│   ├── data/                    # Data Layer
│   │   ├── local/              # Room Database
│   │   ├── remote/             # Firebase/API
│   │   └── repository/         # Repository Implementations
│   ├── domain/                 # Domain Layer
│   │   ├── model/              # Domain Models
│   │   ├── repository/         # Repository Interfaces
│   │   └── usecase/            # Use Cases
│   ├── ui/                     # Presentation Layer
│   │   ├── components/         # Reusable UI Components
│   │   ├── theme/              # App Theme & Colors
│   │   └── [feature]/          # Feature Modules
│   └── di/                     # Dependency Injection
```

### **Tecnologías Principales**

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Android** | API 24+ | Plataforma base |
| **Kotlin** | 1.9.22 | Lenguaje principal |
| **Jetpack Compose** | 1.5.0 | UI Framework |
| **Room** | 2.6.1 | Base de datos local |
| **Hilt** | 2.51 | Inyección de dependencias |
| **Navigation** | 2.7.0 | Navegación entre pantallas |
| **Coil** | 2.4.0 | Carga de imágenes |
| **Firebase** | 32.0.0+ | Backend services |
| **Firebase Hosting** | - | Mini-web pública |
| **Firebase Cloud Messaging** | - | Notificaciones push |

## ⚙️ Configuración del Entorno

### **Requisitos del Sistema**

- **Android Studio**: Hedgehog 2023.1.1 o superior
- **JDK**: 17 o superior
- **Android SDK**: API 24-34
- **Gradle**: 8.0+
- **Kotlin**: 1.9.0+

### **Configuración Inicial**

1. **Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/negociolisto2.git
cd negociolisto2
```

2. **Configurar Firebase**
```bash
# Descargar google-services.json desde Firebase Console
# Colocar en app/
```

3. **Sincronizar proyecto**
```bash
./gradlew build
```

4. **Ejecutar en dispositivo**
```bash
./gradlew installDebug
```

## 📁 Estructura del Código

### **Data Layer**

#### **Repositorios**
```kotlin
interface ProductRepository {
    suspend fun getProducts(): Flow<List<Product>>
    suspend fun addProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(id: String)
}
```

#### **Entidades Room**
```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val stock: Int,
    val category: String
)
```

### **Domain Layer**

#### **Modelos de Dominio**
```kotlin
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val stock: Int,
    val category: ProductCategory
)
```

#### **Casos de Uso**
```kotlin
class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> = repository.getProducts()
}
```

### **Presentation Layer**

#### **ViewModels**
```kotlin
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {
    val products = getProductsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
```

#### **Composables**
```kotlin
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    
    LazyColumn {
        items(products) { product ->
            ProductCard(product = product)
        }
    }
}
```

## 🎨 Patrones de Diseño

### **MVVM (Model-View-ViewModel)**
- **Model**: Domain models y data sources
- **View**: Jetpack Compose UI
- **ViewModel**: Lógica de presentación y estado

### **Repository Pattern**
- Abstrae la fuente de datos
- Permite cambiar implementaciones fácilmente
- Centraliza la lógica de acceso a datos

### **Use Cases**
- Encapsula lógica de negocio específica
- Reutilizable entre diferentes ViewModels
- Fácil de testear

### **Dependency Injection (Hilt)**
- Inyección automática de dependencias
- Configuración centralizada
- Testing simplificado

## 🗄️ Base de Datos

### **Room Database**

#### **Configuración Principal**
```kotlin
@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        SaleEntity::class,
        ExpenseEntity::class,
        CollectionEntity::class,
        CalendarEventEntity::class,
        InvoiceEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun saleDao(): SaleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun collectionDao(): CollectionDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun invoiceDao(): InvoiceDao
}
```

## 🎨 UI/UX Guidelines

### **Estándar de Diseño Premium**

#### **Colores Corporativos**
```kotlin
val NLPrimary = Color(0xFF009FE3)    // Azul principal
val NLSecondary = Color(0xFF312783)  // Morado secundario
val NLAccent = Color(0xFF00C853)     // Verde de éxito
val NLError = Color(0xFFD32F2F)      // Rojo de error
```

#### **Componentes Reutilizables**

##### **Botones con Gradiente**
```kotlin
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(NLPrimary, NLSecondary)
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
```

## 🧪 Testing

### **Estructura de Tests**

```
app/src/test/java/           # Unit Tests
app/src/androidTest/java/    # Instrumented Tests
```

### **Unit Tests**
```kotlin
@Test
fun `when product is added, it should be saved to repository`() = runTest {
    // Given
    val product = Product("1", "Test Product", 10.0, 5, ProductCategory.OTHER)
    
    // When
    repository.addProduct(product)
    
    // Then
    val products = repository.getProducts().first()
    assertThat(products).contains(product)
}
```

## 🚀 Despliegue

### **Build de Producción**

1. **Configurar signing**
```gradle
android {
    signingConfigs {
        release {
            keyAlias 'negociolisto'
            keyPassword 'password'
            storeFile file('negociolisto.jks')
            storePassword 'password'
        }
    }
}
```

2. **Generar APK**
```bash
./gradlew assembleRelease
```

### **Firebase Hosting - Mini-Web Pública**

La aplicación incluye una mini-web pública para compartir colecciones con clientes.

#### **Estructura de Archivos**
```
public/
├── index.html                  # Landing page
├── collection.html             # Vista pública de colección
├── js/
│   ├── firebase-config.js      # Configuración Firebase (SDK Web)
│   ├── collection-viewer.js    # Visualización de colección
│   └── chat.js                 # Chat en tiempo real
├── css/
│   └── styles.css              # Estilos de la mini-web
└── assets/
    └── logo.png                # Logo de la app
```

#### **Configuración de Firebase Hosting**

1. **Instalar Firebase CLI**
```bash
npm install -g firebase-tools
```

2. **Iniciar sesión**
```bash
firebase login
```

3. **Configurar Firebase Hosting** (ya configurado en `firebase.json`)
```json
{
  "hosting": {
    "public": "public",
    "ignore": [
      "firebase.json",
      "**/.*",
      "**/node_modules/**"
    ],
    "rewrites": [
      {
        "source": "/collection/**",
        "destination": "/collection.html"
      }
    ]
  }
}
```

4. **Desplegar Hosting**
```bash
# Desplegar solo Hosting
firebase deploy --only hosting

# O desplegar todo (Hosting + Firestore Rules + Indexes)
firebase deploy

# Usar script PowerShell (recomendado)
.\scripts\deploy_firebase.ps1
```

#### **Templates Visuales**

La mini-web soporta 5 templates visuales personalizables:
- **MODERN**: Diseño contemporáneo con gradientes
- **CLASSIC**: Diseño tradicional elegante
- **MINIMAL**: Diseño limpio y minimalista
- **DARK**: Tema oscuro con contraste elegante
- **COLORFUL**: Diseño alegre con animaciones

Cada template se aplica automáticamente según el parámetro `template` en la URL:
```
https://TU_PROYECTO.web.app/collection.html?id=COLLECTION_ID&template=MODERN
```

#### **Chat en Tiempo Real**

El chat utiliza Firebase Firestore para sincronización en tiempo real:
```javascript
// Escuchar mensajes en tiempo real
db.collection('collections')
  .doc(collectionId)
  .collection('messages')
  .orderBy('timestamp', 'desc')
  .onSnapshot((snapshot) => {
    // Actualizar mensajes en tiempo real
  });
```

#### **Sistema de Aprobaciones**

El sistema de aprobaciones gestiona estados de pedidos:
- `PENDING_CLIENT_APPROVAL`: Cliente debe aprobar
- `PENDING_BUSINESS_APPROVAL`: Negocio debe aprobar
- `APPROVED`: Ambos han aprobado
- `IN_PRODUCTION`: En producción
- `READY_FOR_DELIVERY`: Listo para entregar
- `DELIVERED`: Entregado

#### **Notificaciones Push (FCM)**

La app Android implementa Firebase Cloud Messaging para notificaciones:
- Token FCM registrado automáticamente
- Canales de notificación personalizados
- Notificaciones cuando hay nuevos mensajes
- Notificaciones de cambios de estado de pedidos

**Más información**: Ver [collections_extended_features.md](../collections_extended_features.md)

## 🤝 Contribución

### **Flujo de Trabajo**

1. **Fork del repositorio**
2. **Crear rama feature**
```bash
git checkout -b feature/nueva-funcionalidad
```

3. **Hacer cambios y commits**
```bash
git add .
git commit -m "feat: agregar nueva funcionalidad"
```

4. **Push y Pull Request**
```bash
git push origin feature/nueva-funcionalidad
```

### **Estándares de Código**

#### **Naming Conventions**
- **Clases**: PascalCase (`ProductViewModel`)
- **Funciones**: camelCase (`getProducts()`)
- **Variables**: camelCase (`productName`)
- **Constantes**: UPPER_SNAKE_CASE (`MAX_PRODUCTS`)

#### **Estructura de Commits**
```
type(scope): description

feat: nueva funcionalidad
fix: corrección de bug
docs: actualización de documentación
style: cambios de formato
refactor: refactorización de código
test: agregar o modificar tests
chore: tareas de mantenimiento
```

---

**¡Gracias por contribuir a NegocioListo2! 🚀**
