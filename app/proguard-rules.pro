# ==========================================
# REGLAS DE PROGUARD PARA NEGOCIOLISTO
# ==========================================

# Preservar información de debugging para stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ==========================================
# KOTLIN Y JETPACK COMPOSE
# ==========================================

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }

# ==========================================
# ROOM DATABASE
# ==========================================

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep class * extends androidx.room.migration.Migration

# Room entities
-keep class com.negociolisto.app.data.local.entity.** { *; }

# ==========================================
# HILT DEPENDENCY INJECTION
# ==========================================

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# ==========================================
# FIREBASE
# ==========================================

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Auth específico
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.auth.FirebaseAuth { *; }
-keep class com.google.firebase.auth.FirebaseUser { *; }

# Firebase Firestore específico
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.Timestamp { *; }

# Firebase Storage específico
-keep class com.google.firebase.storage.** { *; }

# Firebase Crashlytics
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Firebase Analytics
-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.android.gms.measurement.** { *; }
-dontwarn com.google.firebase.analytics.**

# Firebase Performance Monitoring
-keep class com.google.firebase.perf.** { *; }
-keep class com.google.firebase.perf.metrics.** { *; }
-dontwarn com.google.firebase.perf.**

# ==========================================
# MODELOS DE DOMINIO
# ==========================================

# Preservar modelos de dominio para serialización
-keep class com.negociolisto.app.domain.model.** { *; }

# ==========================================
# REPOSITORIOS
# ==========================================

# Preservar repositorios
-keep class com.negociolisto.app.data.repository.** { *; }
-keep class com.negociolisto.app.domain.repository.** { *; }

# ==========================================
# VIEWMODELS
# ==========================================

# Preservar ViewModels
-keep class com.negociolisto.app.ui.**.viewmodel.** { *; }

# ==========================================
# SERIALIZACIÓN
# ==========================================

# Gson (si se usa)
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ==========================================
# COIL IMAGE LOADING
# ==========================================

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# ==========================================
# CORRUTINAS
# ==========================================

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ==========================================
# KOTLINX SERIALIZATION
# ==========================================

# Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-keep class kotlinx.datetime.** { *; }
-dontwarn kotlinx.serialization.**
-dontwarn kotlinx.datetime.**

# Preservar serializadores
-keep class * implements kotlinx.serialization.KSerializer { *; }
-keep class * implements kotlinx.serialization.SerializationStrategy { *; }
-keep class * implements kotlinx.serialization.DeserializationStrategy { *; }

# ==========================================
# NAVEGACIÓN
# ==========================================

# Navigation Compose
-keep class androidx.navigation.** { *; }

# ==========================================
# WORKMANAGER
# ==========================================

# WorkManager
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker

# ==========================================
# SERVICIOS PERSONALIZADOS
# ==========================================

# Servicios de backup y sincronización
-keep class com.negociolisto.app.data.service.** { *; }
-keep class com.negociolisto.app.data.sync.** { *; }
-keep class com.negociolisto.app.data.preferences.** { *; }
-keep class com.negociolisto.app.data.parsing.** { *; }

# ==========================================
# REGLAS GENERALES
# ==========================================

# Preservar clases nativas
-keepclasseswithmembernames class * {
    native <methods>;
}

# Preservar clases de enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Preservar clases Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Preservar clases Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ==========================================
# OPTIMIZACIONES
# ==========================================

# Optimizar código
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Remover logs en release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}