// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    extra.apply {
        set("compile_sdk_version", 36)
        set("build_tools_version", 36)
        set("target_sdk_version", 36)
    }

    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
    }

    dependencies {
        // 强制使用最新 R8 patch，修复 AGP 8.13.0 自带 R8 8.13.6 在解析
        // Kotlin 2.3.0 metadata 时抛出 "Should never be called" 异常的问题
        classpath("com.android.tools:r8:8.13.19")
    }

    configurations.all {
        resolutionStrategy {
            force("com.android.tools:r8:8.13.19")
        }
    }

}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.multiplatform) apply false
    alias(libs.plugins.jetbrains.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.serialization) apply false
    alias(libs.plugins.google.protobuf)  apply false
    alias(libs.plugins.ksp) apply false
}

allprojects {
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            // This lib is used by com.github.mikaelzero.mojito:SketchImageViewLoader:1.8.7 and only available in bintray
            // It has been moved to mavenCentral with a different module name
            substitute(module("me.panpf:sketch-gif:2.7.1")).using(module("io.github.panpf.sketch:sketch-gif:2.7.1"))
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
