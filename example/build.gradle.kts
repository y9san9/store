plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    application
    alias(libs.plugins.ktlint)
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "me.y9san9.store.example.ExampleKt"
}

dependencies {
    implementation(projects.core)
    implementation(libs.kotlinx.coroutines)
}
