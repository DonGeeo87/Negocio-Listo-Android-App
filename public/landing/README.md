# 🚀 NegocioListo Landing Page

Landing page profesional para la aplicación NegocioListo, enfocada en el usuario final.

## 📋 Estructura

```
landing/
├── index.html          # Página principal
├── styles.css          # Estilos CSS modernos y responsive
├── script.js           # JavaScript para interactividad
├── images/             # Capturas de pantalla de la app
└── README.md           # Este archivo
```

## 🎨 Características

- ✅ Diseño moderno y profesional
- ✅ Totalmente responsive (mobile-first)
- ✅ Animaciones suaves y transiciones fluidas
- ✅ Navegación suave entre secciones
- ✅ Optimizado para SEO
- ✅ Accesible (A11y)
- ✅ Carga rápida y optimizada

## 📱 Secciones

1. **Hero Section** - Presentación principal con CTA
2. **Why NegocioListo** - Razones para elegir la app
3. **Características Principales** - Funcionalidades destacadas con imágenes
4. **Módulos Principales** - Grid de todos los módulos disponibles
5. **Roadmap** - Plan de desarrollo futuro
6. **Download Section** - Call to action para descargar
7. **Footer** - Información del desarrollador y enlaces

## 🖼️ Imágenes Utilizadas

Las siguientes capturas de pantalla están incluidas:

- `onboarding-1.jpg` - Pantalla de onboarding
- `onboarding-2.jpg` - Pantalla de onboarding
- `dashboard.jpg` - Dashboard principal
- `inventario.jpg` - Gestión de inventario
- `ventas.jpg` - Sistema de ventas
- `portal-cliente.jpg` - Portal del cliente
- `facturas.jpg` - Facturas generadas
- `herramientas.jpg` - Herramientas gratuitas

## 🚀 Despliegue

### Desarrollo Local

1. Abre `index.html` en tu navegador
2. O usa un servidor local:
   ```bash
   # Python
   python -m http.server 8000
   
   # Node.js
   npx serve
   ```

### Firebase Hosting

El landing está configurado para servirse desde `/landing` en Firebase Hosting.

**URL de acceso:**
```
https://TU_PROYECTO.web.app/landing
```

**Para desplegar:**

1. Asegúrate de que el contenido esté en `public/landing/`
2. Despliega con Firebase CLI:
   ```bash
   firebase deploy --only hosting
   ```

**Nota:** El contenido de `landing/` se copia automáticamente a `public/landing/` para el despliegue.

## 🎨 Personalización

### Colores

Los colores principales están definidos en `styles.css` como variables CSS:

```css
--primary: #009FE3;
--primary-dark: #312783;
--secondary: #4285F4;
```

### Fuentes

La fuente utilizada es **Inter** de Google Fonts. Puedes cambiarla modificando el import en `index.html`.

## 📝 Notas

- Las imágenes están optimizadas para web
- El diseño sigue los principios de Material Design 3
- Compatible con navegadores modernos (Chrome, Firefox, Safari, Edge)
- Soporte completo para modo oscuro (preparado pero no activado)

## 👨‍💻 Desarrollador

**Giorgio Interdonato Palacios**  
GitHub: [@DonGeeo87](https://github.com/DonGeeo87)

---

**Versión:** 1.0.0  
**Última actualización:** Noviembre 2025

