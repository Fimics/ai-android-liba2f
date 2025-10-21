# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 仅保留 IRobotSDKManager 接口及其所有成员
-keep interface com.noetix.libnoetix.IRobotSDKManager {
    *; # 保留接口的所有方法
}


-keep interface com.noetix.libnoetix.Callback {
    *; # 保留接口的所有方法
}

-keep interface com.noetix.libnoetix.AngleReset {
    *; # 保留接口的所有方法
}

-keep class com.noetix.libnoetix.FacialExpressionMap {
    *;
}

# 保留所有可能通过反射调用的方法
-keepclassmembers class com.noetix.libnoetix.FacialExpressionMap {
    public static java.util.Map createExpressionMap();
    public static final ** *;
}

# 保留所有表达式键名字段（防止字符串匹配失效）
-keepclassmembers class com.noetix.libnoetix.FacialExpressionMap {
    static final java.util.Map <fields>;
}

-keep class com.noetix.libnoetix.FramePair {
    *; # 保留接口的所有方法
}

-keep class com.noetix.libnoetix.NoetixJNI$Audio2FaceCallback {
    public void onResult(float[], float[],int);
}

-keep class com.noetix.libnoetix.TTSHelper {
    *;
}

# 保留 LiveFaceService 类及其所有成员
-keep class com.noetix.libnoetix.LiveFaceService {
    *;  # 保留所有成员（字段/方法/内部类）
}

# 特别保留单例 Holder 内部类
-keep class com.noetix.libnoetix.LiveFaceService$Holder {
    *;
}

# 保留匿名内部类（如 consumerTask 的实现）
-keep class com.noetix.libnoetix.LiveFaceService$* {
    *;
}

# 保留所有原生方法接口（确保 JNI 调用正常）
-keep class com.noetix.libnoetix.INativeApi {
    *;
}
-keep class com.noetix.libnoetix.TTSHelper { *; }


# 强烈推荐：保留必要的Android组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View

# 保留Parcelable序列化
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# 保留JNI方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留枚举值方法
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留Kotlin元数据
-keep class kotlin.Metadata { *; }
-keep class com.noetix.libnoetix.utils.P { *; }

