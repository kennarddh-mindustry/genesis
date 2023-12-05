plugins {
    kotlin("jvm") version "1.9.0"
}

group = "kennarddh"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository")
    maven("https://www.jitpack.io")
}

val mindustryVersion by extra { "v146" }
val jabelVersion by extra { "93fde537c7" }

dependencies {
    compileOnly("com.github.Anuken.Arc:arc-core:$mindustryVersion")
    compileOnly("com.github.Anuken.Mindustry:core:$mindustryVersion")
    annotationProcessor("com.github.Anuken:jabel:$jabelVersion")
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    main {
        java.srcDir("src/main/kotlin")
    }
}

tasks.register<Jar>("buildJAR") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output)
    from(rootProject.fileTree("src/main/resources/"))
}
