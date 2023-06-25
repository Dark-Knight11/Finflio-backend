val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val kmongo_version: String by project
val koin_ktor: String by project
val commons_codec_version: String by project

plugins {
    kotlin("jvm") version "1.8.21"
    id("io.ktor.plugin") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.21"
    id("org.jetbrains.dokka") version "1.8.20"
}

group = "com.finflio"
version = "0.0.1"
application {
    mainClass.set("com.finflio.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {

    // Content Negotiation
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")

    // Core
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")

    // Kotlin Serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")

    // CORS
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")

    // Status Pages
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")

    // Auth
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")

    // Netty
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")

    // Tests
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    // Kmongo
    implementation("org.litote.kmongo:kmongo-coroutine:$kmongo_version")
    implementation("org.litote.kmongo:kmongo-async:$kmongo_version")

    // Koin for Ktor
    implementation("io.insert-koin:koin-ktor:$koin_ktor")
    // SLF4J Logger
    implementation("io.insert-koin:koin-logger-slf4j:$koin_ktor")

    // Hashing
    implementation("commons-codec:commons-codec:$commons_codec_version")
}