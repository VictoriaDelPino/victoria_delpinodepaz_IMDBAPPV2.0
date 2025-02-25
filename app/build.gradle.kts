import org.apache.tools.ant.util.JavaEnvUtils.VERSION_1_6

plugins {

    alias(libs.plugins.android.application)

    id("com.google.gms.google-services")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
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
    implementation(libs.play.services.maps)
    implementation(libs.exifinterface)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation ("com.google.code.gson:gson:2.8.6")

    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-analytics")
// Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

// Google Sign-In y One Tap
    implementation("com.google.android.gms:play-services-auth:20.6.0")
//Facebook
    implementation("com.facebook.android:facebook-android-sdk:16.0.0")
    //Core de firebase para operaciones complejas
    implementation("com.google.firebase:firebase-core:21.1.1")
//Firestore
    implementation("com.google.firebase:firebase-firestore:25.1.2")
    implementation ("com.google.firebase:firebase-storage:20.2.1")

    //maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.libraries.places:places:4.1.0")

    //Codepicker
    implementation("com.hbb20:ccp:2.5.0")

}