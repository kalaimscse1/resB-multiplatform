// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.dagger.hilt) apply false
    alias(libs.plugins.kotlin.ksp) apply false
//    alias(libs.plugins.kotlin.kapt) apply false
//    alias(libs.plugins.kotlin.)
}

// Project-level build.gradle.kts
buildscript {

//    val hiltVersion = "2.48" // Ensure this is defined
    dependencies {
        classpath(libs.hilt.android.gradlePlugin)
        // ... other classpaths
//        dagger-hilt-android-gradle-plugin
    }
}

