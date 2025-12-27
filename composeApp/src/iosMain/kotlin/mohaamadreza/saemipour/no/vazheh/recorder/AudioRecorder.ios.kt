package mohaamadreza.saemipour.no.vazheh.recorder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
actual class RecorderManager : AudioRecorder {

    private var audioRecorder: AVAudioRecorder? = null

    actual override fun start(outputFilePath: String) {
        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setCategory(AVAudioSessionCategoryPlayAndRecord, error = null)
        audioSession.setActive(true, error = null)

        val url = NSURL.fileURLWithPath(outputFilePath)

        val settings = mapOf<Any?, Any?>(
            AVFormatIDKey to kAudioFormatMPEG4AAC,
            AVSampleRateKey to 44100.0,
            AVNumberOfChannelsKey to 1,
            AVEncoderAudioQualityKey to 2 // AVAudioQuality.high = 2
        )

        audioRecorder = AVAudioRecorder(url, settings, null)
        audioRecorder?.prepareToRecord()
        audioRecorder?.record()
    }

    actual override fun stop() {
        audioRecorder?.stop()
        audioRecorder = null

        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setActive(
            false,
            withOptions = AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation,
            error = null
        )
    }
}

@Composable
actual fun rememberRecorderManager(): RecorderManager {
    return remember { RecorderManager() }
}