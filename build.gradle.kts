// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    id("com.google.protobuf") version "0.9.5" apply false
}

buildscript {
    dependencies {
        classpath(libs.protobuf.gradle.plugin)
    }
}