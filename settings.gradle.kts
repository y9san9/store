enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "store"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(
    "core",
    "example",
)

