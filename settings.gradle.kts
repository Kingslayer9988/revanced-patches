rootProject.name = "revanced-patches"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/revanced/registry")
            credentials {
                username = providers.gradleProperty("gpr.user").getOrElse(System.getenv("GITHUB_ACTOR"))
                password = providers.gradleProperty("gpr.key").getOrElse(System.getenv("GITHUB_TOKEN"))
            }
        }
    }
}

plugins {
    id("revanced-patches-gradle-plugin") version "1.0.0-dev.7"
}

settings {
    extensions {
        defaultNamespace = "app.revanced.extension"

        proguardFiles("../proguard-rules.pro")
    }
}

include(":patches:stub")
