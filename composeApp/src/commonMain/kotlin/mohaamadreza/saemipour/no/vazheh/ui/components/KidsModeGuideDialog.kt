package mohaamadreza.saemipour.no.vazheh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MutedText
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealPurple

/**
 * Dialog showing parents how to enable screen lock for kids mode
 * راهنمای فعال‌سازی حالت کودک
 */
@Composable
fun KidsModeGuideDialog(
    onDismiss: () -> Unit,
    isAndroid: Boolean = true // Platform detection
) {
    Dialog(onDismissRequest = onDismiss, DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "🔒",
                    fontSize = 48.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "حالت کودک",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "برای جلوگیری از خروج کودک از برنامه، مراحل زیر را انجام دهید:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedText,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isAndroid) {
                    AndroidGuide()
                } else {
                    IOSGuide()
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPurple)
                ) {
                    Text(
                        text = "متوجه شدم",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun AndroidGuide() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "📱 اندروید - قفل صفحه (Screen Pinning)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TealPurple
        )
        
        GuideStep(
            number = 1,
            text = "به تنظیمات > امنیت > قفل صفحه بروید"
        )
        GuideStep(
            number = 2,
            text = "گزینه Screen Pinning را فعال کنید"
        )
        GuideStep(
            number = 3,
            text = "وارد برنامه شوید و دکمه Overview را بزنید"
        )
        GuideStep(
            number = 4,
            text = "روی آیکون برنامه بزنید و Pin را انتخاب کنید"
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "💡 برای خروج: دکمه‌های Back و Overview را همزمان نگه دارید",
            style = MaterialTheme.typography.bodySmall,
            color = MutedText,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = TealPurple.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        )
    }
}

@Composable
private fun IOSGuide() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "🍎 آیفون/آیپد - دسترسی هدایت‌شده (Guided Access)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TealPurple
        )
        
        GuideStep(
            number = 1,
            text = "به تنظیمات > دسترسی‌پذیری > دسترسی هدایت‌شده بروید"
        )
        GuideStep(
            number = 2,
            text = "گزینه Guided Access را فعال کنید"
        )
        GuideStep(
            number = 3,
            text = "یک رمز عبور تنظیم کنید"
        )
        GuideStep(
            number = 4,
            text = "در برنامه، دکمه کناری را سه بار بزنید"
        )
        GuideStep(
            number = 5,
            text = "Start را بزنید تا حالت کودک فعال شود"
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "💡 برای خروج: دکمه کناری را سه بار بزنید و رمز را وارد کنید",
            style = MaterialTheme.typography.bodySmall,
            color = MutedText,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = TealPurple.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        )
    }
}

@Composable
private fun GuideStep(
    number: Int,
    text: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Number badge
        Text(
            text = "$number",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .size(24.dp)
                .background(TealPurple, CircleShape)
                .padding(4.dp),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkText,
            modifier = Modifier.weight(1f)
        )
    }
}





