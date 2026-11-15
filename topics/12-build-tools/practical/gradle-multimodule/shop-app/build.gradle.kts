plugins {
    application                            // adds the 'run' task and distribution zips
}

dependencies {
    implementation(project(":shop-core"))  // module-to-module dependency
}

application {
    mainClass.set("com.jdk.shop.app.ShopApp")
}

sourceSets {
    main { java.srcDir("../../maven-multimodule/shop-app/src/main/java") }
}
