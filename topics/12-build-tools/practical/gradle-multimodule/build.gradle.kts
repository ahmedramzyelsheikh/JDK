// Root build: configuration shared by all subprojects (like a Maven parent POM)
subprojects {
    apply(plugin = "java")

    group = "com.jdk.shop"
    version = "1.0.0-SNAPSHOT"

    repositories { mavenCentral() }

    extensions.configure<JavaPluginExtension> {
        toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    }

    tasks.withType<Test>().configureEach { useJUnitPlatform() }
}
