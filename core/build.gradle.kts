import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.maven.publish)
}

kotlin {
    explicitApi()

    compilerOptions {
        extraWarnings = true
        allWarningsAsErrors = true
        progressiveMode = true
    }

    jvmToolchain(21)

    jvm()
}

dependencies {
    commonMainImplementation(libs.kotlinx.coroutines)
}

group = "me.y9san9.store"
version = libs.versions.store.get()

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    pom {
        name = "store"
        description = "Make mutable objects safe!"
        url = "https://github.com/y9san9/store"

        licenses {
            license {
                name = "MIT"
                distribution = "repo"
                url = "https://github.com/y9san9/store/blob/main/LICENSE.md"
            }
        }

        developers {
            developer {
                id = "y9san9"
                name = "Alex Sokol"
                email = "y9san9@gmail.com"
            }
        }

        scm {
            connection = "scm:git:ssh://github.com/y9san9/store.git"
            developerConnection = "scm:git:ssh://github.com/y9san9/store.git"
            url = "https://github.com/y9san9/store"
        }
    }

    signAllPublications()
}
