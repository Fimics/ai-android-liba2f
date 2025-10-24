import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    compileSdk = 35
    namespace = "com.libnoetix.demo"
    defaultConfig {
        applicationId = "com.libnoetix.demo"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
        ndk {
            abiFilters.addAll(arrayOf("arm64-v8a"))
        }
    }

    signingConfigs {
        create("keyStore") {
            storeFile = file("../main/platform.jks")
            keyPassword = "android"
            keyAlias = "androidplatformkey"
            storePassword = "android"
        }
    }

    buildTypes {
        val signConfig = signingConfigs.getByName("keyStore")

        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signConfig
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isZipAlignEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signConfig
        }
    }

    fun getGitCommit(): String {
        val process = ProcessBuilder("git", "rev-parse", "HEAD")
            .start()
        process.waitFor()
        return process.inputStream.bufferedReader().readText().trim()
    }

    // 获取当前 Git commit 的 SHA-1 标识
    val gitCommit = getGitCommit()
    println("Git commit: $gitCommit")

    // 输出类型
    android.applicationVariants.all {
        // 编译类型
        val buildType = this.buildType.name
        print("buildType name -> $buildType")
        var vName = this.versionName
        val date = SimpleDateFormat("yyyyMMddHHmm").format(Date())
        outputs.all {
            // 判断是否是输出 apk 类型
            if (this is ApkVariantOutputImpl) {
                this.outputFileName = flavorName +
                        "_${vName}_${date}_${buildType}.apk"
            }
        }
    }

    flavorDimensions += listOf("terminalType")
    productFlavors {
        create("demo") {
        }
    }

    packagingOptions {
        packagingOptions {
            pickFirst("lib/arm64-v8a/libc++_shared.so")
            // 如果需要，也可以为其他ABI添加
            pickFirst("lib/armeabi-v7a/libc++_shared.so")
            pickFirst("lib/x86/libc++_shared.so")
            pickFirst("lib/x86_64/libc++_shared.so")
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }
    buildFeatures {
        viewBinding = true
    }


}

dependencies {
    implementation(fileTree(mapOf("includes" to listOf("*.aar", "*.jar"), "dir" to "libs")))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Speech Engine

    implementation(libs.gson)
    implementation(project(":libNoetix"))

}
