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
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("Main")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}

tasks.test {
    enabled = false
}