plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.appcompat)

    // **追加したライブラリ**

    // WebView のためのライブラリ
    implementation(libs.androidx.webkit)

    // HTTP通信（Retrofit + Gson）
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Coroutine（非同期処理）
    implementation(libs.kotlinx.coroutines.android)

    // Jetpack ViewModel（状態管理用）
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // JSONパーサー（Gson）
    implementation(libs.gson)
}
