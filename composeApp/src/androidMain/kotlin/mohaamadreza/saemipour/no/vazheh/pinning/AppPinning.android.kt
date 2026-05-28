package mohaamadreza.saemipour.no.vazheh.pinning

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberAppPinner(): AppPinner {
    val activity = LocalActivity.current
    return remember(activity) { AndroidAppPinner(activity) }
}

/**
 * Android implementation that uses [Activity.startLockTask] to enter
 * **screen pinning** (the user-exitable variant of Lock Task Mode).
 *
 * This intentionally does NOT try to become Device Owner — that requires
 * provisioning the device with `adb dpm set-device-owner` or via Android
 * Enterprise enrollment, which is not viable for a consumer Play Store app.
 *
 * For [pin] to succeed the user must have enabled "App pinning / Screen
 * pinning" in system Settings (the path is brand-specific — see
 * `KidsModeGuideDialog`). On some OEMs the OS will also show a one-time
 * confirmation dialog the first time pinning is attempted. After pinning the
 * user can exit at any time using the standard back+recents gesture, which
 * is desirable here so a parent can leave Kids Mode.
 */
private class AndroidAppPinner(
    private val activity: Activity?
) : AppPinner {

    override fun pin(): AppPinningResult {
        val act = activity ?: return AppPinningResult.Failed

        // If we're already pinned (or fully locked via DPC), report success and
        // skip the call — startLockTask is a no-op in that state but it keeps
        // the contract simple for the caller.
        if (isPinned()) return AppPinningResult.Pinned

        return try {
            act.startLockTask()
            // Re-check: some OEMs silently refuse and never enter pinning even
            // though no exception was thrown.
            if (isPinned()) AppPinningResult.Pinned else AppPinningResult.NotAvailable
        } catch (_: IllegalStateException) {
            // Thrown when the activity isn't in the foreground or screen
            // pinning is disabled in Settings.
            AppPinningResult.NotAvailable
        } catch (_: SecurityException) {
            AppPinningResult.NotAvailable
        } catch (_: Throwable) {
            AppPinningResult.Failed
        }
    }

    override fun unpin(): Boolean {
        val act = activity ?: return false
        return try {
            act.stopLockTask()
            true
        } catch (_: Throwable) {
            false
        }
    }

    override fun isPinned(): Boolean {
        val act = activity ?: return false
        val am = act.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val state = am.lockTaskModeState
            state == ActivityManager.LOCK_TASK_MODE_PINNED ||
                state == ActivityManager.LOCK_TASK_MODE_LOCKED
        } else {
            @Suppress("DEPRECATION")
            am.isInLockTaskMode
        }
    }
}
