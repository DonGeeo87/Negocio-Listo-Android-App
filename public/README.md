# Documentación del Portal del Cliente

El Portal del Cliente es la experiencia web pública que complementa a la aplicación móvil NegocioListo. Esta mini-plataforma permite que los clientes visualicen colecciones, creen pedidos, chateen con el negocio y den seguimiento a sus transacciones sin necesidad de instalar nada.

## Recursos principales

- `public/customer-portal.html`: página principal que carga colecciones y chat integrados.
- `public/landing/` y `landing/`: versiones estáticas del landing page que describen el portal para clientes y enlaces públicos.
- `public/collection.html`: plantilla con parámetros `id` y `template` para mostrar colecciones compartidas.
- `public/index.html` / `public/landing/index.html`: puntos de entrada usados por Firebase Hosting y para previews del portal.

## Personalización

1. Ajusta los templates visuales (MODERN, CLASSIC, MINIMAL, DARK, COLORFUL) editando los estilos y scripts dentro de `public/`.
2. Cambia los mensajes automáticos y textos del chat desde `scripts/` y `app/src/main/java/...` según necesites.
3. Para probar localmente, abre `public/customer-portal.html` en el navegador y pasa los parámetros query requeridos.

## Despliegue

El portal se publica junto con los assets de Firebase Hosting en `public/`. Usa los scripts de `scripts/deploy_firebase.ps1` o los comandos dentro de `presentation/DEPLOY_FIREBASE.md` para subir los cambios.

## Referencias internas

- El portal se alimenta de colecciones definidas en `app/src/main/java/com/negociolisto/app/ui/collections`.
- El backend que responde a pedidos proviene de Firebase Functions (`functions/index.js`).

> Este documento sirve para guiar a cualquier desarrollador que necesite entender cómo funciona el Portal del Cliente antes de editar los assets que viven dentro de `public/`.

