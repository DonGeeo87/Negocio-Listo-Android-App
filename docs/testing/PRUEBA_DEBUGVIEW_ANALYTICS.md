# 🔍 Guía: Prueba de Eventos con Firebase DebugView

**Desarrollador:** Giorgio Interdonato Palacios  
**GitHub:** @DonGeeo87  
**Fecha:** Enero 2025

---

## 🎯 Objetivo

Usar Firebase DebugView para ver eventos de Analytics en tiempo real durante el desarrollo.

---

## 📋 Pasos para Habilitar DebugView

### Paso 1: Conectar Dispositivo

```bash
adb devices
```

Debe mostrar tu dispositivo conectado.

### Paso 2: Habilitar Modo Debug de Analytics

```bash
adb shell setprop debug.firebase.analytics.app com.negociolisto.app
```

**Nota:** Reemplaza `com.negociolisto.app` con el `applicationId` de tu app si es diferente.

### Paso 3: Abrir Firebase Console

1. Ir a: https://console.firebase.google.com/project/app-negocio-listo/analytics/debugview
2. O navegar: **Analytics** > **DebugView**

### Paso 4: Usar la App

1. Abrir la app en el dispositivo
2. Realizar acciones que generen eventos:
   - Crear una venta
   - Agregar un producto
   - Agregar un cliente
   - Navegar entre pantallas

### Paso 5: Verificar Eventos

En DebugView deberías ver los eventos apareciendo en tiempo real (sin delay de 1-2 minutos).

---

## 📊 Eventos a Verificar

### Eventos que Deben Aparecer:

1. **`sale_created`**
   - **Acción:** Crear una venta
   - **Parámetros:** `total`, `item_count`

2. **`product_added`**
   - **Acción:** Agregar un producto
   - **Parámetros:** `product_name`, `category`

3. **`product_updated`**
   - **Acción:** Editar un producto
   - **Parámetros:** `product_name`

4. **`product_deleted`**
   - **Acción:** Eliminar un producto
   - **Parámetros:** `product_name`

5. **`customer_added`**
   - **Acción:** Agregar un cliente
   - **Parámetros:** `customer_name`

6. **`screen_view`**
   - **Acción:** Navegar entre pantallas
   - **Parámetros:** `screen_name`

7. **`login`** o **`sign_up`**
   - **Acción:** Iniciar sesión o registrarse
   - **Parámetros:** `method`

---

## 🔍 Verificación con Logs (ADB)

Mientras pruebas, también puedes ver los logs:

```bash
adb logcat | grep "Analytics"
```

Deberías ver:
```
Analytics: 📊 Enviando evento: sale_created con params: {total=1500.0, item_count=3}
Analytics: ✅ Evento sale_created enviado exitosamente
```

---

## ⚠️ Desactivar DebugView (Opcional)

Una vez terminadas las pruebas:

```bash
adb shell setprop debug.firebase.analytics.app .none.
```

---

## 🐛 Troubleshooting

### Problema: No aparecen eventos en DebugView

**Soluciones:**
1. Verificar que el comando `setprop` se ejecutó correctamente
2. Reiniciar la app después de ejecutar `setprop`
3. Verificar que el `applicationId` es correcto
4. Verificar conexión a internet
5. Verificar logs con ADB para ver si hay errores

### Problema: Solo aparecen eventos automáticos

**Causa posible:**
- Los eventos personalizados pueden no estar siendo llamados
- Verificar logs con ADB
- Verificar que el código se está ejecutando

---

## ✅ Checklist de Prueba

- [ ] Dispositivo conectado (`adb devices`)
- [ ] DebugView habilitado (`setprop`)
- [ ] Firebase Console abierto en DebugView
- [ ] App abierta en el dispositivo
- [ ] Venta creada → Verificar `sale_created`
- [ ] Producto agregado → Verificar `product_added`
- [ ] Cliente agregado → Verificar `customer_added`
- [ ] Navegación → Verificar `screen_view`

---

## 📝 Notas Importantes

1. **DebugView vs Tiempo Real:**
   - DebugView muestra eventos inmediatamente (sin delay)
   - La vista "real-time events" puede tener delay de 1-2 minutos

2. **Solo en Desarrollo:**
   - DebugView solo funciona cuando está habilitado con `setprop`
   - No afecta eventos en producción

3. **Eventos Históricos:**
   - Los eventos en DebugView no aparecen en informes históricos
   - Los informes históricos pueden tardar 24-48 horas

---

**Última actualización:** Enero 2025  
**Versión:** 1.0.1

