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
// 添加对 ai-native-sdk 项目的包含
//includeBuild("../ai-native-sdk")  // 假设 ai-native-sdk 在 ai-android-liba2f 的上一级目录



