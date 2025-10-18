package dev.mos.prom.profile.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AchievementChip(label: String, background: Color) {
    Surface(
        color = background.copy(alpha = 0.2f),
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = label,
            color = background,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 13.sp
        )
    }
}
