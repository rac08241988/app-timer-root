plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.apptimerroot.lock"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.apptimerroot.lock"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"
    }

    signingConfigs {
        create("release") {
            // Values are supplied via gradle.properties / -P flags or env.
            // See INSTALL.md. A throwaway keystore is fine for a personal tool.
            storeFile = file(
                (project.findProperty("ATR_STORE_FILE") as String?)
                    ?: "${rootDir}/apptimerroot-release.jks"
            )
            storePassword = (project.findProperty("ATR_STORE_PASSWORD") as String?) ?: "apptimerroot"
            keyAlias = (project.findProperty("ATR_KEY_ALIAS") as String?) ?: "apptimerroot"
            keyPassword = (project.findProperty("ATR_KEY_PASSWORD") as String?) ?: "apptimerroot"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = false
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.12.0")
}
