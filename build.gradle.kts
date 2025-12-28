plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.lifiprotransmitter"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.lifiprotransmitter"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
