package mohaamadreza.saemipour.no.vazheh.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import mohaamadreza.saemipour.no.vazheh.data.CustomWordDTO
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MutedText
import mohaamadreza.saemipour.no.vazheh.ui.theme.cardBackground
import mohaamadreza.saemipour.no.vazheh.ui.theme.cardBackground3

@Composable
fun CustomWordContent(
    word: CustomWordDTO,
    isPlaying: Boolean = false,
    onPlayClick: (audioUrl: String) -> Unit = {},
    onStopClick: () -> Unit = {},
    onDeleteClick: (wordId: Int) -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(1.dp, color = MutedText.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if(!word.imageUrl.isNullOrBlank()) {
                val platformContext = LocalPlatformContext.current
                AsyncImage(
                    model = ImageRequest.Builder(platformContext)
                        .data(word.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "تصویر انتخاب شده",
                    modifier = Modifier.size(45.dp).clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))
            }

            // Word name
            Text(
                text = word.wordFa,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = DarkText,
                modifier = Modifier.weight(1f)
            )

            // Play audio button
            if (word.audioUrl.isNotBlank()) {
                Box(
                    modifier = Modifier.background(
                        shape = CircleShape,
                        color = cardBackground3
                    ).clip(CircleShape).clickable{
                        if (isPlaying) {
                            onStopClick()
                        } else {
                            onPlayClick(word.audioUrl)
                        }
                    }.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        // Stop icon (square)
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(MutedText, RoundedCornerShape(4.dp))
                        )
                    } else {
                        // Play icon (triangle using text)
                        Text(
                            text = "▶",
                            fontSize = 24.sp,
                            color = MutedText,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete button
            Box(
                modifier = Modifier
                    .background(
                        shape = CircleShape,
                        color = CoralRed.copy(alpha = 0.1f)
                    )
                    .clip(CircleShape)
                    .clickable { onDeleteClick(word.id) }
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف کلمه",
                    tint = CoralRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}