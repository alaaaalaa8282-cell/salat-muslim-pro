import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val keystoreProps = Properties()
val keystoreFile = rootProject.file("keystore.properties")
if (keystoreFile.exists()) keystoreProps.load(keystoreFile.inputStream())

android {
    namespace   = "com.alaa"
    compileSdk  = 35

    defaultConfig {
        applicationId = "com.alaa"
        minSdk        = 24
        targetSdk     = 35
        versionCode   = 1
        versionName   = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        create("release") {
            val ksPath = System.getenv("KEYSTORE_PATH")
                ?: keystoreProps.getProperty("storeFile")
            if (ksPath != null) {
                storeFile     = rootProject.file(ksPath)
                storePassword = System.getenv("STORE_PASSWORD") ?: keystoreProps.getProperty("storePassword") ?: ""
                keyAlias      = System.getenv("KEY_ALIAS")      ?: keystoreProps.getProperty("keyAlias")      ?: ""
                keyPassword   = System.getenv("KEY_PASSWORD")   ?: keystoreProps.getProperty("keyPassword")   ?: ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled   = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val sc = signingConfigs.getByName("release")
            if (sc.storeFile?.exists() == true) signingConfig = sc
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.datastore)

    // Material (للـ XML Theme)
    implementation(libs.material)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.icons)
    implementation(libs.navigation.compose)
    debugImplementation(libs.compose.ui.tooling.debug)

    // Koin DI
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Prayer times
    implementation(libs.adhan)

    // Location
    implementation(libs.play.location)

    // Coroutines
    implementation(libs.coroutines)

    // Image
    implementation(libs.coil)
}
