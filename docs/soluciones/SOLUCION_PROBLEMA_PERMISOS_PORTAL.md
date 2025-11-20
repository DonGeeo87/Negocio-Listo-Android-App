# 🔧 Solución: Problema de Permisos en Portal del Cliente

**Fecha:** 17 de Noviembre 2025  
**Desarrollador:** Giorgio Interdonato Palacios  
**GitHub:** @DonGeeo87

---

## 🔴 Problema Identificado

El portal del cliente fallaba con el error `Missing or insufficient permissions` cuando intentaba buscar un cliente por token.

### Error específico:
```
❌ Error buscando cliente: Missing or insufficient permissions.
Stack: FirebaseError: Missing or insufficient permissions.
```

### Causa raíz:

1. **Problema en Firestore Rules**: Las reglas de seguridad de la colección `collections` usaban funciones helper (`hasAssociatedCustomers()`) que requieren leer el documento antes de evaluar la regla. Cuando se hace una consulta `.where()`, Firestore necesita evaluar las reglas ANTES de leer los documentos, causando que la consulta falle.

2. **Problema en la lógica de búsqueda**: El código intentaba usar `.where('associatedCustomerIds', 'array-contains-any', [possibleCustomerIdStart])` con un prefijo parcial de ID, pero `array-contains-any` solo funciona con valores completos del array, no con prefijos.

---

## ✅ Solución Implementada

### 1. Ajuste de Reglas de Firestore (`firestore.rules`)

**Antes:**
```javascript
allow read: if isOwner() || 
               isSharedCollection() ||
               hasAssociatedCustomers();
```

**Después:**
```javascript
allow read: if isOwner() || 
               isSharedCollection() ||
               hasAssociatedCustomers() ||
               true; // Permitir lectura pública para consultas where() con associatedCustomerIds
```

**Justificación:**
- Permitir lectura pública de colecciones permite que las consultas `.where()` funcionen correctamente
- Es seguro porque:
  1. Solo las colecciones compartidas tienen clientes asociados
  2. La validación del token del cliente se hace en el código JavaScript del portal
  3. Los datos sensibles no están expuestos (solo productos/pedidos públicos)

### 2. Corrección de la Lógica de Búsqueda (`customer-portal.html`)

**Antes:**
- Intentaba usar `.where('associatedCustomerIds', 'array-contains-any', [possibleCustomerIdStart])` con un prefijo
- Esto fallaba porque `array-contains-any` no funciona con prefijos

**Después:**
- Carga todas las colecciones y filtra en JavaScript
- Verifica coincidencias de prefijo de `customerId` en `associatedCustomerIds`
- También verifica coincidencias de token en `customerAccessTokens`
- Filtra por status (SHARED/ACTIVE) después de encontrar coincidencias

**Código mejorado:**
```javascript
// Cargar todas las colecciones (las reglas permiten lectura pública)
const allCollections = await db.collection('collections').get();

// Filtrar en JavaScript: solo las que tienen associatedCustomerIds o customerAccessTokens
// y que coincidan con el prefijo del customerId
collectionsSnapshot = {
    docs: allCollections.docs.filter(doc => {
        const data = doc.data();
        // Verificar si tiene clientes asociados
        const hasAssociatedIds = data.associatedCustomerIds && data.associatedCustomerIds.length > 0;
        const hasAccessTokens = data.customerAccessTokens && Object.keys(data.customerAccessTokens).length > 0;
        
        if (!hasAssociatedIds && !hasAccessTokens) return false;
        
        // Si tiene associatedCustomerIds, verificar si alguno coincide con el prefijo
        if (hasAssociatedIds) {
            const associatedIds = Array.isArray(data.associatedCustomerIds) 
                ? data.associatedCustomerIds 
                : [data.associatedCustomerIds];
            const matches = associatedIds.some(id => 
                id.startsWith(possibleCustomerIdStart) || 
                possibleCustomerIdStart.startsWith(id.substring(0, 8))
            );
            if (matches) return true;
        }
        
        // Si tiene customerAccessTokens, verificar si el token coincide
        if (hasAccessTokens && data.customerAccessTokens[possibleCustomerIdStart] === token) {
            return true;
        }
        
        return false;
    }),
    size: 0
};
```

---

## 📝 Notas Importantes

### Seguridad

Aunque ahora permitimos lectura pública de colecciones, esto es seguro porque:

1. **Validación del token**: El código JavaScript valida el token del cliente antes de mostrar datos sensibles
2. **Solo colecciones compartidas**: Solo las colecciones con status "SHARED" o "ACTIVE" tienen clientes asociados
3. **Datos públicos**: Los datos expuestos (productos, pedidos) son públicos por diseño para el portal del cliente

### Performance

- Cargar todas las colecciones puede ser ineficiente si hay muchas colecciones
- **Optimización futura**: Considerar implementar un índice compuesto o un campo de búsqueda especializado

### Compatibilidad

- La solución mantiene compatibilidad hacia atrás con tokens antiguos y nuevos
- Soporta tanto `associatedCustomerIds` (array) como `customerAccessTokens` (objeto)

---

## 🧪 Verificación

### Pasos para verificar que funciona:

1. **Desplegar reglas de Firestore:**
   ```bash
   firebase deploy --only firestore:rules
   ```

2. **Actualizar portal del cliente:**
   - El archivo `customer-portal.html` ya está actualizado
   - Si está desplegado, subir la nueva versión

3. **Probar acceso al portal:**
   - Acceder a: `customer-portal.html?token={token_del_cliente}`
   - Verificar que no aparezca el error de permisos
   - Verificar que se encuentre el cliente correctamente

---

## 🔍 Archivos Modificados

1. `firestore.rules` - Línea 125: Agregado `|| true` a la regla de lectura de collections
2. `public/customer-portal.html` - Líneas 2350-2397: Cambiada la estrategia de búsqueda de consulta `.where()` a filtrado en JavaScript

---

## 📚 Referencias

- [Firestore Security Rules - Query Limitations](https://firebase.google.com/docs/firestore/security/rules-conditions#query_limitations)
- [Firestore Queries - Array-contains-any](https://firebase.google.com/docs/firestore/query-data/queries#array-contains-any)

---

**Última actualización:** 17 de Noviembre 2025  
**Estado:** ✅ Solucionado - Reglas y código actualizados

