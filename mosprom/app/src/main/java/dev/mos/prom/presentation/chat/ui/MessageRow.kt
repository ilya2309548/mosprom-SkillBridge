package dev.mos.prom.presentation.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.mos.prom.utils.placeholderPainter

@Composable
fun MessageRow(
    avatarUrl: String?,
    author: String,
    text: String,
    time: String,
    isOutgoing: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        if (!isOutgoing) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = null,
                placeholder = placeholderPainter(),
                error = placeholderPainter(),
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(8.dp))
        } else {
            Spacer(Modifier.weight(1f))
        }

        Column(
            modifier = Modifier
                .background(
                    if (isOutgoing) MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .weight(3f, fill = false)
        ) {
            if (author.isNotBlank() && !isOutgoing) {
                Text(author, style = MaterialTheme.typography.labelSmall, color = Color.Black.copy(alpha = 0.7f))
            }
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isOutgoing) Color.White else Color.Black,
                fontWeight = if (isOutgoing) FontWeight.Medium else FontWeight.Normal
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.weight(1f))
                Text(time, style = MaterialTheme.typography.labelSmall, color = if (isOutgoing) Color.White.copy(0.8f) else Color.Black.copy(0.6f))
            }
        }

        if (!isOutgoing) {
            Spacer(Modifier.weight(1f))
        }
    }
}