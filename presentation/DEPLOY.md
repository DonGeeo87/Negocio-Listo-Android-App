# 🚀 Guía de Deploy - NegocioListo Presentation

## Opción 1: Vercel (Recomendado - Más Fácil)

### Pasos:

1. **Instalar Vercel CLI** (si no lo tienes):
```bash
npm i -g vercel
```

2. **Desde la carpeta presentation, ejecutar**:
```bash
cd presentation
vercel
```

3. **Seguir las instrucciones**:
   - ¿Set up and deploy? → **Y**
   - ¿Which scope? → Tu cuenta
   - ¿Link to existing project? → **N**
   - ¿What's your project's name? → **negociolisto-presentation**
   - ¿In which directory is your code located? → **./**
   - ¿Override settings? → **N**

4. **¡Listo!** Vercel te dará una URL como: `https://negociolisto-presentation.vercel.app`

---

## Opción 2: Vercel desde GitHub (Automático)

1. Sube el código a GitHub
2. Ve a [vercel.com](https://vercel.com)
3. Importa el repositorio
4. Configura:
   - Framework Preset: **Next.js**
   - Root Directory: **presentation**
5. Deploy automático

---

## Opción 3: Netlify

1. **Instalar Netlify CLI**:
```bash
npm i -g netlify-cli
```

2. **Build y deploy**:
```bash
cd presentation
npm run build
netlify deploy --prod --dir=out
```

---

## Opción 4: Firebase Hosting (Requiere build local)

1. **Hacer build**:
```bash
cd presentation
npm run build
```

2. **Copiar archivos a public**:
```bash
# Copiar el contenido de 'out' a la carpeta 'public' del proyecto raíz
```

3. **Deploy**:
```bash
firebase deploy --only hosting
```

---

## Solución al Error de Permisos

Si tienes el error `EPERM`, cierra:
- El servidor de desarrollo (`npm run dev`)
- Android Studio si está abierto
- Cualquier proceso que use archivos en `.next`

Luego intenta el build de nuevo.

