package mohaamadreza.saemipour.no.vazheh.recorder

import androidx.compose.runtime.Composable

interface AudioRecorder {
    fun start(outputFilePath: String)
    fun stop()
}

expect class RecorderManager : AudioRecorder {
    override fun start(outputFilePath: String)
    override fun stop()
}

@Composable
expect fun rememberRecorderManager(): RecorderManager
