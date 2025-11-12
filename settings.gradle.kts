rootProject.name = "IGambling"

include(":shared")
include(":syncGameJob")
include(":proto")
include(":client-api")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
