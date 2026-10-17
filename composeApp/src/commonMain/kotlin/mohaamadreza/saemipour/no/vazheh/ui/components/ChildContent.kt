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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mohaamadreza.saemipour.no.vazheh.data.ChildDTO
import mohaamadreza.saemipour.no.vazheh.data.Gender
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MutedText
import mohaamadreza.saemipour.no.vazheh.ui.theme.cardBackground
import mohaamadreza.saemipour.no.vazheh.ui.theme.cardBackground3
import mohaamadreza.saemipour.no.vazheh.ui.theme.iconTint
import novazheh.composeapp.generated.resources.Res
import novazheh.composeapp.generated.resources.face_man_profile
import novazheh.composeapp.generated.resources.face_woman_profile
import org.jetbrains.compose.resources.painterResource

@Composable
fun ChildContent(
    child: ChildDTO,
    onClick: () -> Unit = {},
    onDeleteClick: (() -> Unit)? = null,
    isDeleteEnabled: Boolean = true,
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji based on gender
            val emoji = when (child.gender) {
                Gender.BOY -> Res.drawable.face_man_profile
                Gender.GIRL -> Res.drawable.face_woman_profile
            }
            
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(cardBackground3),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(emoji),
                    modifier = Modifier.size(24.dp),
                    contentDescription = null,
                    tint = iconTint
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Child name
            Text(
                text = child.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = DarkText,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            onDeleteClick?.let { onDelete ->
                Box(
                    modifier = Modifier
                        .background(
                            shape = CircleShape,
                            color = CoralRed.copy(alpha = 0.1f)
                        )
                        .clip(CircleShape)
                        .clickable(enabled = isDeleteEnabled, onClick = onDelete)
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف کودک",
                        tint = CoralRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Navigation arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "مشاهده جزئیات",
                tint = MutedText,
                modifier = Modifier.size(28.dp).rotate(180f)
            )
        }
    }
}
