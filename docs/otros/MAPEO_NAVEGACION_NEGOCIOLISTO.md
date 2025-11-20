# 🗺️ MAPEO DE NAVEGACIÓN - NegocioListo

## 📱 Estructura Principal de Navegación

### 🚀 Flujo de Inicio
```
Splash → Welcome → Login/Register → MainScreen
```

### 🏠 MainScreen (Pantalla Principal)
**Navegación Principal:**
- **Sidebar** (Menú lateral): `ModernSidebar.kt`
- **Top Bar**: `ModernTopAppBar.kt` 
- **Help Bottom Sheet**: `HelpBottomSheet.kt`

---

## 🎯 RUTAS DE NAVEGACIÓN COMPLETAS

### 📊 DASHBOARD (`dashboard`)
**Pantalla:** `DashboardScreen.kt`
**Botones de navegación:**
- `onNavigateToSales` → `sales`
- `onNavigateToExpenses` → `expenses` 
- `onNavigateToInventory` → `inventory`
- `onNavigateToCustomers` → `customers`
- `onGoogleSignIn` → `login`
- `onGoogleSignUp` → `register`

### 📦 INVENTARIO (`inventory`)
**Pantalla:** `InventoryListScreen.kt`
**Botones:**
- `onProductClick` → `product/detail/{productId}`
- `onEditProductClick` → `product/edit/{productId}`
- `onAddProductClick` → `product/add`
- `onBackClick` → `popBackStack()`

**Sub-rutas del Inventario:**
- `product/add` → `AddEditProductScreen.kt`
  - `onDone` → `popBackStack()`
  - `onNavigateToCategoryManagement` → `product/category-management`

- `product/edit/{productId}` → `AddEditProductScreen.kt`
  - `onDone` → `popBackStack()`
  - `onNavigateToCategoryManagement` → `product/category-management`

- `product/detail/{productId}` → `ProductDetailScreen.kt`
  - `onBack` → `popBackStack()`
  - `onEdit` → `product/edit/{productId}`

### 📈 REPORTES/VENTAS (`sales`)
**Pantalla:** `SalesListScreen.kt`
**Botones:**
- `onAddSale` → `sales/record`
- `onGenerateInvoice` → `invoices/create?saleId={saleId}`
- `onBackClick` → `popBackStack()`

**Sub-rutas de Ventas:**
- `sales/record` → `RecordSaleScreen.kt`
  - `onDone` → `popBackStack()`

### 💸 GASTOS (`expenses`)
**Pantalla:** `ExpenseListScreen.kt`
**Botones:**
- `onAddExpense` → `expenses/add`
- `onEditExpense` → `expenses/edit/{expenseId}`
- `onBackClick` → `popBackStack()`

**Sub-rutas de Gastos:**
- `expenses/add` → `AddEditExpenseScreen.kt`
  - `onDone` → `popBackStack()`

- `expenses/edit/{expenseId}` → `AddEditExpenseScreen.kt`
  - `onDone` → `popBackStack()`

### 👥 CLIENTES (`customers`)
**Pantalla:** `CustomerListScreen.kt`
**Botones:**
- `onAddCustomer` → `customers/add`
- `onEditCustomer` → `customers/edit/{customerId}`
- `onBackClick` → `popBackStack()`
- `onImportContacts` → `customers/import`

**Sub-rutas de Clientes:**
- `customers/add` → `AddEditCustomerScreen.kt`
  - `onDone` → `popBackStack()`

- `customers/edit/{customerId}` → `AddEditCustomerScreen.kt`
  - `onDone` → `popBackStack()`

- `customers/import` → `ContactImportScreen.kt`
  - `onNavigateBack` → `popBackStack()`
  - `onImportComplete` → `popBackStack()`

### 📚 COLECCIONES (`collections`)
**Pantalla:** `CollectionListScreen.kt`
**Botones:**
- `onAddCollection` → `collections/add`
- `onEditCollection` → `collections/edit/{collectionId}`
- `onBackClick` → `popBackStack()`

**Sub-rutas de Colecciones:**
- `collections/add` → `AddEditCollectionScreen.kt`
  - `onDone` → `popBackStack()`
  - `onNavigateToProductDetail` → `product/detail/{productId}`

- `collections/edit/{collectionId}` → `AddEditCollectionScreen.kt`
  - `onDone` → `popBackStack()`
  - `onNavigateToProductDetail` → `product/detail/{productId}`

### 📄 FACTURAS (`invoices`)
**Pantalla:** `InvoiceListScreen.kt`
**Botones:**
- `onInvoiceClick` → `invoices/detail/{invoiceId}`
- `onCreateInvoice` → `invoices/create`
- `onSettingsClick` → `invoices/settings`
- `onBackClick` → `popBackStack()`

