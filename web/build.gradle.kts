plugins {
    application
}

dependencies {
    implementation("io.ktor", "ktor-server-core", "1.5.4")
    implementation("io.ktor", "ktor-server-cio", "1.5.4")

    testImplementation("io.ktor", "ktor-server-tests", "1.5.4")
}

application {
    mainClass.set("com.noahhendrickson.beachbunny.BeachBunnyKt")
}
