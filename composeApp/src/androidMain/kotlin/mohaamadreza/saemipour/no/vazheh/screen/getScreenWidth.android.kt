package mohaamadreza.saemipour.no.vazheh.screen

import android.content.res.Resources

actual fun getScreenWidth(): Float {
    val configuration = Resources.getSystem().configuration
    return configuration.screenWidthDp.toFloat()
}