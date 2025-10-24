plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
}

// 保留路径打印任务（通过任务打印，更安全）
tasks.register("printCmakePath") {
    doLast {
        val cmakeConfig = android.externalNativeBuild.cmake
        val cmakePath = cmakeConfig.path
        println("CMake 配置路径（绝对路径）: ${cmakePath?.absolutePath ?: "路径未设置"}")
    }
}


android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildVersion.get()

    // 合并后的 externalNativeBuild（只保留一个，指向 ai-native-sdk）
    externalNativeBuild {
        cmake {
            // 1. 先定义局部常量存储路径，再赋值给 path（避免智能转换错误）
            val sdkCmakePath = file("../../ai-native-sdk/CMakeLists.txt")
            path = sdkCmakePath
            version = "3.22.1"
            // 可选：如果需要在这里打印，用局部常量访问属性
            println("AI-Native-SDK CMake 路径: ${sdkCmakePath.absolutePath}")
        }
    }


    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")

        // 原生构建的编译参数配置（与顶层 externalNativeBuild 职责不同，保留）
        externalNativeBuild {
            cmake {
                arguments("-DANDROID_STL=c++_shared", "-DANDROID_TOOLCHAIN=clang")
            }
        }

        ndk {
            abiFilters.addAll(arrayOf("arm64-v8a", "armeabi-v7a"))
        }
        ndkVersion = "25.2.9519653"
    }

    buildTypes {

        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")

        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // 👇 已删除重复的 externalNativeBuild 配置（之前指向 src/main/cpp 的无效配置）

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")
            res {
                srcDirs("src/main/res")
            }
        }
    }
    namespace = "com.noetix"
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar", "*.aar"), "dir" to "libs")))
    implementation(libs.material)

    api("org.apache.commons:commons-csv:1.9.0")
    implementation(libs.kotlinxcoroutines)
    implementation(libs.viewmodel)
    implementation(libs.lifecycle)

    api(libs.okhttp)
    api(libs.logginginterceptor)

    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.convertergson)
    implementation(libs.adapterrxjava)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.github.tonyofrancis.Fetch:fetch2:3.4.1")

    api("io.github.jeremyliao:live-event-bus-x:1.8.0")
    api("io.github.jeremyliao:leb-processor-gson:1.8.0")

    implementation("com.umeng.umsdk:common:9.6.3")
    implementation("com.umeng.umsdk:asms:1.8.0")
    implementation("com.umeng.umsdk:apm:+")
    implementation(libs.androidx.appcompat)
}