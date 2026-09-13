
plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<JavaCompile>().all {
    enabled = false
}

dependencies {
    val pbandkVersion = "0.16.0"
    compileOnly("pro.streem.pbandk:pbandk-runtime:$pbandkVersion")
    compileOnly("pro.streem.pbandk:protoc-gen-pbandk-lib:$pbandkVersion")
}