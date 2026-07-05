pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    // 自动解析并下载 grpc-generator 所需的 JDK 17 toolchain
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
        maven("https://gitlab.com/api/v4/projects/38224197/packages/maven")
    }
}
rootProject.name = "bilimiao"
include(":app")
include(":desktop-app")
include(":bilimiao-comm", ":bilimiao-cover", ":bilimiao-download", "bilimiao-appwidget", "bilimiao-compose")
include(":DanmakuFlameMaster")
include(":danmaku-engine")
include(":benchmark")
include(":grpc-generator")

// Use forked mediamp with desktop MPV render support
// Published to local Maven via: cd mediamp && ./gradlew publishToMavenLocal

