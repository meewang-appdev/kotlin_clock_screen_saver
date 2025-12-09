package com.example.clockscreensaver.data

data class UserPreferences(
    val is24Hour: Boolean = true,
    val textColorHex: String = "#B08D57",
    val fontStyle: String = "default",
    val brightnessLevel: Int = 70,
    val burnInProtection: Boolean = true,
    val clockStyle: String = "minimal"
)
