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
    get() = if (isDarkThemeGlobal) Color(0xFF000000) else Color(0xFFF1F5F9)

val SlateCard: Color
    get() = if (isDarkThemeGlobal) Color(0xFF000000) else Color(0xFFFFFFFF)

val TealPrimary = Color(0xFF3B66F6)    // Royal Blue primary action / buttons
val MintAccent = Color(0xFF10B981)     // Original crisp tech green for positive balances/charts
val AmberWarning = Color(0xFFF59E0B)   // Tech warm warning amber
val AlertCoral = Color(0xFFEF4444)     // Clean alert coral red
val ElectricBlue = Color(0xFF3B66F6)   // Royal Blue accent

val OffWhite: Color
    get() = if (isDarkThemeGlobal) Color(0xFFFFFFFF) else Color(0xFF0F172A)

val SoftGray: Color
    get() = if (isDarkThemeGlobal) Color(0xFF9E9E9E) else Color(0xFF64748B)

val BorderSlate: Color
    get() = if (isDarkThemeGlobal) Color(0xFF1C1C24) else Color(0xFFE2E8F0)

