# 🤝 Guía de Contribución - NegocioListo2

**Cómo contribuir al desarrollo de NegocioListo2**

## 🎯 Cómo Contribuir

### **Tipos de Contribuciones**

- 🐛 **Reportar bugs**
- ✨ **Sugerir nuevas características**
- 📝 **Mejorar documentación**
- 🔧 **Corregir código**
- 🎨 **Mejorar UI/UX**
- 🧪 **Agregar tests**

## 🚀 Configuración del Entorno

### **1. Fork del Repositorio**

1. Ve a [NegocioListo2](https://github.com/tu-usuario/negociolisto2)
2. Haz clic en "Fork" en la esquina superior derecha
3. Clona tu fork localmente:

```bash
git clone https://github.com/tu-usuario/negociolisto2.git
cd negociolisto2
```

### **2. Configurar Upstream**

```bash
git remote add upstream https://github.com/original/negociolisto2.git
```

### **3. Instalar Dependencias**

```bash
./gradlew build
```

## 🔄 Flujo de Trabajo

### **1. Crear una Rama**

```bash
git checkout -b feature/nombre-de-la-funcionalidad
# o
git checkout -b fix/descripcion-del-bug
# o
git checkout -b docs/actualizacion-documentacion
```

### **2. Hacer Cambios**

- Escribe código limpio y bien documentado
- Sigue las convenciones del proyecto
- Agrega tests para nuevas funcionalidades
- Actualiza documentación si es necesario

### **3. Commit de Cambios**

```bash
git add .
git commit -m "feat: agregar nueva funcionalidad de exportación"
```

#### **Formato de Commits**

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

### **4. Push y Pull Request**

```bash
git push origin feature/nombre-de-la-funcionalidad
```

Luego crea un Pull Request en GitHub.

## 📋 Estándares de Código

### **Kotlin**

#### **Naming Conventions**
```kotlin
// Clases: PascalCase
class ProductViewModel

// Funciones: camelCase
fun getProducts()

// Variables: camelCase
val productName: String

// Constantes: UPPER_SNAKE_CASE
const val MAX_PRODUCTS = 100

// Enums: PascalCase
enum class ProductCategory
```

#### **Estructura de Archivos**
```kotlin
// 1. Imports
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

// 2. Package declaration
package com.negociolisto.app.ui.products

// 3. Data classes
data class Product(...)

// 4. Interfaces
interface ProductRepository

// 5. Classes
class ProductViewModel

// 6. Composables
@Composable
fun ProductScreen()
```

### **Jetpack Compose**

#### **Estructura de Composables**
```kotlin
@Composable
fun ProductCard(
    product: Product,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onProductClick(product) },
        shape = RoundedCornerShape(16.dp)
    ) {
        // Content
    }
}
```

#### **Parámetros de Composable**
- **Obligatorios primero**: `product: Product`
- **Callbacks después**: `onProductClick: (Product) -> Unit`
- **Modifier al final**: `modifier: Modifier = Modifier`

### **Architecture Components**

#### **ViewModels**
```kotlin
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState = _uiState.asStateFlow()
    
    fun loadProducts() {
        viewModelScope.launch {
            getProductsUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message
                    )
                }
                .collect { products ->
                    _uiState.value = _uiState.value.copy(
                        products = products,
                        isLoading = false
                    )
                }
        }
    }
}
```

## 🧪 Testing

### **Unit Tests**

```kotlin
@Test
fun `when product is added, it should be saved to repository`() = runTest {
    // Given
    val product = Product("1", "Test Product", 10.0, 5, ProductCategory.OTHER)
    val repository = mockk<ProductRepository>()
    coEvery { repository.addProduct(product) } just Runs
    
    // When
    repository.addProduct(product)
    
    // Then
    coVerify { repository.addProduct(product) }
}
```

### **UI Tests**

```kotlin
@Test
fun productList_displaysProducts() {
    composeTestRule.setContent {
        ProductListScreen()
    }
    
    composeTestRule
        .onNodeWithText("Test Product")
        .assertIsDisplayed()
}
```

## 📝 Documentación

### **Comentarios de Código**

```kotlin
/**
 * Pantalla principal de gestión de productos
 * 
 * @param viewModel ViewModel que maneja el estado de la pantalla
 * @param onProductClick Callback cuando se hace clic en un producto
 * @param modifier Modifier para personalizar el layout
 */
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel = hiltViewModel(),
    onProductClick: (Product) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Implementation
}
```

### **README de Funcionalidades**

Cada nueva funcionalidad debe incluir:
- Descripción de la funcionalidad
- Cómo usar la funcionalidad
- Screenshots si aplica
- Tests implementados

## 🐛 Reportar Bugs

### **Template de Bug Report**

```markdown
## 🐛 Descripción del Bug

Descripción clara y concisa del problema.

## 🔄 Pasos para Reproducir

1. Ve a '...'
2. Haz clic en '...'
3. Scroll hasta '...'
4. Ve el error

## 🎯 Comportamiento Esperado

Descripción de lo que debería pasar.

## 📱 Información del Dispositivo

- Dispositivo: [ej. Samsung Galaxy S21]
- Android: [ej. 13]
- Versión de la app: [ej. 1.0.0]

## 📸 Screenshots

Si aplica, agrega screenshots del problema.

## 📋 Información Adicional

Cualquier otra información relevante.
```

## ✨ Sugerir Características

### **Template de Feature Request**

```markdown
## ✨ Descripción de la Característica

Descripción clara y concisa de la característica deseada.

## 🎯 Problema que Resuelve

¿Qué problema resuelve esta característica?

## 💡 Solución Propuesta

Descripción de la solución que propones.

## 🔄 Alternativas Consideradas

Otras soluciones que consideraste.

## 📋 Información Adicional

Cualquier otra información relevante.
```

## 🔍 Code Review

### **Checklist para Reviewers**

- [ ] **Funcionalidad**: ¿El código hace lo que se supone que debe hacer?
- [ ] **Estilo**: ¿Sigue las convenciones del proyecto?
- [ ] **Tests**: ¿Hay tests adecuados?
- [ ] **Documentación**: ¿Está bien documentado?
- [ ] **Performance**: ¿Hay problemas de rendimiento?
- [ ] **Seguridad**: ¿Hay vulnerabilidades de seguridad?
- [ ] **Accesibilidad**: ¿Es accesible para todos los usuarios?

### **Checklist para Contributors**

- [ ] **Tests**: ¿Todos los tests pasan?
- [ ] **Documentación**: ¿Actualicé la documentación?
- [ ] **Commits**: ¿Mis commits son descriptivos?
- [ ] **Código**: ¿Mi código es limpio y legible?
- [ ] **Funcionalidad**: ¿Probé mi código?

## 🏷️ Etiquetas de Issues

- `bug`: Algo no funciona
- `enhancement`: Nueva característica
- `documentation`: Mejoras en documentación
- `good first issue`: Bueno para principiantes
- `help wanted`: Se necesita ayuda extra
- `priority: high`: Alta prioridad
- `priority: medium`: Prioridad media
- `priority: low`: Baja prioridad

## 🎉 Reconocimientos

### **Contribuidores Destacados**

- **@usuario1**: Implementó sistema de facturación
- **@usuario2**: Mejoró UI/UX del inventario
- **@usuario3**: Agregó tests completos

### **Cómo Ser Reconocido**

- Contribuciones consistentes
- Código de alta calidad
- Ayuda a otros contribuidores
- Mejoras significativas

## 📞 Contacto

### **Para Preguntas**

- **GitHub Issues**: Para bugs y features
- **Discussions**: Para preguntas generales
- **Email**: dev@negociolisto.com

### **Para Desarrolladores**

- **Slack**: #negociolisto-dev
- **Discord**: NegocioListo2 Dev Server
- **Twitter**: @NegocioListo2

## 📄 Licencia

Al contribuir, aceptas que tu código será licenciado bajo la [Licencia MIT](LICENSE).

---

**¡Gracias por contribuir a NegocioListo2! Tu ayuda hace posible que esta app sea mejor para todos los emprendedores. 🚀**