package mohaamadreza.saemipour.no.vazheh.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MutedText
import mohaamadreza.saemipour.no.vazheh.ui.theme.SoftGray
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealPurple

private const val PIN_LENGTH = 4

/**
 * Reason the [PinCodeDialog] is being shown. Drives copy and whether we ask
 * the user to confirm the PIN twice.
 *
 * - [Create] — first-time setup; the user enters the PIN twice.
 * - [Verify] — gate an existing action (e.g. pin/unpin). The user enters their
 *   saved PIN once and we check it against [verifyAgainst].
 * - [Change] — change the existing PIN: verify the old PIN, then set a new one
 *   twice. The current PIN is supplied via [verifyAgainst].
 */
sealed class PinCodeMode {
    data object Create : PinCodeMode()
    data class Verify(val verifyAgainst: String) : PinCodeMode()
    data class Change(val verifyAgainst: String) : PinCodeMode()
}

/**
 * 4-digit PIN entry dialog used to gate Kids Mode app pinning / unpinning.
 *
 * - In [PinCodeMode.Create]: user enters a 4-digit PIN, then confirms it again.
 *   On match → [onSuccess] is invoked with the chosen PIN.
 * - In [PinCodeMode.Verify]: user enters a single PIN. If it matches
 *   [PinCodeMode.Verify.verifyAgainst] → [onSuccess] is invoked with that PIN.
 * - In [PinCodeMode.Change]: verify the existing PIN first, then run the
 *   Create flow for the new PIN.
 */
@Composable
fun PinCodeDialog(
    mode: PinCodeMode,
    onDismiss: () -> Unit,
    onSuccess: (pin: String) -> Unit
) {
    // Tracks which sub-step we're on inside the dialog. The dialog is its own
    // little state machine so the caller doesn't need to chain dialogs.
    var step by remember(mode) {
        mutableStateOf(
            when (mode) {
                PinCodeMode.Create -> PinStep.EnterNew
                is PinCodeMode.Verify -> PinStep.VerifyExisting
                is PinCodeMode.Change -> PinStep.VerifyExisting
            }
        )
    }
    var firstPin by remember(mode) { mutableStateOf("") }
    var input by remember(mode, step) { mutableStateOf("") }
    // Keyed only on `mode` so that an error raised at the end of one step
    // (e.g. mismatched confirm PIN) survives the step rewind back to EnterNew
    // and stays visible until the user starts typing again.
    var errorMessage by remember(mode) { mutableStateOf<String?>(null) }

    val title = when (step) {
        PinStep.EnterNew -> "تعیین رمز ۴ رقمی"
        PinStep.ConfirmNew -> "تأیید رمز جدید"
        PinStep.VerifyExisting -> "رمز را وارد کنید"
    }
    val subtitle = when (step) {
        PinStep.EnterNew -> "یک رمز ۴ رقمی برای حالت کودک انتخاب کنید"
        PinStep.ConfirmNew -> "رمز را یک‌بار دیگر وارد کنید"
        PinStep.VerifyExisting -> when (mode) {
            is PinCodeMode.Change -> "برای تغییر رمز، رمز فعلی را وارد کنید"
            else -> "برای ادامه، رمز ۴ رقمی خود را وارد کنید"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🔐", fontSize = 36.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    PinInputCells(
                        value = input,
                        onValueChange = { new ->
                            input = new.filter { it.isDigit() }.take(PIN_LENGTH)
                            errorMessage = null
                        },
                        hasError = errorMessage != null
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoralRed,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        when (step) {
                            PinStep.VerifyExisting -> {
                                val expected = when (mode) {
                                    is PinCodeMode.Verify -> mode.verifyAgainst
                                    is PinCodeMode.Change -> mode.verifyAgainst
                                    PinCodeMode.Create -> ""
                                }
                                if (input == expected) {
                                    when (mode) {
                                        is PinCodeMode.Verify -> onSuccess(input)
                                        is PinCodeMode.Change -> {
                                            // Verified; move on to setting a new PIN
                                            input = ""
                                            step = PinStep.EnterNew
                                        }
                                        PinCodeMode.Create -> Unit
                                    }
                                } else {
                                    errorMessage = "رمز اشتباه است"
                                    input = ""
                                }
                            }
                            PinStep.EnterNew -> {
                                firstPin = input
                                input = ""
                                step = PinStep.ConfirmNew
                            }
                            PinStep.ConfirmNew -> {
                                if (input == firstPin) {
                                    onSuccess(input)
                                } else {
                                    errorMessage = "رمزها مطابقت ندارند"
                                    input = ""
                                    firstPin = ""
                                    step = PinStep.EnterNew
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = input.length == PIN_LENGTH,
                    colors = ButtonDefaults.buttonColors(containerColor = TealPurple)
                ) {
                    Text(
                        text = when (step) {
                            PinStep.EnterNew -> "ادامه"
                            PinStep.ConfirmNew -> "ذخیره"
                            PinStep.VerifyExisting -> "تأیید"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text(text = "انصراف", color = MutedText)
                }
            }
        }
    }
}

/** Internal step within the dialog's mini state-machine. */
private enum class PinStep { EnterNew, ConfirmNew, VerifyExisting }

/**
 * Four visual cells stacked over a transparent [BasicTextField]. Tapping any
 * cell focuses the underlying field which brings up the numeric keyboard.
 */
@Composable
private fun PinInputCells(
    value: String,
    onValueChange: (String) -> Unit,
    hasError: Boolean
) {
    val focusRequester = remember { FocusRequester() }

    // Auto-focus when first composed so the keyboard appears right away.
    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Visual cells
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.clickable { focusRequester.requestFocus() }
        ) {
            repeat(PIN_LENGTH) { index ->
                val filled = index < value.length
                val borderColor = when {
                    hasError -> CoralRed
                    filled -> TealPurple
                    else -> SoftGray
                }
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 56.dp)
                        .background(
                            color = if (filled) TealPurple.copy(alpha = 0.06f) else Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.5.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (filled) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(TealPurple, CircleShape)
                        )
                    }
                }
            }
        }

        // Invisible input layered on top of the cells
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .focusRequester(focusRequester)
                .size(width = 1.dp, height = 1.dp),
            singleLine = true,
            cursorBrush = SolidColor(Color.Transparent),
            textStyle = TextStyle(color = Color.Transparent),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
        )
    }
}
