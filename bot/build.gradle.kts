import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

repositories {
    maven("https://maven.kotlindiscord.com/repository/maven-public/")
    maven("https://dl.bintray.com/kordlib/Kord")
}

dependencies {
    implementation(project(":database"))

    implementation("com.kotlindiscord.kord.extensions", "kord-extensions", "1.4.0-RC7")
    implementation("com.gitlab.kordlib", "kordx.emoji", "0.4.0")

    implementation("com.google.cloud", "google-cloud-dialogflow", "3.0.2")
}

application {
    mainClass.set("com.noahhendrickson.beachbunny.bot.BeachBunnyKt")
    mainClassName = "com.noahhendrickson.beachbunny.bot.BeachBunnyKt"
}

tasks.withType<ShadowJar> {
    archiveName = "BeachBunnyBot-${archiveVersion.get()}.jar"
}
