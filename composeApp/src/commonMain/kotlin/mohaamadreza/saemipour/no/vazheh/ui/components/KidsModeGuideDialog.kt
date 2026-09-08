package mohaamadreza.saemipour.no.vazheh.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.pinning.AppPinningResult
import mohaamadreza.saemipour.no.vazheh.pinning.rememberAppPinState
import mohaamadreza.saemipour.no.vazheh.pinning.rememberAppPinner
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MintGreen
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
    // The step-by-step guide stays hidden until the parent actually tries to pin
    // and it fails. On iOS there's no programmatic pinning, so reveal it up-front.
    var showGuide by remember { mutableStateOf(!isAndroid) }

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
                    fontSize = 24.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "حالت کودک",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(20.dp))
                // Quick-action button sits at the top so the mother can pin/unpin
                // with one tap without scrolling through the brand guide first.
                PinActionsSection(
                    isAndroid = isAndroid,
                    onPinFailed = { showGuide = true }
                )

                // The brand guide only appears once pinning fails (or on iOS),
                // so the dialog stays focused on the single action by default.
                if (showGuide) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Wrap the platform-specific guide in a single top-level dropdown
                    // so the dialog stays compact by default. The parent acts as
                    // "Show me the step-by-step guide" and the brand sub-cards are
                    // collapsed too — so first paint is just a stack of headers.
                    ParentGuideDropdown(
                        title = if (isAndroid) {
                            "📱 اندروید - پین کردن صفحه"
                        } else {
                            "🍎 آیفون/آیپد - دسترسی هدایت‌شده"
                        }
                    ) {
                        if (isAndroid) {
                            AndroidGuide()
                        } else {
                            IOSGuide()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "متوجه شدم",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TealPurple
                    )
                }
            }
        }
    }
}

/**
 * Top-of-dialog quick actions. On Android the mother gets a primary "pin now"
 * button (calls [Activity.startLockTask]) and a secondary "unpin"
 * (`stopLockTask`) button side-by-side, plus a status banner that reflects
 * the most recent action. On iOS both buttons are hidden because there's no
 * public Guided Access API — the user gets a one-time informational banner.
 *
 * The parent reaches this dialog only after passing the Settings/Profile
 * account-password gate, so pin and unpin actions execute directly here.
 */
@Composable
private fun PinActionsSection(
    isAndroid: Boolean,
    onPinFailed: () -> Unit
) {
    val pinner = rememberAppPinner()
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<PinActionStatus?>(null) }

    // Drives the single toggle button: "unlock" when the app is pinned, "lock" otherwise.
    // Mirrors the REAL OS pin state and self-heals when it changes externally (system
    // Recents pin, OS unpin gesture, consent confirmed after pinAndAwait timed out).
    var isPinned by rememberAppPinState(pinner)

    fun pin() {
        // startLockTask() doesn't block and there's no "user tapped OK" callback, so
        // wait for the OS to actually report the pinned state before showing success.
        scope.launch {
            val result = pinner.pinAndAwait()
            status = PinActionStatus.fromPin(result)
            isPinned = pinner.isPinned()
            // Pinning didn't engage (disabled in Settings, consent cancelled, OEM
            // quirk) — reveal the step-by-step guide so the parent can enable it.
            if (result == AppPinningResult.NotAvailable || result == AppPinningResult.Failed) {
                onPinFailed()
            }
        }
    }

    fun unpin() {
        status = if (pinner.unpin()) PinActionStatus.Unpinned else PinActionStatus.UnpinFailed
        isPinned = pinner.isPinned()
    }

    // On iOS surface the "not supported" hint up-front so the mother knows the
    // top buttons aren't applicable to her device.
    LaunchedEffect(isAndroid) {
        if (!isAndroid) {
            status = PinActionStatus.UnsupportedPlatform
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isAndroid) {
            // Single toggle: shows "unlock" while pinned, "lock" otherwise.
            Button(
                onClick = {
                    if (isPinned) unpin() else pin()
                },
                modifier = Modifier
                    .fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = if (isPinned) {
                    ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = TealPurple
                    )
                } else {
                    ButtonDefaults.buttonColors(containerColor = TealPurple)
                },
                border = if (isPinned) BorderStroke(width = 1.5.dp, color = TealPurple) else null
                ) {
                    Text(
                        text = if (isPinned) "🔓 خروج از قفل" else "📌 قفل کن",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPinned) TealPurple else Color.White
                    )
                }
        }

        // Status banner reflecting the most recent action
        when (status) {
            PinActionStatus.Pinned -> StatusBanner(
                text = "✅ برنامه پین شد! برای خروج روی «خروج از قفل» بزنید.",
                color = MintGreen
            )
            PinActionStatus.Unpinned -> StatusBanner(
                text = "🔓 پین برنامه برداشته شد.",
                color = MintGreen
            )
            PinActionStatus.NotAvailable -> StatusBanner(
                text = "⚠️ هنوز پین کردن برنامه در تنظیمات گوشی فعال نیست. ابتدا مطابق راهنمای زیر آن را روشن کنید.",
                color = CoralRed
            )
            PinActionStatus.UnsupportedPlatform -> StatusBanner(
                text = "ℹ️ پین کردن خودکار روی iOS پشتیبانی نمی‌شود. لطفاً از مسیر دستی Guided Access استفاده کنید.",
                color = MutedText
            )
            PinActionStatus.PinFailed -> StatusBanner(
                text = "❌ پین کردن انجام نشد. لطفاً مطابق راهنمای زیر تنظیمات گوشی را بررسی کنید.",
                color = CoralRed
            )
            PinActionStatus.UnpinFailed -> StatusBanner(
                text = "❌ خروج از پین انجام نشد. اگر برنامه پین نیست، نیازی به این عمل ندارید.",
                color = CoralRed
            )
            null -> Unit
        }
    }
}

