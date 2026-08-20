import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.javadoc.Javadoc

plugins {
    `java-library`
    `maven-publish`
    id("xyz.jpenilla.toothpick")
}

toothpick {
    forkName = "Purpur"
    groupId = "net.pl3x.purpur"
    forkUrl = "https://github.com/pl3xgaming/Purpur"
    val versionTag = System.getenv("BUILD_NUMBER")
        ?: "\"${commitHash() ?: error("Could not obtain git hash")}\""
    forkVersion = "git-$forkName-$versionTag"

    minecraftVersion = "1.16.5"
    nmsPackage = "1_16_R3"
    nmsRevision = "R0.1-SNAPSHOT"

    upstream = "Paper"
    upstreamBranch = "origin/master"

    server {
        project = projects.purpurServer.dependencyProject
        patchesDir = rootProject.projectDir.resolve("patches/server")
    }
    api {
        project = projects.purpurApi.dependencyProject
        patchesDir = rootProject.projectDir.resolve("patches/api")
    }
}

tasks.named("paperclip") {
    doFirst {
        val paperclipPom = layout.projectDirectory.file("Paper/work/Paperclip/assembly/pom.xml").asFile
        paperclipPom.writeText(
            paperclipPom.readText().replace(
                "https://papermc.io/repo/repository/maven-releases/",
                "https://repo.papermc.io/repository/maven-releases/"
            )
        )
    }
}

subprojects {
    repositories {
        // use available dependency repositories for 1.16.5 builds
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://nexus.velocitypowered.com/repository/velocity-artifacts-snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            name = "sonatype-oss-snapshots"
        }
    }

    java {
        sourceCompatibility = JavaVersion.toVersion(17)
        targetCompatibility = JavaVersion.toVersion(17)
    }

    tasks.withType<Javadoc>().configureEach {
        isFailOnError = false
    }

    publishing.repositories.maven {
        url = uri("https://repo.pl3x.net/snapshots")
        credentials(PasswordCredentials::class)
    }
}

project(":purpur-server") {
    val runtimeClasspath = configurations.named("runtimeClasspath")

    tasks.withType<ShadowJar>().configureEach {
        dependencies {
            exclude(dependency("io.papermc:minecraft-server:.*"))
        }
        from({
            runtimeClasspath.get()
                .filter { it.name.startsWith("minecraft-server-") }
                .map {
                    zipTree(it).matching {
                        exclude(
                            "com/google/common/**",
                            "com/google/gson/**",
                            "com/google/thirdparty/**",
                            "io/netty/**",
                            "META-INF/io.netty.versions.properties",
                            "META-INF/native/libnetty*",
                            "com/mojang/brigadier/**",
                            "org/apache/logging/**",
                            "org/slf4j/**",
                            "META-INF/MANIFEST.MF",
                            "com/mojang/authlib/yggdrasil/YggdrasilGameProfileRepository.class",
                            "net/minecraft/server/MinecraftVersion.class",
                            "META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat"
                        )
                    }
                }
        })
        exclude("META-INF/services/javax.annotation.processing.Processor")
    }
}
