# Crypto Tracker App ProGuard Rules
# Optimized for Google Play Store deployment

# ============================================================================
# DEBUGGING AND CRASH REPORTING
# ============================================================================

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep stack trace information
-keepattributes Exceptions

# Keep BuildConfig for feature flags and debugging
-keep class com.kcode.gankotlin.BuildConfig { *; }

# Keep crash reporting information
-keepattributes *Annotation*

# ============================================================================
# NETWORKING - RETROFIT AND OKHTTP
# ============================================================================

# Keep annotations for Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# Retrofit interface methods
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Retrofit service interfaces
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# OkHttp platform warnings
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# OkHttp specific optimizations
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# ============================================================================
# JSON SERIALIZATION - MOSHI
# ============================================================================

# Moshi annotations and adapters
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}

# Keep JsonQualifier annotations
-keep @com.squareup.moshi.JsonQualifier @interface *

# Keep enum values for Moshi serialization
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
}

# Keep Moshi adapters
-keep class **JsonAdapter { *; }
-keep class * extends com.squareup.moshi.JsonAdapter { *; }

# Keep classes with Moshi annotations
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# ============================================================================
# DEPENDENCY INJECTION - HILT
# ============================================================================

# Keep Hilt core classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Hilt Android components
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }

# Keep classes with Hilt annotations
-keepclasseswithmembers class * {
    @dagger.hilt.android.AndroidEntryPoint <methods>;
}

# Keep Hilt entry points
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.Provides class * { *; }

# Keep Hilt generated classes
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager { *; }
-keep class **_HiltModules { *; }
-keep class **_HiltComponents { *; }

# ============================================================================
# DATABASE - ROOM
# ============================================================================

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase { *; }

# Keep Room entities
-keep @androidx.room.Entity class * { *; }

# Keep Room DAOs
-keep @androidx.room.Dao class * { *; }

# Keep Room type converters
-keep @androidx.room.TypeConverter class * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter <methods>;
}

# Keep Room database callbacks
-keep class * extends androidx.room.RoomDatabase$Callback { *; }

# Room paging warnings
-dontwarn androidx.room.paging.**

# ============================================================================
# UI FRAMEWORK - JETPACK COMPOSE
# ============================================================================

# Keep Compose runtime classes
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }

# Keep Compose compiler annotations
-keep class androidx.compose.compiler.** { *; }

# Keep Kotlin metadata for Compose
-keep class kotlin.Metadata { *; }

# Keep Composable functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep Compose navigation
-keep class androidx.navigation.compose.** { *; }

# ============================================================================
# ASYNC PROGRAMMING - KOTLIN COROUTINES
# ============================================================================

# Keep coroutine dispatchers
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Keep coroutine context elements
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }

# Keep Flow and StateFlow
-keep class kotlinx.coroutines.flow.** { *; }

# Keep suspend function metadata
-keepattributes *Annotation*

# ============================================================================
# APPLICATION SPECIFIC CLASSES
# ============================================================================

# Keep domain models for API serialization
-keep class com.kcode.gankotlin.domain.model.** { *; }

# Keep data transfer objects
-keep class com.kcode.gankotlin.data.remote.dto.** { *; }

# Keep database entities
-keep class com.kcode.gankotlin.data.local.entity.** { *; }

# Keep ViewModels
-keep class com.kcode.gankotlin.presentation.viewmodel.** { *; }

# Keep use cases
-keep class com.kcode.gankotlin.domain.usecase.** { *; }

# ============================================================================
# THIRD-PARTY LIBRARIES
# ============================================================================

# Glance (App Widgets)
-keep class androidx.glance.** { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class com.kcode.gankotlin.widget.** { *; }

# Vico Charts
-keep class com.patrykandpatrick.vico.** { *; }
-keep class com.patrykandpatrick.vico.core.** { *; }
-keep class com.patrykandpatrick.vico.compose.** { *; }

# Coil Image Loading
-keep class coil.** { *; }
-keep interface coil.** { *; }

# WorkManager
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# ============================================================================
# SECURITY AND OPTIMIZATION
# ============================================================================

# Remove debug logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove Kotlin intrinsic checks in release builds
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNull(...);
    public static void checkExpressionValueIsNotNull(...);
    public static void checkNotNullExpressionValue(...);
    public static void checkParameterIsNotNull(...);
    public static void checkNotNullParameter(...);
    public static void checkReturnedValueIsNotNull(...);
    public static void checkFieldIsNotNull(...);
}

# Remove debug assertions
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
    static void checkNotNullParameter(java.lang.Object, java.lang.String);
    static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String);
    static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
}

# Remove BuildConfig debug fields
-assumenosideeffects class com.kcode.gankotlin.BuildConfig {
    public static final boolean DEBUG return false;
}

# ============================================================================
# OPTIMIZATION SETTINGS
# ============================================================================

# Enable aggressive optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Enable more aggressive shrinking
-repackageclasses ''
-allowaccessmodification

# Optimize method calls
-optimizations !method/marking/static

# ============================================================================
# SECURITY CONFIGURATION
# ============================================================================

# Obfuscate package names
-flattenpackagehierarchy

# Obfuscate class names more aggressively
-repackageclasses 'o'

# Keep sensitive classes but obfuscate them
-keep,allowobfuscation class com.kcode.gankotlin.data.remote.interceptor.** { *; }
-keep,allowobfuscation class com.kcode.gankotlin.data.validator.** { *; }

# Obfuscate API endpoints and keys (keep functionality, hide implementation)
-keepclassmembers,allowobfuscation class * {
    @retrofit2.http.* <methods>;
}

# ============================================================================
# FINAL CLEANUP
# ============================================================================

# Remove unused resources (handled by Android Gradle Plugin)
# This is configured in build.gradle.kts with isShrinkResources = true

# Remove unused code
-dontshrink

# Print mapping file for debugging
-printmapping mapping.txt

# Print seeds file
-printseeds seeds.txt

# Print usage file
-printusage usage.txt