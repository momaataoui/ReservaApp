plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    // Pas de plugin Kotlin ici car tu travailles en Java
}

android {
    namespace = "com.ensab.reservaapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ensab.reservaapp"
        minSdk = 24
        targetSdk = 36
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
        // Java 17 est requis pour les versions récentes d'Android
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Indispensable pour éviter findViewById et rendre le code Java plus propre
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Bibliothèques de base Android (depuis libs.versions.toml)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase (Utilisation du BoM pour gérer les versions automatiquement)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Glide pour le chargement d'images
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    // AnnotationProcessor est OBLIGATOIRE en Java pour Glide
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}