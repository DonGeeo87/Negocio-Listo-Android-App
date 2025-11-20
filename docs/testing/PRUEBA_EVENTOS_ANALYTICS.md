# 🧪 Guía de Prueba: Eventos de Analytics en Tiempo Real

**Desarrollador:** Giorgio Interdonato Palacios  
**GitHub:** @DonGeeo87  
**Fecha:** Enero 2025

---

## 🎯 Objetivo

Verificar que los eventos personalizados de Firebase Analytics se están enviando correctamente y aparecen en tiempo real en Firebase Console.

---

## 📋 Preparación

### Paso 1: Compilar la App

```bash
.\gradlew assembleDebug
```

### Paso 2: Instalar en Dispositivo

```bash
.\gradlew installDebug
```

O instalar manualmente el APK:
```
app\build\outputs\apk\debug\app-debug.apk
```

---

## 🔍 Método 1: Verificación en Firebase Console (Tiempo Real)

### Pasos:

1. **Abrir Firebase Console:**
   - Ir a: https://console.firebase.google.com/project/app-negocio-listo/analytics/events

2. **Activar Vista en Tiempo Real:**
   - Click en el botón **"View real-time events"** o **"Ver eventos en tiempo real"**
   - Debe mostrar eventos que ocurren en los últimos 30 minutos

3. **Abrir la App en el Dispositivo:**
   - Asegurarse de estar conectado a internet
   - Iniciar sesión en la app

4. **Realizar Acciones y Verificar:**

   **a) Crear una Venta:**
   - Ir a la pantalla de Ventas
   - Crear una nueva venta con productos
   - **Esperar 1-2 minutos**
   - Verificar en Firebase Console que aparece `sale_created`

   **b) Agregar un Producto:**
   - Ir a Inventario
   - Agregar un nuevo producto
   - **Esperar 1-2 minutos**
   - Verificar que aparece `product_added`

   **c) Agregar un Cliente:**
   - Ir a Clientes
   - Agregar un nuevo cliente
   - **Esperar 1-2 minutos**
   - Verificar que aparece `customer_added`

   **d) Navegar entre Pantallas:**
   - Navegar entre diferentes pantallas
   - **Esperar 1-2 minutos**
   - Verificar que aparecen múltiples eventos `screen_view`

---

## 🔍 Método 2: Verificación con Logs (ADB)

### Pasos:

1. **Conectar Dispositivo:**
   ```bash
   adb devices
   ```
   Debe mostrar el dispositivo conectado.

2. **Ver Logs de Analytics:**
   ```bash
   adb logcat | grep "Analytics"
   ```

3. **Realizar Acciones en la App:**
   - Crear una venta
   - Agregar un producto
   - Agregar un cliente

4. **Verificar Logs:**
   Deberías ver logs como:
   ```
   Analytics: 📊 Enviando evento: sale_created con params: {total=1500.0, item_count=3}
   Analytics: ✅ Evento sale_created enviado exitosamente
   ```

---

## 📊 Eventos a Probar

### ✅ Eventos que Deben Aparecer:

1. **`sale_created`**
   - **Acción:** Crear una venta desde la pantalla de Ventas
   - **Parámetros esperados:** `total`, `item_count`
   - **Tiempo de aparición:** 1-2 minutos

2. **`product_added`**
   - **Acción:** Agregar un producto nuevo en Inventario
   - **Parámetros esperados:** `product_name`, `category`
   - **Tiempo de aparición:** 1-2 minutos

3. **`product_updated`**
   - **Acción:** Editar un producto existente
   - **Parámetros esperados:** `product_name`
   - **Tiempo de aparición:** 1-2 minutos

4. **`product_deleted`**
   - **Acción:** Eliminar un producto
   - **Parámetros esperados:** `product_name`
   - **Tiempo de aparición:** 1-2 minutos

5. **`customer_added`**
   - **Acción:** Agregar un cliente nuevo
   - **Parámetros esperados:** `customer_name`
   - **Tiempo de aparición:** 1-2 minutos

6. **`screen_view`**
   - **Acción:** Navegar entre pantallas
   - **Parámetros esperados:** `screen_name`
   - **Tiempo de aparición:** Inmediato (ya aparece)

7. **`login`** o **`sign_up`**
   - **Acción:** Iniciar sesión o registrarse
   - **Parámetros esperados:** `method` ("email" o "google")
   - **Tiempo de aparición:** 1-2 minutos

---

## 🐛 Troubleshooting

### Problema: No aparecen eventos en tiempo real

**Soluciones:**
1. Verificar conexión a internet
2. Verificar que Analytics está habilitado en `NegocioListoApplication.kt`
3. Verificar logs con ADB para ver si hay errores
4. Esperar 2-3 minutos (puede haber delay)
5. Probar con APK release en lugar de debug

### Problema: Aparecen errores en logs

**Verificar:**
```bash
adb logcat | grep -i "error\|exception" | grep -i "analytics"
```

Si hay errores, revisar:
- `google-services.json` está presente
- `applicationId` coincide con Firebase Console
- Permisos de internet en `AndroidManifest.xml`

### Problema: Solo aparecen eventos automáticos

**Causa posible:**
- Los eventos personalizados pueden tardar más en aparecer
- Verificar que el código se está ejecutando (logs con ADB)
- Probar con APK release

---

## ✅ Checklist de Prueba

- [ ] App compilada e instalada
- [ ] Dispositivo conectado a internet
- [ ] Firebase Console abierto en "View real-time events"
- [ ] Logs de ADB configurados (opcional)
- [ ] Venta creada → Verificar `sale_created`
- [ ] Producto agregado → Verificar `product_added`
- [ ] Cliente agregado → Verificar `customer_added`
- [ ] Navegación → Verificar `screen_view`
- [ ] Login/Registro → Verificar `login` o `sign_up`

---

## 📝 Notas Importantes

1. **Tiempo de Delay:**
   - Los eventos en tiempo real pueden tardar 1-2 minutos en aparecer
   - Los eventos históricos pueden tardar 24-48 horas

2. **Modo Debug vs Release:**
   - Algunos eventos pueden comportarse diferente en debug
   - Probar con APK release para comportamiento completo

3. **Límites de Firebase:**
   - Máximo 500 eventos personalizados por proyecto
   - Máximo 25 parámetros por evento
   - Nombres de eventos: máximo 40 caracteres

4. **Eventos Automáticos:**
   - `screen_view`, `user_engagement`, `session_start` aparecen automáticamente
   - No requieren código adicional

---

## 🎯 Resultado Esperado

Después de realizar las acciones, deberías ver en Firebase Console (tiempo real):

```
Eventos en los últimos 30 minutos:
- screen_view: 15 eventos
- sale_created: 1 evento
- product_added: 1 evento
- customer_added: 1 evento
- user_engagement: 12 eventos
- session_start: 1 evento
```

Y en los logs de ADB:
```
Analytics: 📊 Enviando evento: sale_created con params: {total=1500.0, item_count=3}
Analytics: ✅ Evento sale_created enviado exitosamente
```

---

**Última actualización:** Enero 2025  
**Versión:** 1.0.1

