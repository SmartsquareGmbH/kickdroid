# Retrofit
-keepattributes Signature, InnerClasses

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit

# Moshi
-keep @com.squareup.moshi.JsonQualifier interface *
-keepnames @com.squareup.moshi.JsonClass class *

-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}

-keep class **JsonAdapter {
    <init>(...);
    <fields>;
}
