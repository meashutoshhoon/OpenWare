import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktfmt.gradle)
    id("kotlin-parcelize")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")

val baseVersionName = currentVersion.name

android {
    compileSdk = 36

    if (keystorePropertiesFile.exists()) {
        val keystoreProperties = Properties()
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
        signingConfigs {
            create("githubPublish") {
                keyAlias = keystoreProperties["keyAlias"].toString()
                keyPassword = keystoreProperties["keyPassword"].toString()
                storeFile = file(keystoreProperties["storeFile"]!!)
                storePassword = keystoreProperties["storePassword"].toString()
            }
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "jb.openware.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1

        versionName = baseVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("githubPublish")
            }
        }
        debug {
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("githubPublish")
            }
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_name", "OpenWare Debug")
        }
    }
    flavorDimensions += "publishChannel"

    productFlavors {
        create("generic") {
            dimension = "publishChannel"
            isDefault = true
        }

        create("githubPreview") {
            dimension = "publishChannel"
            applicationIdSuffix = ".preview"
            resValue("string", "app_name", "OpenWare Preview")
        }
    }

    lint { disable.addAll(listOf("MissingTranslation", "ExtraTranslation", "MissingQuantity")) }

    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "OpenWare-${defaultConfig.versionName}-${name}.apk"
        }
    }

    kotlin {
        compilerOptions {
            optIn.add("kotlin.RequiresOptIn")
        }
    }

    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }

    namespace = "jb.openware.app"
}

ktfmt { kotlinLangStyle() }

kotlin { jvmToolchain(21) }

dependencies {
    implementation(project(":imageViewer"))

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    //Other
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.drawerlayout)

    //Swipe refresh layout
    implementation(libs.swiperefreshlayout)

    // Shimmer effect
    implementation(libs.shimmer)

    // Lottie animation
    implementation(libs.lottie)

    // File picker
    implementation(libs.activity.compose)

    // ViewPager2
    implementation(libs.viewpager2)

    // Glide
    implementation(libs.glide)

    // Tools
    implementation(libs.gson)
    implementation(libs.okhttp)

    // Round ImageView
    implementation(libs.roundedimageview)

    // Volley
    implementation(libs.volley)

    // CircleImageView
    implementation(libs.circleimageview)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // Google Admob
    implementation(libs.bundles.admob)

    // MMKV(ultra fast storage)
    implementation(libs.mmkv)

    // SafetyNet
    implementation(libs.play.services.safetynet)
}