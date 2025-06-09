plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20210307")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

application {
    mainClass.set("Main")
}

tasks.test {
    enabled = false
}