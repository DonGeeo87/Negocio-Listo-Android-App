# 🎨 NegocioListo - Presentación Web Interactiva

Presentación web interactiva y moderna para mostrar los aspectos técnicos y funcionales de la aplicación NegocioListo.

## 🚀 Características

- ✨ **Diseño Moderno**: Interfaz limpia y profesional con animaciones suaves
- 📱 **Responsive**: Adaptable a todos los dispositivos
- 🎯 **Interactivo**: Secciones desplegables y navegación fluida
- 💻 **Código Destacado**: Ejemplos de código con syntax highlighting
- 🏗️ **Arquitectura Visual**: Diagramas interactivos de la arquitectura
- 🎨 **Tema Personalizado**: Colores corporativos de NegocioListo

## 📋 Secciones

1. **Hero**: Introducción impactante con características principales
2. **Stack Tecnológico**: Librerías y tecnologías utilizadas con detalles
3. **Arquitectura**: Clean Architecture + MVVM explicada visualmente
4. **Features**: Características principales con descripciones detalladas
5. **Funcionamiento**: Flujo de datos y ejemplos de código

## 🛠️ Instalación

### Prerrequisitos

- Node.js 18+ 
- npm o yarn

### Pasos

1. **Instalar dependencias**:
```bash
cd presentation
npm install
```

2. **Ejecutar en desarrollo**:
```bash
npm run dev
```

3. **Abrir en el navegador**:
```
http://localhost:3000
```

## 📦 Build para Producción

```bash
# Generar build optimizado
npm run build

# Ejecutar servidor de producción
npm start
```

## 🌐 Despliegue

### Vercel (Recomendado)

1. Instalar Vercel CLI:
```bash
npm i -g vercel
```

2. Desplegar:
```bash
vercel
```

### Netlify

1. Build command: `npm run build`
2. Publish directory: `.next`

### Firebase Hosting

1. Build: `npm run build`
2. Deploy: `firebase deploy --only hosting`

## 🎨 Personalización

### Colores

Los colores corporativos están definidos en `tailwind.config.js`:

```javascript
colors: {
  primary: {
    DEFAULT: '#009FE3',
    dark: '#0077A3',
    light: '#33B5E8',
  },
  secondary: {
    DEFAULT: '#312783',
    dark: '#1F1653',
    light: '#4A3BA3',
  },
}
```

### Contenido

- **Hero**: `components/sections/Hero.tsx`
- **Stack Tecnológico**: `components/sections/TechStack.tsx`
- **Arquitectura**: `components/sections/Architecture.tsx`
- **Features**: `components/sections/Features.tsx`
- **Funcionamiento**: `components/sections/HowItWorks.tsx`

## 📚 Tecnologías Utilizadas

- **Next.js 14**: Framework React con App Router
- **TypeScript**: Tipado estático
- **Tailwind CSS**: Estilos utility-first
- **Framer Motion**: Animaciones fluidas
- **Lucide React**: Iconos modernos
- **React Syntax Highlighter**: Resaltado de código

## 🎯 Uso para Presentación

1. **Preparación**:
   - Revisar y actualizar el contenido según necesidades
   - Ajustar colores si es necesario
   - Verificar que todas las secciones estén completas

2. **Durante la Presentación**:
   - Usar navegación superior para saltar entre secciones
   - Hacer clic en cards para expandir información
   - Mostrar código destacado en la sección "Funcionamiento"

3. **Tips**:
   - Usar modo pantalla completa (F11)
   - Navegar con el teclado usando las flechas
   - Las animaciones se activan al hacer scroll

## 📝 Estructura del Proyecto

```
presentation/
├── app/
│   ├── layout.tsx          # Layout principal
│   ├── page.tsx            # Página principal
│   └── globals.css         # Estilos globales
├── components/
│   ├── Navigation.tsx       # Barra de navegación
│   └── sections/
│       ├── Hero.tsx        # Sección hero
│       ├── TechStack.tsx   # Stack tecnológico
│       ├── Architecture.tsx # Arquitectura
│       ├── Features.tsx    # Features
│       └── HowItWorks.tsx  # Funcionamiento
├── package.json
├── tailwind.config.js
└── tsconfig.json
```

## 🔧 Scripts Disponibles

- `npm run dev`: Servidor de desarrollo
- `npm run build`: Build de producción
- `npm start`: Servidor de producción
- `npm run lint`: Linter de código

## 📄 Licencia

MIT - Desarrollado por Giorgio Interdonato Palacios - GitHub @DonGeeo87

---

**¡Disfruta presentando NegocioListo! 🚀**

