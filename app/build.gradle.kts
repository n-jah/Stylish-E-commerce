plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.stylish"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.stylish"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.firebase.database.ktx)
    implementation(libs.common)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.room.ktx)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.glide) // Latest stable version as of the last update
    annotationProcessor (libs.compiler)
    implementation (libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v287)
    implementation(libs.play.services.auth)
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.facebook.android:facebook-android-sdk:15.1.0")
    implementation ("androidx.security:security-crypto:1.1.0-alpha03")
    // OkHttp for making HTTP requests
    implementation ("com.sun.mail:android-mail:1.6.2")
    implementation ("com.sun.mail:android-activation:1.6.2")
    implementation ("com.airbnb.android:lottie:5.0.3")
    implementation ("com.stripe:stripe-android:20.53.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation (libs.material.v190)
    //swiprefresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")



}