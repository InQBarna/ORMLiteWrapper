# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/davidgarcia/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep interface com.inqbarna.inqorm.DataAccessor
-keep class com.inqbarna.inqorm.DataTool
-dontnote com.j256.ormlite.android.DatabaseTableConfigUtil
-keep class com.j256.ormlite.android.AndroidLog {
    <init>(java.lang.String);
    public <methods>;
}

-keep class com.j256.ormlite.logger.LoggerFactory$LogType {
    *;
}