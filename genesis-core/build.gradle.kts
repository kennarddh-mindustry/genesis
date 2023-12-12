plugins {
    kotlin("jvm") version "1.9.10"
    `maven-publish`
}

group = "kennarddh"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository")
    maven("https://www.jitpack.io")
    maven("https://maven.xpdustry.com/releases")
}

val mindustryVersion by extra { "v146" }
val jabelVersion by extra { "93fde537c7" }

dependencies {
    compileOnly("com.github.Anuken.Arc:arc-core:$mindustryVersion")
    compileOnly("com.github.Anuken.Mindustry:core:$mindustryVersion")
    compileOnly("com.github.Anuken.Mindustry:server:$mindustryVersion")
    annotationProcessor("com.github.Anuken:jabel:$jabelVersion")

    compileOnly("com.xpdustry:kotlin-runtime:3.1.0-k.1.9.10")
    compileOnly(kotlin("reflect"))
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    main {
        java.srcDir("src/main/kotlin")
    }
}

configurations.runtimeClasspath {
    exclude(group = "org.jetbrains.kotlin")
    exclude(group = "org.jetbrains.kotlinx")
}

tasks.register<Jar>("buildJAR") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    from(rootProject.fileTree("src/main/resources/"))
}

publishing {
    repositories {
        maven {
            name = "reposilite"
            url = uri("http://23.95.107.12:9999/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
            isAllowInsecureProtocol = true
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "kennarddh"
            artifactId = "genesis-core"
            version = version
            from(components["java"])
        }
    }
}