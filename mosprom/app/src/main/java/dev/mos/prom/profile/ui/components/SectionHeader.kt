package dev.mos.prom.profile.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SectionHeader(title: String) {
    Spacer(Modifier.height(20.dp))

    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            color = androidx.compose.ui.graphics.Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
    )

    Spacer(Modifier.height(4.dp))
}
