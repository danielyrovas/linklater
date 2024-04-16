import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("org.yrovas.linklater")
        }
    }
}

android {
    namespace = libs.versions.app.versionID.get()
    compileSdk = libs.versions.app.compileSDK.get().toInt()

    defaultConfig {
        applicationId = libs.versions.app.versionID.get()
        minSdk = libs.versions.app.minimumSDK.get().toInt()
        targetSdk = libs.versions.app.targetSDK.get().toInt()
        versionCode = libs.versions.app.versionCode.get().toInt()
        versionName = libs.versions.app.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        debug { applicationIdSuffix = ".debug" }
    }
    compileOptions {
        val javaVersion = JavaVersion.valueOf(libs.versions.app.javaVersion.get())
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions { jvmTarget = libs.versions.app.kotlinJVMTarget.get() }
    buildFeatures { compose = true }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.app.composeCompiler.get()
    }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }

    applicationVariants.all { variant ->
        variant.outputs.all {
            (this as BaseVariantOutputImpl).outputFileName =
                "app-${variant.productFlavors[0].name}-${variant.buildType.name}-${variant.versionName}.apk"
        }
        true
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.destinations)
    implementation(libs.androidx.datastore.preferences)
    ksp(libs.destinations.ksp)

    ksp(libs.kotlin.inject.compiler.ksp)
    implementation(libs.kotlin.inject.runtime)

    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material)

    implementation(libs.ktor.client.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.logging.jvm)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.coroutines.extensions)
    implementation(libs.android.driver)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
