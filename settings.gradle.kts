pluginManagement {
    includeBuild("bilimiao-build")
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") //pbandk
    }
}
rootProject.name = "bilimiao"
include(":app")
include(":bilimiao-comm", ":bilimiao-cover", ":bilimiao-download", "bilimiao-appwidget", "bilimiao-compose")
include(":DanmakuFlameMaster")
include(":benchmark")
include(":grpc-generator")
