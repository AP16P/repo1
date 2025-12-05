pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.github.com/JorenSix/TarsosDSP") }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/JorenSix/TarsosDSP")
            credentials {
                username = providers.gradleProperty("gpr.user").orElse(System.getenv("USERNAME")).get()
                password = providers.gradleProperty("gpr.key").orElse(System.getenv("TOKEN")).get()
            }
        }
    }
}

rootProject.name = "voice"
include(":app")