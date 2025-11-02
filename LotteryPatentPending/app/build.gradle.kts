plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.lotterypatentpending"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.lotterypatentpending"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    //Firebase dependencies
    implementation("com.google.firebase:firebase-firestore")
      implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Generate QR bitmaps
    implementation("com.google.zxing:core:3.5.3")
    // (Scanner) Lightweight, easy camera QR scanner
    implementation("com.github.yuriy-budiyev:code-scanner:2.3.2")
}