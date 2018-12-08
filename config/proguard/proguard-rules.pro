# Keep info required to make stacktraces readable.
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Iconics
-keepclassmembernames enum * implements com.mikepenz.iconics.typeface.IIcon { *; }

# MPAndroidChart
-keepclassmembers class com.github.mikephil.charting.animation.ChartAnimator {
    setPhaseY(...);
    setPhaseX(...);
}
