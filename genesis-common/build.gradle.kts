import fr.xpdustry.toxopid.dsl.mindustryDependencies
import fr.xpdustry.toxopid.spec.ModMetadata
import fr.xpdustry.toxopid.spec.ModPlatform
import fr.xpdustry.toxopid.task.GithubArtifactDownload

plugins {
    kotlin("jvm") version "1.9.10"
    java
    `maven-publish`
    id("fr.xpdustry.toxopid") version "3.2.0"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.xpdustry.com/releases")
    maven("https://maven.xpdustry.com/mindustry")

    maven {
        url = uri("http://23.95.107.12:9999/releases")
        isAllowInsecureProtocol = true
    }
}

toxopid {
    // The version with which your mod/plugin is compiled.
    // If not set, will compile with v143 by default.
    compileVersion.set("v146")
    // The version with which your mod/plugin is tested.
    // If not set, defaults to the value of compileVersion.
    runtimeVersion.set("v146")

    // The platforms you target, you can choose DESKTOP, HEADLESS or/and ANDROID.
    // If not set, will target DESKTOP by default.
    platforms.add(ModPlatform.HEADLESS)
}

val metadata = ModMetadata.fromJson(project.file("plugin.json"))

project.group = "kennarddh"
project.version = metadata.version

dependencies {
    mindustryDependencies()

    compileOnly("com.xpdustry:kotlin-runtime:3.1.0-k.1.9.10")

    compileOnly(project(":genesis-core"))

    implementation("org.slf4j:slf4j-api:2.0.11")
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

tasks {
    jar {
        doFirst {
            val metadataFile = temporaryDir.resolve("plugin.json")

            metadataFile.writeText(metadata.toJson(true))

            from(metadataFile)
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(sourceSets.main.get().output)
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }
}

val downloadKotlinRuntime =
    tasks.register<GithubArtifactDownload>("downloadKotlinRuntime") {
        user.set("xpdustry")
        repo.set("kotlin-runtime")
        name.set("kotlin-runtime.jar")
        version.set("v3.1.0-k.1.9.10")
    }

val genesisCorePlugin: File = project.file("../genesis-core/build/libs/genesis-core-1.0.1.jar")

if (genesisCorePlugin == null)
    throw Exception("Core artifact missing. Built it before running this")

tasks.runMindustryServer {
    mods.setFrom(setOf(tasks.jar, downloadKotlinRuntime, genesisCorePlugin))
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
            groupId = groupId
            artifactId = metadata.name
            version = version
            from(components["java"])
        }
    }
}

tasks.register<Jar>("buildAndPublish") {
    dependsOn(tasks.jar, tasks.publish)
}

tasks.register<Jar>("buildAndPublishLocal") {
    dependsOn(tasks.jar, tasks.publishToMavenLocal)
}

tasks.register("getArtifactPath") {
    doLast { println(tasks.jar.get().archiveFile.get().toString()) }
}