plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildVersion.get()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")

        externalNativeBuild {
            cmake {
                arguments("-DANDROID_STL=c++_shared", "-DANDROID_TOOLCHAIN=clang")
            }
        }
        ndk {
//            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
//             abiFilters.addAll(arrayOf("armeabi-v7a", "arm64-v8a", "x86_64"))
//            if (isArm64){
            abiFilters.addAll(arrayOf("arm64-v8a","armeabi-v7a"))
//            abiFilters.addAll(arrayOf("armeabi-v7a"))
//            }else{
//                abiFilters.addAll(arrayOf("armeabi-v7a"))
//            }
        }

        ndkVersion = "25.2.9519653"
    }

    buildTypes {

        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")

            externalNativeBuild {
                cmake {
                    arguments("-DANDROID_STL=c++_shared", "-DANDROID_TOOLCHAIN=clang")
                }
            }
        }

        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }


    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }



    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs") // Correct Kotlin DSL syntax
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
//    implementation(libs.librecyclerview)

    api("org.apache.commons:commons-csv:1.9.0")
    implementation(libs.kotlinxcoroutines)
    //lifecycle view model
    implementation(libs.viewmodel)
    implementation(libs.lifecycle)

    //okhttp
    api(libs.okhttp)
    api(libs.logginginterceptor)

    //gson
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.convertergson)
    implementation(libs.adapterrxjava)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.github.tonyofrancis.Fetch:fetch2:3.4.1")

//    implementation("io.github.azhon:appupdate:4.3.6")
    //event bus
    api("io.github.jeremyliao:live-event-bus-x:1.8.0")
    api("io.github.jeremyliao:leb-processor-gson:1.8.0")

    //umeng
    implementation("com.umeng.umsdk:common:9.6.3")
    implementation("com.umeng.umsdk:asms:1.8.0")
    implementation("com.umeng.umsdk:apm:+")
    implementation(libs.androidx.appcompat)


}
