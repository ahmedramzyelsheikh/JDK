plugins {
    `java-library`                         // adds the 'api' configuration
}

dependencies {
    implementation(libs.commons.lang3)     // like Maven compile scope, but NOT leaked to consumers' compile classpath

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testRuntimeOnly(libs.junit.launcher)
}

// sources: reuse the Maven module's code so both builds compile the same files
sourceSets {
    main { java.srcDir("../../maven-multimodule/shop-core/src/main/java") }
    test { java.srcDir("../../maven-multimodule/shop-core/src/test/java") }
}
