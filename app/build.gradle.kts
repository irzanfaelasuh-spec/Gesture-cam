import java.net.URL

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.gestra.camera"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.gestra.camera"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-demo"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE*",
            "META-INF/NOTICE*"
        )
    }
}

val handModel = layout.projectDirectory.file("src/main/assets/hand_landmarker.task").asFile
val downloadHandModel by tasks.registering {
    outputs.file(handModel)
    doLast {
        if (!handModel.exists() || handModel.length() < 1_000_000L) {
            handModel.parentFile.mkdirs()
            val source = URL("https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task")
            source.openConnection().apply {
                connectTimeout = 30_000
                readTimeout = 120_000
                setRequestProperty("User-Agent", "GESTRA-Android-Build/1.0")
            }.getInputStream().use { input ->
                handModel.outputStream().use { output -> input.copyTo(output) }
            }
        }
        check(handModel.exists() && handModel.length() > 1_000_000L) {
            "MediaPipe hand model is missing or incomplete. Run scripts/download-model.sh and retry."
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn(downloadHandModel)
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-ktx:1.11.0")

    val cameraX = "1.6.2"
    implementation("androidx.camera:camera-core:$cameraX")
    implementation("androidx.camera:camera-camera2:$cameraX")
    implementation("androidx.camera:camera-lifecycle:$cameraX")
    implementation("androidx.camera:camera-view:$cameraX")

    implementation("com.google.mediapipe:tasks-vision:0.10.35")
}
