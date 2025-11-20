# 🚀 Instrucciones de Deploy - NegocioListo Presentation

## ⚡ Opción Rápida: Vercel (Recomendado)

### Pasos:

1. **Abre una terminal en la carpeta `presentation`**

2. **Ejecuta**:
```bash
vercel login
```
   - Te abrirá el navegador para autenticarte
   - Acepta y vuelve a la terminal

3. **Deploy**:
```bash
vercel --prod
```

4. **¡Listo!** Te dará una URL como: `https://negociolisto-presentation.vercel.app`

---

## 🌐 Opción Alternativa: Netlify Drop (Sin instalación)

1. Ve a: https://app.netlify.com/drop
2. Arrastra la carpeta `out` (después de hacer `npm run build`)
3. ¡Listo! Obtienes una URL instantánea

---

## 📦 Para hacer build local (si quieres probar primero):

```bash
cd presentation
npm run build
```

Los archivos estarán en la carpeta `out/`

---

## ⚠️ Si tienes problemas de permisos:

1. Cierra el servidor de desarrollo (`Ctrl+C` si está corriendo)
2. Cierra Android Studio
3. Intenta el build de nuevo

