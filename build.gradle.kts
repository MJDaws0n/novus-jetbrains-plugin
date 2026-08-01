plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "dev.mjdawson.novus"
version = "0.1.2"

val localIdePath = providers.gradleProperty("localIdePath").orNull

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        if (localIdePath != null) {
            local(localIdePath)
        } else {
            intellijIdea("2026.2")
        }
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        id = "dev.mjdawson.novus"
        name = "Novus Language Support"
        version = project.version.toString()
        description = """
            Full language and toolchain support for the Novus systems language: parser,
            highlighting, completion, navigation, refactoring, structure, formatting,
            Novus run/build configurations, and Nox package-management actions.
        """.trimIndent()
        vendor {
            name = "Novus Community"
            url = "https://github.com/MJDaws0n/Novus"
        }
        ideaVersion {
            sinceBuild = "262"
        }
        changeNotes = "Fixes invisible typed input in interactive Novus run configurations while retaining terminal emulation."
    }
    buildSearchableOptions = false
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(25) }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 25
    }
}
