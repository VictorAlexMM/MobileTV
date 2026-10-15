plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
}

android {
    namespace = "com.iptvplayer.app"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.iptvplayer.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
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
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = false
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)

  // Media3 (ExoPlayer, HLS, DASH, UI, OkHttp DataSource, Session)
  implementation(libs.media3.exoplayer)
  implementation(libs.media3.exoplayer.hls)
  implementation(libs.media3.exoplayer.dash)
  implementation(libs.media3.datasource.okhttp)
  implementation(libs.media3.ui)
  implementation(libs.media3.session)

  // Room Database
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.paging)
  ksp(libs.androidx.room.compiler)

  // Paging 3
  implementation(libs.androidx.paging.runtime)
  implementation(libs.androidx.paging.compose)

  // Networking & Serialization
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.kotlinx.serialization)
  implementation(libs.okhttp)
  implementation(libs.okhttp.logging)
  implementation(libs.kotlinx.serialization.json)

  // Coil (Image loading)
  implementation(libs.coil.compose)

  // Background Work & Preferences
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.security.crypto)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)
}

val apkMobileDir = layout.projectDirectory.dir("../apk/mobile")
val apkTvDir = layout.projectDirectory.dir("../apk/tv")

tasks.register<Copy>("copyMobileApk") {
  description = "Copia o APK gerado para a pasta apk/mobile"
  dependsOn("packageDebug")
  from(layout.buildDirectory.dir("outputs/apk/debug"))
  include("app-debug.apk")
  into(apkMobileDir)
  rename("app-debug.apk", "MobileTV-Mobile-debug.apk")
}

tasks.register<Copy>("copyTvApk") {
  description = "Copia o APK gerado para a pasta apk/tv"
  dependsOn("packageDebug")
  from(layout.buildDirectory.dir("outputs/apk/debug"))
  include("app-debug.apk")
  into(apkTvDir)
  rename("app-debug.apk", "MobileTV-TV-debug.apk")
}

tasks.register("copyAllApks") {
  description = "Copia os APKs para as pastas apk/mobile e apk/tv"
  dependsOn("copyMobileApk", "copyTvApk")
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
  finalizedBy("copyAllApks")
}



