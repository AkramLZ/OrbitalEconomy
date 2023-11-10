plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version("7.1.2")
}

group = "com.akraml"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        content {
            includeGroup("org.bukkit")
            includeGroup("org.spigotmc")
        }
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.12-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("co.aikar:acf-bukkit:0.5.1-SNAPSHOT")
    implementation("com.zaxxer:HikariCP:4.0.2")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}