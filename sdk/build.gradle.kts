plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

android {
    namespace = "com.webuild.statusbar"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
        targetSdk = 36
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "consumer-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    api(libs.appcompat)
    api(libs.core)
}

publishing {
    publications {
        create<MavenPublication>("release") {
            val repo = System.getenv("GITHUB_REPOSITORY") ?: ""
            val owner = repo.substringBefore("/")
            groupId = if (owner.isNotEmpty()) "com.github.$owner" else "com.webuild"
            artifactId = "statusbar-sdk"
            version = System.getenv("VERSION_NAME") ?: "0.1.0"
            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            val repo = System.getenv("GITHUB_REPOSITORY") ?: ""
            url = uri("https://maven.pkg.github.com/$repo")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
