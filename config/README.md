# ⚙️ Configuración del Proyecto

**Desarrollador:** Giorgio Interdonato Palacios  
**GitHub:** @DonGeeo87

---

## 📁 Estructura

```
config/
├── firebase/          # Configuración de Firebase
│   ├── firestore.rules
│   ├── firestore.indexes.json
│   └── storage.rules
├── keys/              # Archivos sensibles (NO versionar)
│   ├── google-services.json
│   ├── release_key_negociolisto_app
│   └── client_secret_*.json
└── add_descriptions.json
```

---

## 🔥 Firebase

### Archivos de Configuración

Los archivos de Firebase están en `config/firebase/`:

- **`firestore.rules`**: Reglas de seguridad de Firestore
- **`firestore.indexes.json`**: Índices de Firestore
- **`storage.rules`**: Reglas de seguridad de Storage

**Nota:** El archivo `firebase.json` en la raíz del proyecto referencia estos archivos con rutas relativas.

### Despliegue

Para desplegar las reglas de Firebase:

```bash
firebase deploy --only firestore:rules
firebase deploy --only storage:rules
```

---

## 🔐 Keys y Credenciales

### ⚠️ IMPORTANTE: Archivos Sensibles

La carpeta `config/keys/` contiene archivos **sensibles** que **NO deben versionarse**:

- `google-services.json` - Configuración de Firebase (se copia a `app/`)
- `release_key_negociolisto_app` - Keystore para firmar APKs de producción
- `client_secret_*.json` - Credenciales de OAuth

### Configuración de google-services.json

El archivo `google-services.json` debe estar en dos lugares:

1. **`config/keys/google-services.json`** - Archivo original (no versionado)
2. **`app/google-services.json`** - Copia para el build de Android (se genera automáticamente)

**Para actualizar:**

```bash
# Copiar desde config/keys/ a app/
cp config/keys/google-services.json app/google-services.json
```

### Configuración del Keystore

El keystore de release está en `config/keys/release_key_negociolisto_app`.

Las credenciales se configuran en `local.properties` (no versionado):

```properties
keystore.password=tu_password
keystore.key.alias=negociolisto-release
keystore.key.password=tu_password
```

---

## 📝 Otros Archivos

- **`add_descriptions.json`**: Configuración adicional del proyecto

---

## 🔒 Seguridad

Todos los archivos en `config/keys/` están en `.gitignore` y **NO deben subirse al repositorio**.

Si necesitas compartir configuración:
- Usa variables de entorno
- Usa archivos de ejemplo (`.example`)
- Documenta el proceso de configuración

---

**Última actualización:** Noviembre 2025

