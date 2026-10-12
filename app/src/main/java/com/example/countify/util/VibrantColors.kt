package com.example.countify.util

import kotlin.random.Random

object VibrantColors {
    val BACKGROUND_COLORS = listOf(
        0xFFFF3B30.toInt(), // Vibrant Red
        0xFFFF9500.toInt(), // Vibrant Orange
        0xFFFFCC00.toInt(), // Vibrant Amber/Yellow
        0xFF34C759.toInt(), // Vibrant Green
        0xFF00C7BE.toInt(), // Vibrant Teal
        0xFF30B0C7.toInt(), // Cyan
        0xFF007AFF.toInt(), // Vibrant Blue
        0xFF5856D6.toInt(), // Indigo
        0xFFAF52DE.toInt(), // Vibrant Purple
        0xFFFF2D55.toInt(), // Hot Pink
        0xFF00E676.toInt(), // Bright Emerald
        0xFFFF6D00.toInt(), // Deep Orange
        0xFF2979FF.toInt(), // Bright Royal Blue
        0xFF1DE9B6.toInt()  // Mint Turquoise
    )

    val TEXT_COLORS = listOf(
        0xFFFFFFFF.toInt(), // Pure White
        0xFF000000.toInt(), // Pure Black
        0xFFFFEB3B.toInt(), // Bright Yellow
        0xFF00E5FF.toInt(), // Bright Cyan
        0xFFAEEA00.toInt(), // Lime Accent
        0xFFFF4081.toInt(), // Accent Pink
        0xFFFF9100.toInt(), // Bright Orange
        0xFF212121.toInt()  // Dark Charcoal
    )

    fun getRandomVibrantColor(): Int {
        return BACKGROUND_COLORS[Random.nextInt(BACKGROUND_COLORS.size)]
    }
}
