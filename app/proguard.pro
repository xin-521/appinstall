-keep class com.appinstall.** { *; }
-keepclassmembers class * {
    @dagger.hilt.InstallIn <init>(...);
}
