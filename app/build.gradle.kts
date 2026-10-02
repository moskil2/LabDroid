import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val versionPropertiesFile = file("version.properties")
val versionProperties = Properties().apply {
    versionPropertiesFile.inputStream().use { load(it) }
}
val appVersionMajor = versionProperties.getProperty("versionMajor").toInt()
val appVersionMinor = versionProperties.getProperty("versionMinor").toInt()
val appVersionPatch = versionProperties.getProperty("versionPatch").toInt()
val appVersionCode = versionProperties.getProperty("versionCode").toInt()
val appVersionName = "$appVersionMajor.$appVersionMinor.$appVersionPatch"
val projectRootDir = rootProject.projectDir.parentFile

android {
    namespace = "com.labdroid.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.labdroid.app"
        minSdk = 26
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        val buildStamp = SimpleDateFormat("yyyyMMdd.HHmm").format(Date())
        buildConfigField("String", "BUILD_STAMP", "\"$buildStamp\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    applicationVariants.all {
        val variant = this
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = "LabDroid_v$appVersionName.apk"
        }
    }
}

tasks.register("incrementVersionPatch") {
    doLast {
        val props = Properties()
        versionPropertiesFile.inputStream().use { props.load(it) }
        val newPatch = props.getProperty("versionPatch").toInt() + 1
        val newCode = props.getProperty("versionCode").toInt() + 1
        props.setProperty("versionPatch", newPatch.toString())
        props.setProperty("versionCode", newCode.toString())
        versionPropertiesFile.outputStream().use { props.store(it, null) }
        println("Version bumped to $appVersionMajor.$appVersionMinor.$newPatch (code $newCode) for next build")
    }
}

tasks.register("copyApkToProjectRoot") {
    doLast {
        val apkDir = layout.buildDirectory.dir("outputs/apk/debug").get().asFile
        apkDir.listFiles { candidate -> candidate.extension == "apk" }?.forEach { apk ->
            apk.copyTo(File(projectRootDir, apk.name), overwrite = true)
        }
    }
    finalizedBy("incrementVersionPatch")
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
    finalizedBy("copyApkToProjectRoot")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.windowsizeclass)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.play.services.location)

    implementation(libs.vico.compose.m3)

    implementation(libs.androidx.documentfile)
}
