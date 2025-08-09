plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.kcode.gankotlin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kcode.coincow"
        minSdk = 26
        targetSdk = 34
        versionCode = getVersionCode()
        versionName = getVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        buildConfigField("String", "COINGECKO_API_KEY", "\"${project.findProperty("COINGECKO_API_KEY") ?: ""}\"")
    }

    signingConfigs {
        create("release") {
            val keystoreFile = System.getenv("KEYSTORE_FILE")
            val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
            val keyAlias = System.getenv("KEY_ALIAS")
            val keyPassword = System.getenv("KEY_PASSWORD")
            
            if (!keystoreFile.isNullOrEmpty() && !keystorePassword.isNullOrEmpty() && 
                !keyAlias.isNullOrEmpty() && !keyPassword.isNullOrEmpty()) {
                val keystoreFileObj = file(keystoreFile)
                if (keystoreFileObj.exists()) {
                    storeFile = keystoreFileObj
                    storePassword = keystorePassword
                    this.keyAlias = keyAlias
                    this.keyPassword = keyPassword
                    println("Using release keystore: ${keystoreFileObj.absolutePath}")
                } else {
                    println("Warning: Keystore file not found at: ${keystoreFileObj.absolutePath}")
                    // Use debug keystore as fallback
                    storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
                    storePassword = "android"
                    this.keyAlias = "androiddebugkey"
                    this.keyPassword = "android"
                }
            } else {
                println("Warning: Release keystore environment variables not set. Using debug keystore.")
                // Use debug keystore for local development
                storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
                storePassword = "android"
                this.keyAlias = "androiddebugkey"
                this.keyPassword = "android"
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("String", "BUILD_TYPE", "\"debug\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
        }
        
        release {
            // Code shrinking and obfuscation
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            
            // Signing configuration
            signingConfig = signingConfigs.getByName("release")
            
            // ProGuard/R8 configuration
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Build configuration fields
            buildConfigField("String", "BUILD_TYPE", "\"release\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            
            // Enable R8 full mode for maximum optimization
            isMinifyEnabled = true
            
            // Optimize for size and performance
            isCrunchPngs = true
            
            // Enable ZIP alignment for better performance
            isZipAlignEnabled = true
        }
    }
    
    bundle {
        language {
            // Enable language-based APK splits for smaller downloads
            enableSplit = true
        }
        density {
            // Enable density-based APK splits
            enableSplit = true
        }
        abi {
            // Enable ABI-based APK splits
            enableSplit = true
        }
    }
    
    // Packaging options for optimization
    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module",
                "META-INF/gradle/incremental.annotation.processors"
            )
        }
    }
    
    // Lint configuration for release builds
    lint {
        checkReleaseBuilds = true
        abortOnError = false
        warningsAsErrors = false
        disable += setOf("MissingTranslation", "ExtraTranslation")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    kapt(libs.moshi.codegen)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Image loading
    implementation(libs.coil.compose)

    // Charts
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.core)

    // Widget
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.compiler)
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
    
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Version management functions
fun getVersionCode(): Int {
    // Try to get version code from environment variable (for CI/CD)
    val envVersionCode = System.getenv("VERSION_CODE")
    if (envVersionCode != null) {
        return envVersionCode.toInt()
    }
    
    // Try to get from Git tag count
    return try {
        val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
            .directory(project.rootDir)
            .start()
        val output = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        if (process.exitValue() == 0) {
            output.toInt()
        } else {
            1
        }
    } catch (e: Exception) {
        println("Warning: Could not determine version code from Git. Using default value 1.")
        1
    }
}

fun getVersionName(): String {
    // Try to get version name from environment variable (for CI/CD)
    val envVersionName = System.getenv("VERSION_NAME")
    if (envVersionName != null) {
        return envVersionName
    }
    
    // Try to get from Git tag
    return try {
        val process = ProcessBuilder("git", "describe", "--tags", "--abbrev=0")
            .directory(project.rootDir)
            .start()
        val output = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        if (process.exitValue() == 0 && output.isNotEmpty()) {
            // Remove 'v' prefix if present
            output.removePrefix("v")
        } else {
            "1.0.0"
        }
    } catch (e: Exception) {
        println("Warning: Could not determine version name from Git tag. Using default value 1.0.0.")
        "1.0.0"
    }
}

// Gradle tasks for version management
tasks.register("printVersionCode") {
    doLast {
        println(android.defaultConfig.versionCode)
    }
}

tasks.register("printVersionName") {
    doLast {
        println(android.defaultConfig.versionName)
    }
}

tasks.register("printVersionInfo") {
    doLast {
        println("Version Code: ${android.defaultConfig.versionCode}")
        println("Version Name: ${android.defaultConfig.versionName}")
        println("Application ID: ${android.defaultConfig.applicationId}")
        println("Target SDK: ${android.defaultConfig.targetSdk}")
        println("Min SDK: ${android.defaultConfig.minSdk}")
    }
}