package dev.mos.prom.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.mos.prom.R

private val Nunito = FontFamily(
    Font(R.font.nunito_light, weight = FontWeight.Light),
    Font(R.font.nunito_medium, weight = FontWeight.Medium),
    Font(R.font.nunito_bold, weight = FontWeight.Bold),
)

val MosTypography = Typography(
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
    bodyMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)
