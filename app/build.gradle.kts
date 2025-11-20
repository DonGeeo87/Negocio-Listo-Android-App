// Este archivo le dice a Android Studio qué librerías necesita nuestra app
// Es como una lista de compras para el supermercado de código 🛒

// Cargar propiedades del keystore desde local.properties (no versionado)
import java.util.Properties
import java.io.FileInputStream

val keystorePropertiesFile = rootProject.file("local.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    FileInputStream(keystorePropertiesFile).use { keystoreProperties.load(it) }
}

plugins {
    // Plugin para aplicaciones Android
    alias(libs.plugins.android.application)
    // Plugin para usar Kotlin (el lenguaje de programación)
    alias(libs.plugins.kotlin.android)
    // Plugin para KSP (procesamiento de anotaciones moderno)
    alias(libs.plugins.ksp)
    // Plugin para Hilt (inyección de dependencias)
    alias(libs.plugins.hilt.android)
    // Plugin para Google Services (Firebase)
    alias(libs.plugins.google.services)
    // Plugin para Crashlytics
    id("com.google.firebase.crashlytics")
    // Plugin para serialización JSON
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.negociolisto.app"
    compileSdk = 34 // Versión de Android que usamos para compilar

    defaultConfig {
        applicationId = "com.negociolisto.app" // ID único de nuestra app
        minSdk = 24     // Android mínimo soportado (Android 7.0)
        targetSdk = 34  // Android objetivo (más reciente)
        versionCode = 3 // Número interno de versión
        versionName = "1.0.2" // Versión que ve el usuario

        // Configuración para pruebas automatizadas
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Configuración para Compose (nuestra librería de UI)
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Configuración de firma para producción
    signingConfigs {
        create("release") {
            // Usar el nuevo keystore
            storeFile = file("../config/keys/release_key_negociolisto_app")
            // Leer credenciales desde local.properties (no versionado)
            storePassword = keystoreProperties["keystore.password"] as String? ?: ""
            keyAlias = keystoreProperties["keystore.key.alias"] as String? ?: "key_negociolisto_app"
            keyPassword = keystoreProperties["keystore.key.password"] as String? ?: ""
        }
    }

    buildTypes {
        // Configuración para desarrollo
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            // applicationIdSuffix = ".debug" // Comentado para evitar problemas con Firebase
            versionNameSuffix = "-debug"
        }
        
        // Configuración para cuando publiquemos la app
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // ✅ Keystore configurado - usar signingConfig de release
            signingConfig = signingConfigs.getByName("release")
        }
    }
    
    // Configuración del compilador de Kotlin
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    // Habilitar Jetpack Compose (nuestra librería de UI moderna)
    buildFeatures {
        compose = true
    }
    
    // Configuración del Compose Compiler
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // 📱 LIBRERÍAS BÁSICAS DE ANDROID
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    // SplashScreen API (Android 12+ con fallback)
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // 🎨 JETPACK COMPOSE (Para crear interfaces bonitas)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    
    // 🎨 MATERIAL DESIGN (Para temas y componentes tradicionales)
    implementation("com.google.android.material:material:1.11.0")
    
    // 🧭 NAVEGACIÓN (Para moverse entre pantallas)
    implementation(libs.androidx.navigation.compose)
    
    // 🏗️ ARQUITECTURA MVVM
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    
    // 💾 BASE DE DATOS LOCAL (Room)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // 🔧 INYECCIÓN DE DEPENDENCIAS (Hilt - para organizar el código)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    
    // 🔐 SEGURIDAD (Para guardar contraseñas de forma segura)
    implementation(libs.androidx.security.crypto)
    
    // 🌐 SERVICIOS EN LA NUBE (Firebase)
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation("com.google.firebase:firebase-messaging-ktx")
    
    // 🔔 NOTIFICACIONES
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Preferencias (DataStore) para escala global de UI
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    
    // 🔐 AUTENTICACIÓN CON GOOGLE (Firebase Auth)
    // NOTA: firebase-auth-ktx ya incluido en Firebase BOM (línea 142)
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // 📅 CALENDARIO PARA COMPOSE
    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0")
    implementation("com.maxkeppeler.sheets-compose-dialogs:calendar:1.0.3")
    
    // 📅 FECHAS Y TIEMPO
    implementation(libs.kotlinx.datetime)
    
    // 🖼️ CARGA DE IMÁGENES
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    
    // 📷 ESCANEO DE CÓDIGOS DE BARRAS
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.2")
    
    // 📦 SERIALIZACIÓN JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // 📊 EXCEL COMPATIBLE (CSV con formato mejorado para Excel)
    // Nota: Usamos CSV con formato especial que Excel abre perfectamente
    
    
    // ☁️ GOOGLE DRIVE API (Para backups automáticos - IMPLEMENTACIÓN REAL)
    // NOTA: play-services-auth ya incluido arriba (línea 151)
    implementation("com.google.apis:google-api-services-drive:v3-rev20220815-2.0.0") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
    }
    implementation("com.google.api-client:google-api-client-android:2.0.0") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
    implementation("com.google.http-client:google-http-client-gson:1.43.3") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
    }
    // Agregar dependencias necesarias para Android HTTP
    implementation("com.google.api-client:google-api-client-android:2.0.0")
    implementation("com.google.http-client:google-http-client-android:1.43.3")
    
    // 🔄 PROGRAMACIÓN ASÍNCRONA (Corrutinas - para no bloquear la app)
    implementation(libs.kotlinx.coroutines.android)
    
    // 🧪 PRUEBAS UNITARIAS (Para verificar que todo funciona)
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    
    // 🧪 PRUEBAS DE INTERFAZ
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    
    // 🛠️ HERRAMIENTAS DE DESARROLLO
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    
    // 🚀 OPTIMIZACIONES DE RENDIMIENTO
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.4")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    implementation("androidx.documentfile:documentfile:1.0.1")
}

// ¿Qué es cada cosa?
// 
// 🎨 Jetpack Compose: Es como tener bloques de LEGO para crear pantallas
// 💾 Room: Una caja fuerte para guardar datos en el teléfono
// 🔧 Hilt: Un organizador automático que conecta las piezas de código
// 🌐 Firebase: Servicios de Google para autenticación y base de datos en la nube
// 🧪 Testing: Herramientas para verificar que todo funciona correctamente

// Configuración de KSP para Room
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Tarea para generar SHA-1 del keystore de release
tasks.register("generateReleaseSha1") {
    doLast {
        val keystorePath = file("../config/keys/release_key_negociolisto_app")
        if (keystorePath.exists()) {
            exec {
                commandLine(
                    "keytool",
                    "-list",
                    "-v",
                    "-keystore",
                    keystorePath.absolutePath,
                    "-alias",
                    keystoreProperties["keystore.key.alias"] as String? ?: "key_negociolisto_app"
                )
            }
        } else {
            println("❌ Keystore no encontrado. Crea uno primero desde Android Studio.")
        }
    }
}