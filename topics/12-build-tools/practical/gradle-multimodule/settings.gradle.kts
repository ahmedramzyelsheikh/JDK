// Gradle equivalent of ../maven-multimodule (reference: Gradle is not run by this repository's CI).
// Run with Gradle 9 installed:  gradle build   |   gradle :shop-app:run
rootProject.name = "shop"

include("shop-core", "shop-app")
