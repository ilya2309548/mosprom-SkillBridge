package dev.mos.prom.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.mos.prom.R

// Expose Nunito so Theme can force a default text style when needed
val Nunito = FontFamily(
    Font(R.font.nunito_light, weight = FontWeight.Light),
    Font(R.font.nunito_medium, weight = FontWeight.Medium),
    Font(R.font.nunito_bold, weight = FontWeight.Bold),
)

// Apply Nunito across all Material3 text styles
val MosTypography = Typography(
    // Display
    displayLarge = TextStyle(fontFamily = Nunito),
    displayMedium = TextStyle(fontFamily = Nunito),
    displaySmall = TextStyle(fontFamily = Nunito),

    // Headline
    headlineLarge = TextStyle(fontFamily = Nunito),
    headlineMedium = TextStyle(fontFamily = Nunito),
    headlineSmall = TextStyle(fontFamily = Nunito),

    // Title
    titleLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Medium,
    ),

    // Body
    bodyLarge = TextStyle(fontFamily = Nunito),
    bodyMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(fontFamily = Nunito),

    // Label
    labelLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Medium),
)
