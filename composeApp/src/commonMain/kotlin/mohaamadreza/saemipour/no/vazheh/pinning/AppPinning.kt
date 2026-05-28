package mohaamadreza.saemipour.no.vazheh.pinning

import androidx.compose.runtime.Composable

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
    fun pin(): AppPinningResult
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
