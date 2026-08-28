package mohaamadreza.saemipour.no.vazheh.pinning

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo

/**
 * Result of attempting to enter screen pinning (Lock Task Mode "pinned" state).
 * نتیجه تلاش برای پین کردن برنامه
 */
enum class AppPinningResult {
    /** Activity is now pinned (or was already pinned). */
    Pinned,

    /**
     * The OS refused to pin — almost always because the user has not enabled
     * "App pinning / Screen pinning" in system Settings yet. The brand-specific
     * guide in [KidsModeGuideDialog] should be surfaced as a fallback.
     */
    NotAvailable,

    /**
     * Pinning is not supported on this platform at all (e.g. iOS — there is no
     * public Guided Access API).
     */
    Unsupported,

    /** Any unexpected failure (foreground requirement, OEM quirk, etc.). */
    Failed
}

/**
 * Platform-specific app pinning controller.
 * - On Android: wraps [Activity.startLockTask] / [stopLockTask] for screen pinning.
 * - On iOS: a stub that always returns [AppPinningResult.Unsupported].
 */
interface AppPinner {
    /**
     * Requests screen pinning and returns *optimistically*: on Android this calls
     * [Activity.startLockTask], which is non-blocking, so on OEMs that show a "OK / Got it"
     * confirmation dialog the result is reported before the user has actually confirmed.
     * Prefer [pinAndAwait] when you need to know the user really pinned.
     */
    fun pin(): AppPinningResult

    /**
     * Requests screen pinning and then waits for the OS to actually enter the pinned state.
     *
     * `startLockTask()` does not block and there is no callback for the user tapping "OK" on
     * the OEM screen-pinning consent dialog, so the only way to know the user confirmed is to
     * observe [ActivityManager.getLockTaskModeState] flipping to pinned. This polls that state
     * until it becomes pinned or [timeoutMs] elapses (the latter meaning the user dismissed /
     * cancelled the consent dialog, or pinning is disabled in Settings).
     *
     * @return [AppPinningResult.Pinned] once the OS confirms pinning, otherwise the failure
     *   reason (or [AppPinningResult.NotAvailable] on timeout).
     */
    suspend fun pinAndAwait(timeoutMs: Long = 6_000L): AppPinningResult

    fun unpin(): Boolean
    fun isPinned(): Boolean
}

/**
 * Obtain an [AppPinner] bound to the current activity/window. Must be called
 * from a Composable so the platform implementation can grab the right
 * `LocalActivity` (Android) or no-op (iOS).
 */
@Composable
expect fun rememberAppPinner(): AppPinner

/**
 * The pin state the UI should display, kept in sync with the REAL OS state - وضعیت واقعی پین
 *
 * [AppPinner.isPinned] always queries the OS, but Android gives apps no callback when the
 * pin state changes externally: the user can pin the app from the system Recents screen,
 * leave with the OS unpin gesture, or confirm the OEM consent dialog AFTER
 * [AppPinner.pinAndAwait] already timed out. A one-shot
 * `remember { mutableStateOf(pinner.isPinned()) }` therefore drifts out of sync in both
 * directions (UI says pinned while free, or unpinned while locked).
 *
 * Every one of those transitions passes through the window losing and regaining focus
 * (system UI temporarily takes focus), so this re-reads the real OS state whenever focus
 * returns — no polling, no arbitrary delays. The returned state can still be written
 * directly (e.g. right after the app's own pin/unpin action), exactly like a plain
 * `remember { mutableStateOf(...) }`. On iOS this is a harmless no-op: the stub pinner
 * always reports unpinned.
 */
@Composable
fun rememberAppPinState(pinner: AppPinner): MutableState<Boolean> {
    val isPinned = remember(pinner) { mutableStateOf(pinner.isPinned()) }

    val isWindowFocused = LocalWindowInfo.current.isWindowFocused
    LaunchedEffect(pinner, isWindowFocused) {
        if (isWindowFocused) isPinned.value = pinner.isPinned()
    }

    return isPinned
}
