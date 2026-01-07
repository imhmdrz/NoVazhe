package mohaamadreza.saemipour.no.vazheh.player

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class AudioPlayer(
    onProgressCallback: (PlayerState) -> Unit,
    onReadyCallback: () -> Unit,
    onErrorCallback: (Exception) -> Unit,
    context: Any?
) {
    fun pause()
    fun play(url: String)
    fun cleanUp()
    fun seek(position: Float)
}
