package mohaamadreza.saemipour.no.vazheh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.data.TokenStorage
import mohaamadreza.saemipour.no.vazheh.pinning.rememberAppPinState
import mohaamadreza.saemipour.no.vazheh.pinning.rememberAppPinner
import org.koin.compose.koinInject

/**
 * Small floating padlock that locks / unlocks Kids Mode app pinning from anywhere
 * in the app. Shows 🔒 while the app is pinned and 🔓 while it isn't; tapping toggles.
 *
 * Unlocking always requires the parent's 4-digit PIN (otherwise a child could just
 * tap to exit). Locking is free, but when no PIN exists yet we run the create flow
 * first so there is always a PIN available to unlock with later.
 *
 * @param refreshKey re-reads the real OS pin state whenever this value changes
 *   (pass the current route) so the icon stays in sync after pinning/unpinning
 *   from elsewhere (e.g. the KidsModeGuideDialog or a system gesture).
 */
@Composable
fun AppPinToggle(
    modifier: Modifier = Modifier,
    refreshKey: Any? = null,
) {
    val pinner = rememberAppPinner()
    val scope = rememberCoroutineScope()
    val tokenStorage = koinInject<TokenStorage>()

    // Kept in sync with the real OS pin state — see rememberAppPinState.
    var isPinned by rememberAppPinState(pinner)
    var showPinDialog by remember { mutableStateOf(false) }
    var pinDialogMode by remember { mutableStateOf<PinCodeMode>(PinCodeMode.Create) }
    // True when the create flow should be followed by an actual pin (i.e. the user
    // tapped "lock" but had no PIN yet); false when we're just verifying to unlock.
    var pinAfterCreate by remember { mutableStateOf(false) }

    // Keep the icon in sync with the real OS state across navigations.
    LaunchedEffect(refreshKey) { isPinned = pinner.isPinned() }

    fun pin() {
        scope.launch {
            pinner.pinAndAwait()
            isPinned = pinner.isPinned()
        }
    }

    fun unpin() {
        pinner.unpin()
        isPinned = pinner.isPinned()
    }

    fun onToggle() {
        val existingPin = tokenStorage.getKidsPin()
        if (isPinned) {
            // Unlock — must verify the parent's PIN first.
            if (existingPin == null) {
                unpin()
            } else {
                pinDialogMode = PinCodeMode.Verify(existingPin)
                pinAfterCreate = false
                showPinDialog = true
            }
        } else {
            // Lock — free, but make sure a PIN exists so it can be unlocked later.
            if (existingPin == null) {
                pinDialogMode = PinCodeMode.Create
                pinAfterCreate = true
                showPinDialog = true
            } else {
                pin()
            }
        }
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .background(Color.White.copy(alpha = 0.90f), CircleShape)
            .border(1.dp, Color.Black, CircleShape)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isPinned) "🔒" else "🔓",
            fontSize = 20.sp,
        )
    }

    if (showPinDialog) {
        PinCodeDialog(
            mode = pinDialogMode,
            onDismiss = { showPinDialog = false },
            onSuccess = { enteredPin ->
                showPinDialog = false
                when (pinDialogMode) {
                    PinCodeMode.Create -> {
                        tokenStorage.saveKidsPin(enteredPin)
                        if (pinAfterCreate) pin()
                    }
                    is PinCodeMode.Verify -> unpin()
                    is PinCodeMode.Change -> Unit
                }
            },
        )
    }
}
