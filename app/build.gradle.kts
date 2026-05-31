plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)

    id("com.google.gms.google-services") // Plugin xử lý cấu hình Firebase chuẩn
}

android {
    namespace = "com.example.pokedex"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pokedex"
        minSdk = 31
        targetSdk = 36
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
        viewBinding = true // Đã bật ViewBinding để dùng `binding.xxx` ngon lành
    }
}

dependencies {
    // AndroidX & Material Design UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)

    implementation("androidx.lifecycle:lifecycle-process:2.6.2")

    // Cấu hình Firebase bằng BoM (Giúp tự động đồng bộ version, tránh xung đột)
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-auth-ktx") // Không cần điền version thủ công nữa

    // Credential Manager (Đăng nhập Google)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Xử lý mạng & hình ảnh Pokemon
    implementation("com.github.bumptech.glide:glide:5.0.7")
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)

    // Tiện ích mở rộng cho Fragment/Activity (Hỗ trợ viewModels())
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.activity:activity-ktx:1.8.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.firebase:firebase-database-ktx")
}