**Sub-rutas de Facturas:**
- `invoices/create` → `CreateInvoiceScreen.kt`
  - `onBack` → `popBackStack()`
  - `onCreated` → `invoices/detail/{invoiceId}` (con popUpTo)

- `invoices/detail/{invoiceId}` → `InvoiceDetailScreen.kt`
  - `onBack` → `popBackStack()`

- `invoices/settings` → `InvoiceSettingsScreen.kt`
  - `onBack` → `popBackStack()`

### ⚙️ AJUSTES (`settings`)
**Pantalla:** `SettingsScreen.kt`
**Botones:**
- `onBack` → `popBackStack()`
- `onLoggedOut` → Delegar al NavHost raíz
- `onEditProfile` → `settings/edit-profile`
- `onEditCompany` → `settings/edit-company`
- `onBackupManagement` → `settings/backup-management`
- `onDataExport` → `data-export`
- `onCategoryManagement` → `settings/category-management`
- `onShowOnboarding` → Abrir overlay de onboarding

**Sub-rutas de Ajustes:**
- `settings/edit-profile` → `EditProfileScreen.kt`
  - `onBack` → `popBackStack()`
  - `onSave` → `popBackStack()`

- `settings/edit-company` → `EditCompanyScreen.kt`
  - `onBack` → `popBackStack()`
  - `onSave` → `popBackStack()`

- `settings/backup-management` → `BackupRestoreScreen.kt`
  - `onNavigateBack` → `popBackStack()`

- `settings/category-management` → `CategoryManagementScreen.kt`
  - `onBackClick` → `popBackStack()`

- `data-export` → `DataExportScreen.kt`
  - `onBack` → `popBackStack()`

---

## 🎨 SIDEBAR NAVIGATION (ModernSidebar.kt)

### 📋 Secciones del Sidebar:

**🏠 Principal:**
- `dashboard` → Dashboard

**📊 Gestión:**
- `inventory` → Inventario
- `customers` → Clientes  
- `collections` → Colecciones

**💰 Finanzas:**
- `sales` → Reportes
- `expenses` → Gastos
- `invoices` → Facturas

**⚙️ Organización:**
- `settings` → Ajustes

---

## 🔐 RUTAS DE AUTENTICACIÓN

### 🚪 Welcome Screen
- `onLoginClick` → `login`
- `onRegisterClick` → `register`
- `onAlreadyLoggedIn` → `main`

### 🔑 Login Screen
- `onBackClick` → `popBackStack()`
- `onLoginSuccess` → `main` (con popUpTo)

### 📝 Register Screen
- `onBackClick` → `popBackStack()`
- `onRegisterSuccess` → `main` (con popUpTo)

---

## 🎯 RUTAS ESPECIALES

### 📚 Onboarding
- `onboarding` → `OnboardingScreen.kt`
  - `onComplete` → `category_setup`

### 🏷️ Configuración de Categorías
- `category_setup` → `InitialCategorySetupScreen.kt`
  - `onComplete` → `dashboard`
  - `onBack` → `dashboard` (si ya se completó onboarding)

### 🏷️ Gestión de Categorías (desde productos)
- `product/category-management` → `CategoryManagementScreen.kt`
  - `onBackClick` → `popBackStack()`
  - `onCategorySelected` → `popBackStack()`

---

## 🔄 FLUJO DE NAVEGACIÓN PRINCIPAL

```
MainScreen
├── Dashboard (inicio)
├── Sidebar Navigation
│   ├── 📊 Dashboard
│   ├── 📦 Inventario → Productos → Categorías
│   ├── 👥 Clientes → Importar Contactos
│   ├── 📚 Colecciones → Productos
│   ├── 📈 Reportes → Ventas
│   ├── 💸 Gastos
│   ├── 📄 Facturas → Crear/Configurar
│   └── ⚙️ Ajustes → Perfil/Empresa/Backup/Export
├── Top Bar (Menu/Help)
└── Help Bottom Sheet
    ├── Onboarding
    └── Reset Tutorials
```

---

## 📱 NAVEGACIÓN POR PANTALLA

### 🎯 Dashboard → Todas las secciones principales
### 📦 Inventario → Gestión completa de productos
### 👥 Clientes → Lista, agregar, editar, importar
### 📚 Colecciones → Agrupar productos
### 📈 Reportes → Ventas y análisis
### 💸 Gastos → Control de gastos
### 📄 Facturas → Generación y gestión
### ⚙️ Ajustes → Configuración completa

---

**Desarrollador:** Giorgio Interdonato Palacios — GitHub @DonGeeo87
**Fecha:** 2025-01-16
**Versión:** 1.0
