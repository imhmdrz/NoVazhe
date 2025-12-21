package mohaamadreza.saemipour.no.vazheh.data

import android.annotation.SuppressLint
import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

@SuppressLint("StaticFieldLeak") private var appContext: Context? = null

fun initializeSettings(context: Context) {
    appContext = context.applicationContext
}

actual fun createSettings(): Settings {
    val context =
            appContext
                    ?: throw IllegalStateException(
                            "Settings not initialized. Call initializeSettings(context) first."
                    )
    val sharedPreferences = context.getSharedPreferences("novazheh_prefs", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(sharedPreferences)
}
