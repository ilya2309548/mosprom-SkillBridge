package dev.mos.prom.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MosPromTopBar (
    title: String,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    navIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
            .systemBarsPadding()
            .padding(start = 8.dp, end = 8.dp, top = 4.dp)
    ) {
        // Title по центру
        Text(
            text = title,
            fontWeight = FontWeight.W400,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.inverseOnSurface,
            modifier = Modifier.align(Alignment.Center)
        )

        // Навигационная иконка слева
        navIcon?.let {
            Box(modifier = Modifier.align(Alignment.CenterStart)) {
                it()
            }
        }

        // Actions справа
        actions?.let {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                it()
            }
        }
    }
}
