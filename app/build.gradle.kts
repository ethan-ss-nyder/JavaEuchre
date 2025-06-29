plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20210307")
    implementation("org.deeplearning4j:deeplearning4j-core:1.0.0-M2.1")
    implementation("org.nd4j:nd4j-native-platform:1.0.0-M2.1")
    implementation("org.slf4j:slf4j-simple:1.7.30")
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