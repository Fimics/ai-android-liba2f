pluginManagement {
    repositories {

        // 阿里云镜像
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://www.jitpack.io") }
        maven { url =uri("https://artifact.bytedance.com/repository/Volcengine/") }

        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {

        // 阿里云镜像
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://www.jitpack.io") }
        maven { url =uri("https://artifact.bytedance.com/repository/Volcengine/") }
        // 添加 fetch2 所需的仓库
        maven { url = uri("https://androidx.dev/storage/compose-compiler/repository/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        google()
        mavenCentral()

    }
}


rootProject.name = "ai-android-liba2f"
include(":main")
include(":libNoetix")



//android 项目 ai-android-liba2f 和c++ 项目ai-native-sdk 在同一目录下，ai-android-liba2f 下的module (libNoetix) 通过怎么下gradle.kts 引用ai-native-sdk 源码，
// 可以编译通过，但怎么在libNoetix 显示ai-native-sdk源码，怎么debug ai-native-sdk的源码

// 添加 ai-native-sdk 作为源码依赖
include(":ai-native-sdk")
project(":ai-native-sdk").projectDir = file("../ai-native-sdk")