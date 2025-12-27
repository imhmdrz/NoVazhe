package mohaamadreza.saemipour.no.vazheh.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MutedText
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealPurple
import novazheh.composeapp.generated.resources.Res
import novazheh.composeapp.generated.resources.audio
import org.jetbrains.compose.resources.painterResource

@Composable
fun AudioRecordingSection(
    isRecording: Boolean,
    hasRecording: Boolean,
    isProcessingAudio: Boolean,
    isPlaying: Boolean,
    microphonePermissionGranted: Boolean,
    showPermissionDenied: Boolean,
    onRequestPermission: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onDeleteRecording: () -> Unit,
    onPlayRecording: () -> Unit,
    onStopPlayback: () -> Unit,
    onOpenSettings: () -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ضبط صدا",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    // Permission denied state
                    showPermissionDenied && !microphonePermissionGranted -> {
                        PermissionDeniedContent(
                            onOpenSettings = onOpenSettings
                        )
                    }
                    // Need permission
                    !microphonePermissionGranted -> {
                        RequestPermissionContent(
                            onRequestPermission = onRequestPermission
                        )
                    }
                    // Processing audio
                    isProcessingAudio -> {
                        ProcessingAudioContent()
                    }
                    // Has recording
                    hasRecording -> {
                        RecordingCompleteContent(
                            isPlaying = isPlaying,
                            onPlayRecording = onPlayRecording,
                            onStopPlayback = onStopPlayback,
                            onDeleteRecording = onDeleteRecording,
                            enabled = enabled
                        )
                    }
                    // Recording in progress
                    isRecording -> {
                        RecordingInProgressContent(
                            onStopRecording = onStopRecording
                        )
                    }
                    // Ready to record
                    else -> {
                        ReadyToRecordContent(
                            onStartRecording = onStartRecording,
                            enabled = enabled
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestPermissionContent(
    onRequestPermission: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(TealPurple.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎤",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "برای ضبط صدا، دسترسی به میکروفون نیاز است",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRequestPermission,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TealPurple
            )
        ) {
            Text(
                text = "اجازه دسترسی",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onOpenSettings: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(CoralRed.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🚫",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "دسترسی به میکروفون رد شده است",
            style = MaterialTheme.typography.bodyMedium,
            color = CoralRed
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "لطفاً از تنظیمات، دسترسی را فعال کنید",
            style = MaterialTheme.typography.bodySmall,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenSettings,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TealPurple
            )
        ) {
            Text(
                text = "باز کردن تنظیمات",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ReadyToRecordContent(
    onStartRecording: () -> Unit,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .clickable(enabled = enabled, onClick = onStartRecording),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.audio),
                contentDescription = null,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "برای ضبط صدا روی میکروفون بزنید.",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedText
        )
    }
}

@Composable
private fun RecordingInProgressContent(
    onStopRecording: () -> Unit
) {
    // Pulsating animation
    val scale by animateFloatAsState(
        targetValue = 1.1f,
        animationSpec = tween(500),
        label = "pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(scale)
                .background(CoralRed, CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onStopRecording),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.White, RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(CoralRed, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "در حال ضبط... برای توقف بزنید",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = CoralRed
            )
        }
    }
}

@Composable
private fun ProcessingAudioContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = TealPurple,
            strokeWidth = 3.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "در حال پردازش صدا...",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedText
        )
    }
}

@Composable
private fun RecordingCompleteContent(
    isPlaying: Boolean,
    onPlayRecording: () -> Unit,
    onStopPlayback: () -> Unit,
    onDeleteRecording: () -> Unit,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Play/Stop button
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (isPlaying) CoralRed else TealPurple,
                    CircleShape
                )
                .clip(CircleShape)
                .clickable(
                    enabled = enabled,
                    onClick = { if (isPlaying) onStopPlayback() else onPlayRecording() }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isPlaying) {
                // Stop icon (square)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                )
            } else {
                // Play icon (triangle using text)
                Text(
                    text = "▶",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isPlaying) "در حال پخش..." else "صدا با موفقیت ضبط شد",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isPlaying) CoralRed else TealPurple
        )

        if (!isPlaying) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "برای گوش دادن، روی دکمه بالا بزنید",
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Delete button
        Button(
            onClick = onDeleteRecording,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CoralRed.copy(alpha = 0.1f),
                contentColor = CoralRed
            ),
            enabled = enabled && !isPlaying
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "حذف و ضبط مجدد",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}