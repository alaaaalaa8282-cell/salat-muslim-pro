# Adhan library
-keep class com.batoulapps.adhan.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Koin
-keepnames class androidx.lifecycle.ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel { <init>(...); }

# Retrofit
-keepattributes Exceptions
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Data models
-keep class com.alaa.data.model.** { *; }
-keep class com.alaa.utils.Constants { *; }

# Compose
-keep class androidx.compose.** { *; }
