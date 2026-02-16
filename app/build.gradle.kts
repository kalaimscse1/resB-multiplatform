import java.util.Properties
import java.io.FileInputStream
import java.io.FileOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.kotlin.ksp)
}

val versionPropsFile = file("${project.rootDir}/version.properties")

fun loadVersionProps(): Properties {
    val props = Properties()
    if (versionPropsFile.exists()) {
        props.load(FileInputStream(versionPropsFile))
    } else {
        props["major"] = "1"
        props["minor"] = "0"
        props["patch"] = "0"
        props["build"] = "1"
    }
    return props
}

val versionProps = loadVersionProps()
val major = versionProps["major"]?.toString()?.toInt() ?: 1
val minor = versionProps["minor"]?.toString()?.toInt() ?: 0
val patch = versionProps["patch"]?.toString()?.toInt() ?: 0
val build = versionProps["build"]?.toString()?.toInt() ?: 1

val computedVersionCode = major * 10000 + minor * 1000 + patch * 100 + build
val computedVersionName = "$major.$minor.$patch.$build"

android {
    namespace = "com.warriortech.resb"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.warriortech.resb"
        minSdk = 26
        targetSdk = 35
        versionCode = computedVersionCode
        versionName = computedVersionName
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
            isShrinkResources = false
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
            if (this is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                outputFileName = "Res-B.apk"
            }
        }
    }
    lint {
        checkDependencies = true
        abortOnError = true
        baseline = file("lint-baseline.xml")
    }
}

tasks.register("incrementVersion") {
    doLast {
        val props = loadVersionProps()
        val currentBuild = props["build"]?.toString()?.toInt() ?: 0
        props["build"] = (currentBuild + 1).toString()
        props.store(FileOutputStream(versionPropsFile), null)
        println("Version incremented to build: ${props["build"]}")
    }
}

tasks.whenTaskAdded {
    if (name == "assembleRelease" || name == "bundleRelease") {
        dependsOn("incrementVersion")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material3.lint)
    implementation(libs.retrofit2.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.play.services.code.scanner)
    implementation(libs.androidx.security.crypto.ktx)
    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android.core)
    implementation(libs.timber)
    implementation(libs.androidx.material.v181)
    implementation(libs.androidx.material.icons.extended)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("com.google.android.material:material:1.12.0")

    implementation("com.itextpdf:itextg:5.5.10")
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.accompanist:accompanist-permissions:0.35.0-alpha")

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
    implementation("co.yml:ycharts:2.1.0")
    
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
}