/**
 * Local UI status enum that merges pin / unpin outcomes into a single state so
 * the banner only ever shows the most recent action.
 */
private enum class PinActionStatus {
    Pinned,
    Unpinned,
    NotAvailable,
    UnsupportedPlatform,
    PinFailed,
    UnpinFailed;

    companion object {
        fun fromPin(result: AppPinningResult): PinActionStatus = when (result) {
            AppPinningResult.Pinned -> Pinned
            AppPinningResult.NotAvailable -> NotAvailable
            AppPinningResult.Unsupported -> UnsupportedPlatform
            AppPinningResult.Failed -> PinFailed
        }
    }
}

@Composable
private fun StatusBanner(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = color.copy(alpha = 0.10f),
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
private fun AndroidGuide() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
            title = "🟦 پیکسل / اندروید استاندارد",
            steps = listOf(
                "تنظیمات (Settings) را باز کنید.",
                "«امنیت و حریم خصوصی» (Security & privacy).",
                "«تنظیمات بیشتر امنیت» (More security).",
                "«پین کردن برنامه» (App pinning) را روشن کنید.",
                "این برنامه را باز و Overview (مربع) را بزنید.",
                "آیکون برنامه بالای کارت ← Pin."
            ),
            exitHint = "خروج: Back و Overview را همزمان نگه دارید."
        )

        BrandGuide(
            title = "🟩 سامسونگ",
            steps = listOf(
                "تنظیمات (Settings) را باز کنید.",
                "«بیومتریک و امنیت» (Biometrics and security).",
                "«سایر تنظیمات امنیتی» (Other security).",
                "«پین کردن صفحه» (Pin windows) را روشن کنید.",
                "این برنامه را باز و Recent (اخیر) را بزنید.",
                "آیکون برنامه بالای کارت ← Pin this app."
            ),
            exitHint = "خروج: Back و Recent را همزمان نگه دارید (در ژست انگشتی: از پایین به بالا بکشید و نگه دارید)."
        )

        BrandGuide(
            title = "🟥 شیائومی / ردمی / پوکو",
            steps = listOf(
                "تنظیمات (Settings) را باز کنید.",
                "«رمز عبور و امنیت» (Passwords & security).",
                "«حریم خصوصی» ← «دسترسی ویژه» (Special permissions).",
                "«پین کردن برنامه» (App pinning) را روشن کنید.",
                "این برنامه را باز و Recent (مربع) را بزنید.",
                "روی کارت برنامه فشار طولانی ← قفل (🔒 / Pin)."
            ),
            exitHint = "خروج: Home و Back را همزمان نگه دارید (در ژست انگشتی: از پایین به بالا بکشید و نگه دارید)."
        )

        BrandGuide(
            title = "🟧 هواوی / آنر",
            steps = listOf(
                "تنظیمات (Settings) را باز کنید.",
                "«امنیت» (Security).",
                "«تنظیمات بیشتر» (More settings).",
                "«پین کردن صفحه» (Screen pinning) را روشن کنید.",
                "این برنامه را باز و Recent (مربع) را بزنید.",
                "آیکون قفل (📌) بالای کارت را بزنید."
            ),
            exitHint = "خروج: Back و Recent را همزمان نگه دارید."
        )

        BrandGuide(
            title = "🟨 اوپو / ریلمی / وان‌پلاس",
            steps = listOf(
                "تنظیمات (Settings) را باز کنید.",
                "«حریم خصوصی» (Privacy).",
                "«پین کردن صفحه» (App pinning) را روشن کنید.",
                "این برنامه را باز و Recent را بزنید.",
                "آیکون برنامه بالای کارت ← Pin / Lock."
            ),
            exitHint = "خروج: Back و Recent را همزمان نگه دارید (یا از پایین به بالا بکشید و نگه دارید)."
        )

        BrandGuide(
            title = "🟪 ویوو / آی‌کوو",
            steps = listOf(
                "تنظیمات (Settings) را باز کنید.",
                "«اثرانگشت، چهره و رمز» یا «امنیت».",
                "«پین کردن صفحه» (Screen pinning) را روشن کنید.",
                "این برنامه را باز و Recent را بزنید.",
                "آیکون قفل (📌) بالای کارت را بزنید."
            ),
            exitHint = "خروج: Back و Recent را همزمان نگه دارید."
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
 * Top-level collapsible container that wraps the whole platform-specific
 * guide ([AndroidGuide] / [IOSGuide]). The brand-specific accordions are
 * already collapsed inside, so the entire guide tree is hidden behind a
 * single tap by default — keeping the dialog short on first render.
 */
@Composable
private fun ParentGuideDropdown(
    title: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = TealPurple.copy(alpha = 0.4f),
                shape = RoundedCornerShape(14.dp)
            )
            .background(
                color = TealPurple.copy(alpha = 0.04f),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TealPurple,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "بستن راهنما" else "نمایش راهنما",
                tint = TealPurple
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            ) {
                content()
            }
        }
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
                .padding(horizontal = 4.dp, vertical = 12.dp),
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
                    .padding(start = 6.dp, end = 6.dp, bottom = 12.dp),
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
        Text(
            text = "$number $text",
            style = MaterialTheme.typography.bodySmall,
            color = DarkText,
            modifier = Modifier.weight(1f)
        )
    }
}





