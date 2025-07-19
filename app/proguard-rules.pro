# SharedPreferences optimizasyonu
-keepclassmembers class * implements android.content.SharedPreferences {
    *;
}

# Compose optimizasyonları
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Performans optimizasyonları
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify