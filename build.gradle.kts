import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
}

group = "net.azisaba.velocity"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://repo.azisaba.net/repository/maven-snapshots/") }
}

dependencies {
    kapt(libs.velocity.api)
    compileOnly(libs.velocity.api)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.mccoroutine.velocity.api)
    implementation(libs.mccoroutine.velocity.core)
    implementation(libs.datadriven)
    implementation(libs.graph)

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
}
