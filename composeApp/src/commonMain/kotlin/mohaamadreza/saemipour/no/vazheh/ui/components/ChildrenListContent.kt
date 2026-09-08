package mohaamadreza.saemipour.no.vazheh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mohaamadreza.saemipour.no.vazheh.data.ChildDTO
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.SoftGray
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealPurple
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.CustomWordsState

@Composable
fun ChildrenListContent(
    children: List<ChildDTO>,
    onChildClick: (ChildDTO) -> Unit,
    onAddChildClick: () -> Unit,
    onAddWordClick: () -> Unit,
    customWordsState: CustomWordsState,
    onRetry: () -> Unit,
    currentPlayingAudioUrl: String? = null,
    onPlayAudio: (audioUrl: String) -> Unit = {},
    onStopAudio: () -> Unit = {},
    onDeleteCustomWord: (wordId: Int) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stickyHeader {
            Row(
                Modifier.background(SoftGray).padding(
                    top = 32.dp, bottom = 12.dp
                )
            ) {
                Text(
                    "فرزندان شما",
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkText
                )

                Text(
                    "+  افزودن فرزند",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TealPurple,
                    modifier = Modifier.clickable(onClick = onAddChildClick)
                )
            }
        }


        if (children.isEmpty()) {
            item {
                EmptyChildrenContent(
                    onAddChildClick = onAddChildClick
                )
            }

        } else {
            items(children.size) { index ->
                ChildContent(
                    child = children[index], onClick = { onChildClick(children[index]) })
            }
        }

        stickyHeader {
            Row(
                Modifier.background(SoftGray).padding(
                    top = 20.dp, bottom = 12.dp
                )
            ) {
                Text(
                    "مدیریت کلمات",
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkText
                )

                Text(
                    "+  افزودن کلمه",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TealPurple,
                    modifier = Modifier.clickable(onClick = onAddWordClick)
                )
            }
        }

        when (customWordsState) {
            is CustomWordsState.Error -> {
                item {
                    ErrorContent(
                        message = customWordsState.message,
                        onRetry = onRetry
                    )
                }
            }

            is CustomWordsState.Success -> {
                items(customWordsState.words.size) { index ->
                    val word = customWordsState.words[index]
                    CustomWordContent(
                        word = word,
                        isPlaying = currentPlayingAudioUrl == word.audioUrl,
                        onPlayClick = { audioUrl -> onPlayAudio(audioUrl) },
                        onStopClick = onStopAudio,
                        onDeleteClick = { wordId -> onDeleteCustomWord(wordId) },
                        onClick = {}
                    )
                }
            }

            else -> {}
        }
    }
}
