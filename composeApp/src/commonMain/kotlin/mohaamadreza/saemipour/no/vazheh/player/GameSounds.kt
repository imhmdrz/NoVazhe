package mohaamadreza.saemipour.no.vazheh.player

import novazheh.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Feedback sounds shared by all games, bundled in composeResources/files.
 * The URIs are playable by [AudioPlayer] on both Android (asset URI) and iOS (file URL).
 * صداهای بازخورد مشترک بازی‌ها: پاسخ درست، پاسخ اشتباه و برد (اشتباه برای باخت هم استفاده می‌شود)
 */
@OptIn(ExperimentalResourceApi::class)
object GameSounds {
    val correct: String get() = Res.getUri("files/correct_answer_sound.mp3")
    val wrong: String get() = Res.getUri("files/wrong_answer.mp3")
    val win: String get() = Res.getUri("files/winning_sound1.mp3")
}
