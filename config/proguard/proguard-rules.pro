# Keep line numbers to make stacktraces readable.
-keepattributes SourceFile,LineNumberTable

# Iconics
-keepclassmembernames enum * implements com.mikepenz.iconics.typeface.IIcon { *; }

# MPAndroidChart
-keepclassmembers class com.github.mikephil.charting.animation.ChartAnimator {
    setPhaseY(...);
    setPhaseX(...);
}
