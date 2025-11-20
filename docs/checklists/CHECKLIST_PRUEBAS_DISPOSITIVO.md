# 📱 Checklist de Pruebas en Dispositivo - NegocioListo v1.0.1

**Fecha:** 17 de Noviembre 2025  
**Desarrollador:** Giorgio Interdonato Palacios  
**GitHub:** @DonGeeo87  
**Dispositivo:** R5CW71X8FVE  
**APK:** app-release.apk (16.4 MB)

---

## ✅ Instalación

- [x] APK instalado exitosamente en dispositivo
- [x] Versión anterior desinstalada (firma incompatible resuelta)
- [x] App aparece en el launcher

---

## 🧪 Pruebas Funcionales Críticas

### **1. Autenticación**
- [ ] Login con email/password funciona
- [ ] Registro de nuevo usuario funciona
- [ ] Login con Google funciona (si está configurado)
- [ ] Recuperación de contraseña funciona
- [ ] Logout funciona correctamente

### **2. Inventario**
- [ ] Agregar producto nuevo funciona
- [ ] Editar producto existente funciona
- [ ] Eliminar producto funciona
- [ ] Búsqueda de productos funciona
- [ ] Filtros por categoría funcionan
- [ ] Imágenes de productos se cargan correctamente
- [ ] Estadísticas de inventario se muestran correctamente

### **3. Ventas**
- [ ] Crear nueva venta funciona
- [ ] Agregar productos a la venta funciona
- [ ] Calcular totales correctamente
- [ ] Guardar venta funciona
- [ ] Ver historial de ventas funciona
- [ ] Detalles de venta se muestran correctamente

### **4. Clientes**
- [ ] Agregar nuevo cliente funciona
- [ ] Editar cliente funciona
- [ ] Eliminar cliente funciona
- [ ] Búsqueda de clientes funciona
- [ ] Ver detalles de cliente funciona
- [ ] Historial de compras del cliente se muestra

### **5. Colecciones**
- [ ] Crear nueva colección funciona
- [ ] Agregar productos a colección funciona
- [ ] Compartir colección genera link público
- [ ] Link público es accesible desde navegador
- [ ] Cliente puede ver colección compartida
- [ ] Cliente puede hacer pedido desde colección

### **6. Facturación**
- [ ] Generar factura funciona
- [ ] PDF de factura se genera correctamente
- [ ] Datos de factura son correctos
- [ ] Compartir factura funciona

### **7. Gastos**
- [ ] Agregar gasto funciona
- [ ] Editar gasto funciona
- [ ] Eliminar gasto funciona
- [ ] Categorías de gastos funcionan
- [ ] Filtros por fecha funcionan

### **8. Dashboard**
- [ ] Dashboard se carga correctamente
- [ ] Estadísticas se muestran correctamente
- [ ] Gráficos se renderizan correctamente
- [ ] Datos son precisos

### **9. Backup y Restauración**
- [ ] Backup a Firebase funciona
- [ ] Restauración desde Firebase funciona
- [ ] Backup local funciona
- [ ] Restauración local funciona

### **10. Configuración**
- [ ] Editar perfil funciona
- [ ] Editar información de empresa funciona
- [ ] Cambiar escala de UI funciona
- [ ] Configuración de backup funciona

---

## 🔄 Pruebas de Sincronización

- [ ] Datos se sincronizan con Firebase correctamente
- [ ] Sincronización funciona después de estar offline
- [ ] Conflictos de sincronización se resuelven correctamente
- [ ] Datos locales se mantienen cuando no hay conexión

---

## 🎨 Pruebas de UI/UX

- [ ] Navegación entre pantallas es fluida
- [ ] Animaciones funcionan correctamente
- [ ] Dark mode funciona correctamente
- [ ] Textos son legibles
- [ ] Botones son accesibles
- [ ] Formularios son fáciles de usar
- [ ] Mensajes de error son claros
- [ ] Loading states se muestran correctamente

---

## ⚡ Pruebas de Rendimiento

- [ ] App inicia en menos de 3 segundos
- [ ] Navegación es rápida y fluida
- [ ] Listas grandes se cargan sin lag
- [ ] Imágenes se cargan eficientemente
- [ ] No hay memory leaks aparentes
- [ ] Uso de batería es razonable

---

## 🔐 Pruebas de Seguridad

- [ ] Datos sensibles no se exponen en logs
- [ ] Permisos se solicitan correctamente
- [ ] Datos se almacenan de forma segura
- [ ] Sesiones se manejan correctamente

---

## 📊 Pruebas de Analytics y Crashlytics

- [ ] Eventos de Analytics se registran (verificar en Firebase Console)
- [ ] Crashlytics captura errores correctamente
- [ ] Performance Monitoring registra métricas

---

## 🐛 Errores Encontrados

### **Críticos (Bloquean funcionalidad)**
- [ ] Ninguno hasta ahora

### **Mayores (Afectan experiencia)**
- [ ] Ninguno hasta ahora

### **Menores (Cosméticos)**
- [ ] Ninguno hasta ahora

---

## 📝 Notas de Pruebas

**Observaciones:**
- APK instalado exitosamente
- Tamaño: 16.4 MB
- Versión: 1.0.1 (versionCode: 2)

**Próximos pasos:**
1. Ejecutar pruebas funcionales críticas
2. Verificar sincronización con Firebase
3. Probar en diferentes escenarios (online/offline)
4. Validar rendimiento y uso de recursos

---

**Última actualización:** 17 de Noviembre 2025  
**Estado:** ✅ APK instalado - Listo para pruebas

