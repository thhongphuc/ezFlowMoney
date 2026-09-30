# Proguard rules for AntiBudget
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
