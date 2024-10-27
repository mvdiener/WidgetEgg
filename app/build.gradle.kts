import com.google.protobuf.gradle.id
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.protobuf")
    kotlin("plugin.serialization") version "2.0.20"
}

android {
    namespace = "com.widgetegg.widgeteggapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.widgetegg.widgeteggapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "1.2.0"

        ndk {
            debugSymbolLevel = "SYMBOL_TABLE"
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        val secretsFile = project.rootProject.file("secrets.properties")
        val properties = Properties()
        properties.load(secretsFile.inputStream())

        val secretKey = properties.getProperty("SECRET_KEY") ?: ""
        debug {
            buildConfigField("String", "SECRET_KEY", secretKey)
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "SECRET_KEY", secretKey)
            isDebuggable = false
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
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Protobuf
    implementation(libs.protobuf.java)
    implementation(libs.protobuf.kotlin)

    // Network
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.kotlinx.coroutines.android)

    // Glance
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)

    // Datastore
    implementation(libs.androidx.datastore.preferences)

    // JSON serialization
    implementation(libs.kotlinx.serialization.json)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.27.0"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("kotlin") {
                    option("lite")
                }
                id("java") {
                    option("lite")
                }
            }
        }
    }
}