plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.kotlin.ksp)
//    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.warriortech.resb"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.warriortech.resb"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
                arguments["room.incremental"] = "true"
                arguments["room.expandProjection"] = "true"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false      // Removes unused resources
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            )
        }
    }
    applicationVariants.all {
        outputs.all {
            if (this is com.android.build.gradle.internal.api.BaseVariantOutputImpl &&
                buildType.name == "release"
            ) {
                outputFileName = "Res-B.apk"
            }
            if (this is com.android.build.gradle.internal.api.BaseVariantOutputImpl &&
                buildType.name == "debug"
            ) {
                outputFileName = "Res-B.apk"
            }
        }
    }
    lint {
        // Set to true to check all issues, including those found in libraries
        checkDependencies = true
        // Set to true to have the build fail if errors are found
        abortOnError = true // This is often true for release builds
        // If true, turns off processing of vital checks on release builds
        // checkReleaseBuilds = false // Not recommended for production quality
        // If true, don't include descriptive text in the error output
        // quiet = true
        // Specifies the baseline file to use.
        // The baseline file is created by running :app:lintDebug (or any lint task)
        // and then copying the lint-results.xml to the baseline file location.
        baseline = file("lint-baseline.xml") // Common location
    }
    splits {
        abi {
            isEnable = false
        }
    }
}

dependencies {
    // Network



    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.navigation.compose.android)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.material3.lint)
    implementation(libs.retrofit2.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation("com.google.accompanist:accompanist-swiperefresh:0.30.1")

//    implementation(libs.esc.pos.usb.net.android)

    // WorkManager for synchronization
    implementation (libs.androidx.work.runtime.ktx)

    // DataStore for preferences
    implementation (libs.androidx.datastore.preferences)

    // Network connectivity monitoring
    implementation (libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.identity.jvm)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.play.services.code.scanner)
//    implementation(libs.play.services.cast.framework)
//    implementation(libs.androidx.room.compiler)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.security.crypto.ktx)
    ksp(libs.room.compiler.v252)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android.core)
    implementation(libs.timber)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.v181)
    implementation(libs.androidx.material.icons.extended)
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("com.google.android.material:material:1.12.0")

    // PDF and Excel export
    implementation("com.itextpdf:itextg:5.5.10")
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0") // Or the latest version

    // AI Integration dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.accompanist:accompanist-permissions:0.35.0-alpha")

    // Performance optimizations
    implementation("androidx.compose.runtime:runtime-tracing:1.0.0-beta01")
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.hilt.android.testing)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-svg:2.4.0")
    // Chart library
    implementation("co.yml:ycharts:2.1.0")
    
    // Barcode Scanner (MLKit)
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
}