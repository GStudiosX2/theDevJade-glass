plugins {
    kotlin("jvm") version "1.9.20"
    application
}

group = "com.thedevjade.glass-kotlin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation("org.slf4j:slf4j-jdk14:2.0.9")
    implementation("io.github.kawamuray.wasmtime:wasmtime-java:0.19.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.thedevjade.glass.MainKt")
}