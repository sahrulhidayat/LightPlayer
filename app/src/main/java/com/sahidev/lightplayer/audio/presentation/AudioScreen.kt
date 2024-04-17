package com.sahidev.lightplayer.audio.presentation

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sahidev.lightplayer.core.data.local.model.Audio
import com.sahidev.lightplayer.core.utils.Formatter
import com.sahidev.lightplayer.ui.theme.LightPlayerTheme

@Composable
fun AudioRoute(
    startAudioService: () -> Unit,
    viewModel: AudioViewModel = hiltViewModel()
) {
    AudioScreen(
        progress = viewModel.progress,
        isAudioPlaying = viewModel.isPlaying,
        currentPlayingAudio = viewModel.currentSelectedAudio,
        audioList = viewModel.audioList,
        onStart = {
            viewModel.onUiEvents(UiEvents.PlayPause)
            startAudioService()
        },
        onItemClick = {
            viewModel.onUiEvents(UiEvents.SelectedAudioChange(it))
            startAudioService()
        },
        onPrevious = { viewModel.onUiEvents(UiEvents.SeekToPrevious) },
        onNext = { viewModel.onUiEvents(UiEvents.SeekToNext) }
    )
}


@Composable
fun AudioScreen(
    progress: Float,
    isAudioPlaying: Boolean,
    currentPlayingAudio: Audio,
    audioList: List<Audio>,
    onStart: () -> Unit,
    onItemClick: (Int) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                itemsIndexed(audioList) { index, audio ->
                    AudioItem(audio = audio, onItemClick = { onItemClick(index) })
                }
            }
            BottomBarPlayer(
                progress = progress,
                audio = currentPlayingAudio,
                isAudioPlaying = isAudioPlaying,
                onStart = { onStart() },
                onPrevious = { onPrevious() },
                onNext = { onNext() },
            )
        }
    }
}

@Composable
fun AudioItem(
    audio: Audio,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 8.dp)
            .clickable { onItemClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable {
                    onItemClick()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = audio.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = audio.artist,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
            Text(text = Formatter.timeStampToDuration(audio.duration.toLong()))
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

@Composable
fun BottomBarPlayer(
    progress: Float,
    audio: Audio,
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = modifier
                .fillMaxWidth()
                .height(3.dp)
        )
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArtistInfo(audio = audio, modifier = Modifier.weight(1f))
                MediaPlayerController(
                    isAudioPlaying = isAudioPlaying,
                    onStart = { onStart() },
                    onPrevious = { onPrevious() },
                    onNext = { onNext() }
                )
            }
        }
    }
}

@Composable
fun MediaPlayerController(
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(48.dp)
            .padding(4.dp)

    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(48.dp)
                .clickable { onPrevious() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = "Previous"
            )
        }
        PlayerIconItem(
            icon = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = "Play or Pause",
            onClick = { onStart() }
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(48.dp)
                .clickable { onNext() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = "Next"
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistInfo(
    audio: Audio,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.border(
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                shape = CircleShape
            )
        ) {
            Icon(
                modifier = Modifier
                    .padding(2.dp)
                    .size(26.dp),
                imageVector = Icons.Default.MusicNote,
                contentDescription = "music note logo"
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column {
            Text(
                text = audio.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Clip,
                maxLines = 1,
                modifier = Modifier.basicMarquee(Int.MAX_VALUE),
            )
            Text(
                text = audio.artist,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Clip,
                maxLines = 1
            )
        }
    }
}

@Composable
fun PlayerIconItem(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    borderStroke: BorderStroke? = null,
    color: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        border = borderStroke,
        contentColor = color,
        color = backgroundColor,
        modifier = modifier
            .clip(CircleShape)
            .clickable {
                onClick()
            }
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(size),
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun AudioScreenPrev() {
    val dummyAudio = Audio(Uri.EMPTY, "Display name", 0L, "Artist", "Data", 123, "Title")
    LightPlayerTheme {
        AudioScreen(
            progress = 50f,
            isAudioPlaying = false,
            currentPlayingAudio = dummyAudio,
            audioList = listOf(dummyAudio, dummyAudio),
            onStart = {},
            onItemClick = {},
            onPrevious = {},
            onNext = {}
        )
    }
}
