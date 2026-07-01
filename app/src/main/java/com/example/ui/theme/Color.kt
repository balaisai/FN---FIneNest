package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import android.content.Context

var themePreferenceGlobal by mutableStateOf("System")
var isDarkThemeGlobal by mutableStateOf(false)

fun initThemePreference(context: Context) {
    val prefs = context.getSharedPreferences("ui_theme_prefs", Context.MODE_PRIVATE)
    themePreferenceGlobal = prefs.getString("selected_theme", "System") ?: "System"
}

fun updateThemePreference(context: Context, newTheme: String) {
    themePreferenceGlobal = newTheme
    val prefs = context.getSharedPreferences("ui_theme_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("selected_theme", newTheme).apply()
}

// Premium Athletic Black & Royal Blue Aesthetic
val SlateDark: Color
    get() = Color(0xFF000000) // Pure solid OLED black background always

val SlateCard: Color
    get() = Color(0xFF000000) // All panels are premium black as requested

val TealPrimary = Color(0xFF3B66F6)    // Royal Blue primary action / buttons
val MintAccent = Color(0xFF10B981)     // Original crisp tech green for positive balances/charts
val AmberWarning = Color(0xFFF59E0B)   // Tech warm warning amber
val AlertCoral = Color(0xFFEF4444)     // Clean alert coral red
val ElectricBlue = Color(0xFF3B66F6)   // Royal Blue accent

val OffWhite: Color
    get() = Color(0xFFFFFFFF) // High-contrast crisp white text

val SoftGray: Color
    get() = Color(0xFF9E9E9E) // Muted slate gray for secondary labels

val BorderSlate: Color
    get() = Color(0xFF1C1C24) // Subtle onyx border around panels

