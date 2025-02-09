import org.apache.tools.ant.util.JavaEnvUtils.VERSION_1_6

plugins {

    alias(libs.plugins.android.application)

    id("com.google.gms.google-services")
}

android {
    namespace = "edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0"
    compileSdk = 34

    defaultConfig {
        applicationId = "edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0"
        minSdk = 27
        targetSdk = 34
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

        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-analytics")
// Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

// Google Sign-In y One Tap
    implementation("com.google.android.gms:play-services-auth:20.6.0")
//Facebook
    implementation("com.facebook.android:facebook-android-sdk:16.0.0")

//Firestore
    implementation("com.google.firebase:firebase-firestore:25.1.2")

}