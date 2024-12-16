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
<<<<<<< HEAD
    id("app.revanced.patches") version "1.0.0-dev.7"
=======
    id("app.revanced-patches") version "1.0.0-dev.7"
>>>>>>> c8c6b1c6a8fd5678aefc8ff7839780b08ee8083d
}

settings {
    extensions {
        defaultNamespace = "app.revanced.extension"

        proguardFiles("../proguard-rules.pro")
    }
}

include(":patches:stub")
