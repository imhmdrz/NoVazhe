package mohaamadreza.saemipour.no.vazheh.pinning

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

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
            AppPinningResult.Pinned
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

    override suspend fun pinAndAwait(timeoutMs: Long): AppPinningResult {
        val act = activity ?: return AppPinningResult.Failed
        if (isPinned()) return AppPinningResult.Pinned

        // Kick off the request. startLockTask() returns immediately — on OEMs that show the
        // "OK / Got it" consent dialog we are NOT pinned yet at this point.
        Log.d(TAG, "pinAndAwait: startLockTask() (state before=${lockTaskStateName()})")
        try {
            act.startLockTask()
        } catch (_: IllegalStateException) {
            Log.d(TAG, "pinAndAwait: IllegalStateException → NotAvailable")
            return AppPinningResult.NotAvailable
        } catch (_: SecurityException) {
            Log.d(TAG, "pinAndAwait: SecurityException → NotAvailable")
            return AppPinningResult.NotAvailable
        } catch (t: Throwable) {
            Log.d(TAG, "pinAndAwait: ${t.javaClass.simpleName} → Failed")
            return AppPinningResult.Failed
        }

        // There is no callback for the user tapping "OK", so poll the real lock-task state
        // until it actually flips to pinned. A timeout means the user dismissed/cancelled the
        // consent dialog (or pinning never engaged).
        val pinned = withTimeoutOrNull(timeoutMs) {
            while (!isPinned()) {
                delay(150)
            }
            true
        } ?: false

        val result = if (pinned) AppPinningResult.Pinned else AppPinningResult.NotAvailable
        Log.d(TAG, "pinAndAwait: result=$result (state after=${lockTaskStateName()})")
        return result
    }

    override fun unpin(): Boolean {
        val act = activity ?: return false
        return try {
            act.stopLockTask()
            // stopLockTask() is a silent no-op (no exception) when the app was never pinned,
            // so a clean return proves nothing — confirm against the real OS state.
            val stillPinned = isPinned()
            Log.d(TAG, "stopLockTask() → stillPinned=$stillPinned (state=${lockTaskStateName()})")
            !stillPinned
        } catch (t: Throwable) {
            Log.d(TAG, "stopLockTask() threw ${t.javaClass.simpleName}")
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

    /** Human-readable OS lock-task state, for the diagnostic logs above. */
    private fun lockTaskStateName(): String {
        val act = activity ?: return "no-activity"
        val am = act.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return "no-am"
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            @Suppress("DEPRECATION")
            return if (am.isInLockTaskMode) "PINNED/LOCKED(legacy)" else "NONE(legacy)"
        }
        return when (am.lockTaskModeState) {
            ActivityManager.LOCK_TASK_MODE_NONE -> "NONE"
            ActivityManager.LOCK_TASK_MODE_LOCKED -> "LOCKED"
            ActivityManager.LOCK_TASK_MODE_PINNED -> "PINNED"
            else -> "UNKNOWN"
        }
    }

    private companion object {
        /** Diagnostic tag — filter logcat with `tag:AppPinning`. Safe to keep. */
        const val TAG = "AppPinning"
    }
}
