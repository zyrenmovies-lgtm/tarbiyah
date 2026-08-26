# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /android-sdk/tools/proguard/proguard-android.txt

# Keep Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep app data classes
-keep class com.tarbiyah.ailearn.ui.feed.FeedPost { *; }
-keep class com.tarbiyah.ailearn.utils.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }
