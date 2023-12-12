import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

plugins {
    kotlin("jvm") version "1.9.10"
    `maven-publish`
}

repositories {
    mavenCentral()
    maven("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository")
    maven("https://www.jitpack.io")
    maven("https://maven.xpdustry.com/releases")
}

val mindustryVersion = "v146"
val jabelVersion = "93fde537c7"
val submoduleName = "genesis-core"

group = "kennarddh"
version = rootProject.file("$submoduleName/version.txt").readLines().first()

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
    val pluginJson = rootProject.file("$submoduleName/src/main/resources/plugin.json")
    val pluginJsonText = pluginJson.readText()
    val jsonSlurper = JsonSlurper()

    @Suppress("UNCHECKED_CAST")
    val pluginJsonParsed = jsonSlurper.parseText(pluginJsonText) as MutableMap<Any, Any>

    pluginJsonParsed["version"] = version

    val builder = JsonBuilder(pluginJsonParsed)

    val pluginJsonModified = builder.toPrettyString()

    pluginJson.writeText(pluginJsonModified)

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

tasks.register<Jar>("buildAndPublish") {
    dependsOn(tasks.build, tasks.publish)
}

tasks.register<Jar>("buildAndPublishLocal") {
    dependsOn(tasks.build, tasks.publishToMavenLocal)
}
