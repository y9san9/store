plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.maven.publish) apply false
}

tasks {
    val printVersion by registering {
        group = "CI"

        doFirst {
            println(libs.versions.store.get())
        }
    }
}
