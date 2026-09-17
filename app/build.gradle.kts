plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.crashlytics)
    id ("kotlin-kapt")
    id ("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.passioagogo.market"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.passioagogo.market"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(

                )
            }
        }

    }

    buildTypes {
        /**
         * Depuración → SANDBOX.
         *
         * El applicationIdSuffix es lo que permite tener AMBAS apps
         * instaladas a la vez en el mismo teléfono: Android las trata como
         * aplicaciones distintas. Así se prueba una venta de mentira sin
         * desinstalar la app con la que se cobra de verdad.
         *
         * El nombre visible ("Agogo SANDBOX") vive en
         * src/debug/res/values/strings.xml, no en resValue: declararlo aquí
         * chocaría con el app_name de src/main y el build fallaría por
         * recurso duplicado.
         *
         * Si SANDBOX_URL no está definida en gradle.properties, cae en las
         * credenciales de producción: evita que el build falle, pero ojo,
         * estarías apuntando a datos reales desde debug.
         */
        debug {
            applicationIdSuffix = ".sandbox"
            versionNameSuffix = "-sandbox"

            buildConfigField(
                "String",
                "SUPABASE_URL",
                "\"${project.findProperty("SANDBOX_URL")
                    ?: project.findProperty("SUPABASE_URL") ?: ""}\""
            )
            buildConfigField(
                "String",
                "SUPABASE_KEY",
                "\"${project.findProperty("SANDBOX_KEY")
                    ?: project.findProperty("SUPABASE_KEY") ?: ""}\""
            )
            buildConfigField("Boolean", "ES_SANDBOX", "true")
        }

        /** Producción: los datos con los que se transacciona. */
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField(
                "String",
                "SUPABASE_URL",
                "\"${project.findProperty("SUPABASE_URL") ?: ""}\""
            )
            buildConfigField(
                "String",
                "SUPABASE_KEY",
                "\"${project.findProperty("SUPABASE_KEY") ?: ""}\""
            )
            buildConfigField("Boolean", "ES_SANDBOX", "false")
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "2.1.20"
    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
    }
}

dependencies {
    // ---------- Compose / Android base ----------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ---------- Hilt ----------
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    kapt(libs.hilt.compiler)

    // ---------- Room (caché de catálogo) ----------
    implementation(libs.androidx.room.ktx)
    kapt(libs.room.compiler)

    // ---------- Supabase 3.x + Ktor OkHttp ----------
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.functions)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)

    // ---------- Firebase (analytics + crashlytics) ----------
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)

    // ---------- Utilidades ----------
    implementation(libs.coil.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.exifinterface)

    // ---------- Punto de venta (escáner, fase posterior) ----------
    implementation(libs.barcode.scanning)
    val cameraxVersion = "1.3.0"
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // ---------- Testing ----------
    testImplementation(libs.bundles.testing)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
kapt {
    correctErrorTypes = true
}