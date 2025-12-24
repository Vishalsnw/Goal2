plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.goalguru"
    compileSdk = 34
    
    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    defaultConfig {
        applicationId = "com.goalguru"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${System.getProperty("user.home")}/.android/goalguru_release.keystore"
            val keystoreFile = file(keystorePath)
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = "GoalGuru@2024#SecureKey"
                keyAlias = "goalguru_key"
                keyPassword = "GoalGuru@2024#SecureKey"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (signingConfigs.getByName("release").storeFile?.exists() == true) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        
        debug {
            isMinifyEnabled = false
        }
    }
    
    lint {
        abortOnError = false
        disable += "MissingTranslation"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        compose = false
    }
}

dependencies {
    // Android Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    
    // Material Design
    implementation("com.google.android.material:material:1.10.0")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Networking & JSON
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Firebase
    implementation("com.google.firebase:firebase-messaging:23.2.1")
    implementation("com.google.firebase:firebase-analytics:21.3.0")
    
    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // Preferences
    implementation("androidx.preference:preference-ktx:1.2.1")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
