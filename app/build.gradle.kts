plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.org.jetbrains.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.getwhatsnew"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.getwhatsnew"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    sourceSets {
        getByName("main") {
            java {
                srcDirs(
                    "src\\main\\java", "src\\main\\java\\data",
                    "src\\main\\java", "src\\main\\java\\ui", "src\\main\\java",
                    "src\\main\\java\\ui\\screen", "src\\main\\java",
                    "src\\main\\java\\res", "src\\main\\java",
                    "src\\main\\java\\ui\\component", "src\\main\\java",
                    "src\\main\\java\\data\\model", "src\\main\\java",
                    "src\\main\\java\\data\\repository"
                )
            }
        }
    }
}

dependencies {
    // 既存のライブラリ
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    implementation(libs.appcompat)

    // WebView のためのライブラリ
    implementation(libs.webkit)

    // HTTP通信（Retrofit + Gson）
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Coroutine（非同期処理）
    implementation(libs.kotlinx.coroutines.android)

    // Jetpack ViewModel（状態管理用）
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.ktx)

    // JSONパーサー（Gson）
    implementation(libs.gson)

    // Material3のPull-to-refresh機能
    implementation(libs.material3)
    implementation("androidx.compose.material:material:1.5.4")

    // Accompanistライブラリ
}
