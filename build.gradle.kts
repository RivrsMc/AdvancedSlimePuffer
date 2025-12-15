import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    id("io.papermc.paperweight.patcher")
}

paperweight {
    upstreams.create("pufferfish") {
        repo.set(github("pufferfish-gg", "pufferfish"))
        ref = providers.gradleProperty("pufferfishRef")

        // Setup file patches for build scripts
        patchFile {
            path = "pufferfish-api/build.gradle.kts"
            outputFile = file("aspaper-api/build.gradle.kts")
            patchFile = file("aspaper-api/build.gradle.kts.patch")
        }
        patchFile {
            path = "pufferfish-server/build.gradle.kts"
            outputFile = file("aspaper-server/build.gradle.kts")
            patchFile = file("aspaper-server/build.gradle.kts.patch")
        }

        patchRepo("paperApi") {
            upstreamPath = "paper-api"
            patchesDir = file("aspaper-api/paper-patches")
            outputDir = file("paper-api")
        }
        patchDir("pufferfishApi") {
            upstreamPath = "pufferfish-api"
            excludes = listOf("build.gradle.kts", "build.gradle.kts.patch", "paper-patches")
            patchesDir = file("aspaper-api/pufferfish-patches")
            outputDir = file("pufferfish-api")
        }
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(JAVA_VERSION)
        }
    }

    repositories {
        mavenCentral()
        maven(PAPER_MAVEN_PUBLIC_URL)
        maven("https://jitpack.io")
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = JAVA_VERSION
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
}
