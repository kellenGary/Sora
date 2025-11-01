plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.example.sora"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.sora"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["appAuthRedirectScheme"] = "com.example.sora"
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = project.findProperty("GOOGLE_MAPS_API_KEY") ?: ""
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add BuildConfig fields for Supabase
        buildConfigField("String", "SUPABASE_URL", "\"${project.findProperty("SUPABASE_URL") ?: ""}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${project.findProperty("SUPABASE_ANON_KEY") ?: ""}\"")

        // Add BuildConfig field for Spotify
        buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"${project.findProperty("SPOTIFY_CLIENT_ID") ?: ""}\"")

        // Add BuildConfig field for Google Maps
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"${project.findProperty("GOOGLE_MAPS_API_KEY") ?: ""}\"")
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
        viewBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtension.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat) // Keep for theme resources (Theme.Sora)
    implementation(libs.google.android.material)
    implementation(libs.appauth)
    implementation(libs.androidx.navigation.compose)

    // Compose dependencies
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // Import the BOM
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview)

    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // Google Maps dependencies
    implementation(libs.play.services.maps)
    implementation(libs.google.maps.compose)

    // Supabase dependencies
    implementation(platform("io.github.jan-tennert.supabase:bom:2.6.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("io.ktor:ktor-client-android:2.3.11")
    implementation(libs.coil.kt.coil.compose)

    // Tooling for previews and debugging
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}