    plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.juan.movil"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.juan.movil"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX y UI
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.activity)

    // Navegación
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Material Design
    implementation(libs.material)

    // Retrofit para llamadas HTTP
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor) // Interceptor para logs de Retrofit

    // Firebase Storage
    implementation(libs.firebase.storage)

    // Glide para carga de imágenes
    implementation(libs.glide)

    // Pruebas
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

