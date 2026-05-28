package mohaamadreza.saemipour.no.vazheh.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import mohaamadreza.saemipour.no.vazheh.ui.theme.SoftGray
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
            text = "📱 اندروید - پین کردن صفحه (Screen Pinning / App Pin)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TealPurple
        )

        Text(
            text = "مسیر فعال‌سازی روی هر گوشی متفاوت است. برند گوشی خود را انتخاب کنید:",
            style = MaterialTheme.typography.bodySmall,
            color = MutedText
        )

        // Brand-specific instructions: every Android skin (One UI / MIUI / HyperOS /
        // EMUI / MagicOS / ColorOS / FuntouchOS / OxygenOS / OneUI / stock) buries
        // screen pinning under a slightly different path, so each brand gets its own
        // collapsible section instead of a single generic guide.
        BrandGuide(
            title = "🟦 پیکسل / اندروید استاندارد (Google Pixel, Android One)",
            steps = listOf(
                "تنظیمات (Settings) را باز کنید",
                "وارد بخش «امنیت و حریم خصوصی» (Security & privacy) شوید",
                "روی «تنظیمات بیشتر امنیت» (More security settings) بزنید",
                "گزینه «پین کردن برنامه» (App pinning) را فعال کنید",
                "این برنامه را باز کنید و دکمه Overview (مربع) را بزنید",
                "روی آیکون برنامه بالای کارت بزنید و گزینه Pin را انتخاب کنید"
            ),
            exitHint = "خروج: دکمه‌های Back و Overview را همزمان نگه دارید"
        )

        BrandGuide(
            title = "🟩 سامسونگ (Samsung - One UI)",
            steps = listOf(
                "تنظیمات (Settings) را باز کنید",
                "وارد بخش «بیومتریک و امنیت» (Biometrics and security) شوید",
                "روی «تنظیمات بیشتر امنیت» (Other security settings) بزنید",
                "گزینه «پین کردن صفحه» (Pin windows) را فعال کنید",
                "این برنامه را باز کنید و دکمه Recent (تَب‌های اخیر) را بزنید",
                "روی آیکون برنامه بالای کارت بزنید و Pin this app را انتخاب کنید"
            ),
            exitHint = "خروج: دکمه‌های Back و Recent را همزمان نگه دارید (یا با ژست انگشت از پایین به بالا بکشید و نگه دارید)"
        )

        BrandGuide(
            title = "🟥 شیائومی / ردمی / پوکو (Xiaomi, Redmi, POCO - MIUI/HyperOS)",
            steps = listOf(
                "تنظیمات (Settings) را باز کنید",
                "وارد «رمز عبور و امنیت» (Passwords & security) شوید",
                "روی «حریم خصوصی» (Privacy) سپس «دسترسی ویژه» (Special permissions) بزنید",
                "گزینه «پین کردن برنامه» (Pinned apps / App pinning) را فعال کنید",
                "این برنامه را باز کنید و دکمه Recent (مربع) را بزنید",
                "روی کارت برنامه فشار طولانی دهید و آیکون قفل (🔒 / Pin) را بزنید"
            ),
            exitHint = "خروج: دکمه‌های Home و Back را همزمان نگه دارید (در ژست انگشتی، از پایین به بالا بکشید و نگه دارید)"
        )

        BrandGuide(
            title = "🟧 هواوی / آنر (Huawei, Honor - EMUI/MagicOS)",
            steps = listOf(
                "تنظیمات (Settings) را باز کنید",
                "وارد بخش «امنیت» (Security) شوید",
                "روی «تنظیمات بیشتر» (More settings) بزنید",
                "گزینه «پین کردن صفحه» (Screen pinning) را فعال کنید",
                "این برنامه را باز کنید و دکمه Recent (مربع) را بزنید",
                "روی آیکون قفل (📌) بالای کارت برنامه بزنید"
            ),
            exitHint = "خروج: دکمه‌های Back و Recent را همزمان نگه دارید"
        )

        BrandGuide(
            title = "🟨 اوپو / ریلمی / وان‌پلاس (Oppo, Realme, OnePlus - ColorOS/OxygenOS)",
            steps = listOf(
                "تنظیمات (Settings) را باز کنید",
                "وارد بخش «حریم خصوصی» (Privacy) شوید",
                "روی «پین کردن صفحه» (Pin current screen / App pinning) بزنید و آن را فعال کنید",
                "این برنامه را باز کنید و دکمه Recent را بزنید",
                "روی آیکون برنامه بالای کارت بزنید و Pin / Lock را انتخاب کنید"
            ),
            exitHint = "خروج: دکمه‌های Back و Recent را همزمان نگه دارید (یا از پایین به بالا بکشید و نگه دارید)"
        )

        BrandGuide(
            title = "🟪 ویوو / آی‌کوو (Vivo, iQOO - FuntouchOS/OriginOS)",
            steps = listOf(
                "تنظیمات (Settings) را باز کنید",
                "وارد بخش «اثرانگشت، چهره و رمز» یا «امنیت» شوید",
                "گزینه «پین کردن صفحه» (Screen pinning) را فعال کنید",
                "این برنامه را باز کنید و دکمه Recent را بزنید",
                "روی آیکون قفل (📌) بالای کارت برنامه بزنید"
            ),
            exitHint = "خروج: دکمه‌های Back و Recent را همزمان نگه دارید"
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "💡 نکته: اگر این مسیرها روی گوشی شما نبود، در جستجوی تنظیمات عبارت «pin» یا «پین» را تایپ کنید تا گزینه‌ی مربوط به مدل گوشی شما نمایش داده شود.",
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

/**
 * Collapsible per-brand section. Brands are collapsed by default so the dialog
 * doesn't overwhelm the parent with text — she taps the brand that matches her
 * phone to expand the exact steps.
 */
@Composable
private fun BrandGuide(
    title: String,
    steps: List<String>,
    exitHint: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = SoftGray,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = DarkText,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "بستن" else "باز کردن",
                tint = TealPurple
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                steps.forEachIndexed { index, step ->
                    GuideStep(number = index + 1, text = step)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = TealPurple.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = "💡 $exitHint",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }
            }
        }
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






