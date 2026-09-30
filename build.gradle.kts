plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
}

group = "com.tuNombre"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.opencollab.dev/main/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("org.geysermc.geyser:api:2.4.0-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.2.3-SNAPSHOT")
    implementation("org.geysermc.cumulus:cumulus:1.1.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("")
        relocate("org.geysermc.cumulus", "com.tuNombre.myplotx.libs.cumulus")
    }
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
}
