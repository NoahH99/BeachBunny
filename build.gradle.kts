import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.4.32"
}

allprojects {
    group = "com.noahhendrickson"
    version = "0.1.0"

    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(kotlin("stdlib"))
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"

        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
}

subprojects {
    dependencies {
        implementation("io.github.microutils", "kotlin-logging-jvm", "2.0.6")
        implementation("org.slf4j", "slf4j-simple", "1.7.30")

        implementation("org.jetbrains.exposed", "exposed-core", "0.31.1")
        implementation("org.jetbrains.exposed", "exposed-jdbc", "0.31.1")

        testImplementation("org.junit.jupiter", "junit-jupiter", "5.7.0")
        testImplementation("io.mockk", "mockk", "1.11.0")
        testImplementation("org.assertj", "assertj-core", "3.19.0")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
