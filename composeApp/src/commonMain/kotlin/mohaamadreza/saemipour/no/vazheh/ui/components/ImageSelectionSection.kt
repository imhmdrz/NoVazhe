package mohaamadreza.saemipour.no.vazheh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.ismoy.imagepickerkmp.domain.models.GalleryPhotoResult
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MutedText
import mohaamadreza.saemipour.no.vazheh.ui.theme.SoftGray
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealPurple

@Composable
fun ImageSelectionSection(
    selectedImage: GalleryPhotoResult?,
    onSelectImage: () -> Unit,
    onDeleteImage: () -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "تصویر کلمه",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            // فیلد الزامی
            Text(
                text = " *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CoralRed
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (selectedImage != null) {
                    // Show selected image
                    ImageSelectedContent(
                        uri = selectedImage.uri,
                        onDeleteImage = onDeleteImage,
                        onChangeImage = onSelectImage,
                        enabled = enabled
                    )
                } else {
                    // Show select image prompt
                    SelectImageContent(
                        onSelectImage = onSelectImage,
                        enabled = enabled
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectImageContent(
    onSelectImage: () -> Unit,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (enabled) TealPurple.copy(alpha = 0.1f) else SoftGray,
                    CircleShape
                )
                .clip(CircleShape)
                .clickable(enabled = enabled, onClick = onSelectImage),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "آپلود تصویر کلمه",
                modifier = Modifier.size(36.dp),
                tint = if (enabled) TealPurple else MutedText
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "آپلود تصویر کلمه",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedText
        )
    }
}

@Composable
private fun ImageSelectedContent(
    uri: String?,
    onDeleteImage: () -> Unit,
    onChangeImage: () -> Unit,
    enabled: Boolean
) {
    val platformContext = LocalPlatformContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display selected image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = SoftGray.copy(alpha = 0.3f)
            )
        ) {
            if (uri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(platformContext)
                        .data(uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "تصویر انتخاب شده",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                // Fallback if decoding fails
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "خطا در نمایش تصویر",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "تصویر با موفقیت انتخاب شد",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TealPurple
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Change image button
            Button(
                onClick = onChangeImage,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPurple.copy(alpha = 0.1f),
                    contentColor = TealPurple
                ),
                enabled = enabled
            ) {
                Text(
                    text = "تغییر تصویر",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Delete image button
            Button(
                onClick = onDeleteImage,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralRed.copy(alpha = 0.1f),
                    contentColor = CoralRed
                ),
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "حذف",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
