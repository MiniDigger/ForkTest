import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java // TODO java launcher tasks
    id("io.papermc.paperweight.patcher") version "2.0.0-SNAPSHOT"
}

paperweight {
    upstreams.register("fork") {
        repo = github("PaperMC", "paperweight-examples")
        ref = "98104f3a8b8709af8b91a63744da6a64167e9b85"

        patchFile {
            path = "fork-server/build.gradle.kts"
            outputFile = file("forky-server/build.gradle.kts")
            patchFile = file("forky-server/build.gradle.kts.patch")
        }
        patchFile {
            path = "fork-api/build.gradle.kts"
            outputFile = file("forky-api/build.gradle.kts")
            patchFile = file("forky-api/build.gradle.kts.patch")
        }
        patchRepo {
            name = "paperApi"
            upstreamPath = "paper-api"
            patchesDir = file("forky-api/paper-patches")
            outputDir = file("paper-api")
        }
        patchRepo {
            name = "paperApiGenerator"
            upstreamPath = "paper-api-generator"
            patchesDir = file("forky-api-generator/paper-patches")
            outputDir = file("paper-api-generator")
        }
        patchDir {
            name = "forkApi"
            upstreamPath = "fork-api"
            excludes = listOf("build.gradle.kts", "build.gradle.kts.patch")
            patchesDir = file("forky-api/fork-patches")
            outputDir = file("fork-api")
        }
    }
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
    }

    dependencies {
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
        options.isFork = true
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test> {
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT)
        }
    }

    extensions.configure<PublishingExtension> {
        repositories {
            /*
            maven("https://repo.papermc.io/repository/maven-snapshots/") {
                name = "paperSnapshots"
                credentials(PasswordCredentials::class)
            }
             */
        }
    }
}
