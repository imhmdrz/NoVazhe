package mohaamadreza.saemipour.no.vazheh.data

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

actual fun createSettings(): Settings {
    val preferences = Preferences.userRoot().node("novazheh")
    return PreferencesSettings(preferences)
}
