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
