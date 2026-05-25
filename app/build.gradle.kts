plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace   = "com.alaa"
    compileSdk  = 35

    defaultConfig {
        applicationId = "com.alaa"
        minSdk        = 24
        targetSdk     = 35
        versionCode   = 1
        versionName   = "1.0.0"
    }

    signingConfigs {
        create("release") {
            val storeFile0 = project.findProperty("STORE_FILE")?.toString()
                ?: System.getenv("STORE_FILE")
            val storePass  = project.findProperty("STORE_PASSWORD")?.toString()
                ?: System.getenv("STORE_PASSWORD")
            val keyAlias0  = project.findProperty("KEY_ALIAS")?.toString()
                ?: System.getenv("KEY_ALIAS")
            val keyPass    = project.findProperty("KEY_PASSWORD")?.toString()
                ?: System.getenv("KEY_PASSWORD")

            if (storeFile0 != null) storeFile = file(storeFile0)
            if (storePass  != null) storePassword = storePass
            if (keyAlias0  != null) keyAlias = keyAlias0
            if (keyPass    != null) keyPassword = keyPass
            storeType = "PKCS12"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled   = false
            isShrinkResources = false
            signingConfig     = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("io.insert-koin:koin-android:3.5.6")
    implementation("io.insert-koin:koin-androidx-compose:3.5.6")

    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
}
