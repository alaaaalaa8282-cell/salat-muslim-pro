-keep class com.alaa.** { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.* <methods>;
}
-dontwarn kotlin.**
-dontwarn kotlinx.**
