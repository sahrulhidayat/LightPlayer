package com.sahidev.lightplayer.audio.presentation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.sahidev.lightplayer.core.data.local.model.Audio
import com.sahidev.lightplayer.core.domain.AudioUseCase
import com.sahidev.lightplayer.core.player.service.LightAudioServiceHandler
import com.sahidev.lightplayer.core.player.service.LightAudioState
import com.sahidev.lightplayer.core.player.service.PlayerEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val useCase: AudioUseCase,
    private val audioServiceHandler: LightAudioServiceHandler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val dummyAudio = Audio(Uri.EMPTY, "Display name", 0L, "Artist", "Data", 0, "Title")

    var duration by mutableLongStateOf(0L)
    var progress by mutableFloatStateOf(0f)
    var progressString by mutableStateOf("0:00")
    var isPlaying by mutableStateOf(false)
    var currentSelectedAudio by mutableStateOf(dummyAudio)
    var audioList by mutableStateOf(listOf<Audio>())

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    init {
        loadAudioData()
        viewModelScope.launch {
            audioServiceHandler.audioState.collectLatest { mediaState ->
                when (mediaState) {
                    LightAudioState.Initial -> _uiState.value = UiState.Initial
                    is LightAudioState.Buffering -> calculateProgressValue(mediaState.progress)
                    is LightAudioState.Playing -> isPlaying = mediaState.isPlaying
                    is LightAudioState.Progress -> calculateProgressValue(mediaState.progress)
                    is LightAudioState.CurrentPlaying -> {
                        currentSelectedAudio = audioList[mediaState.mediaItemIndex]
                    }

                    is LightAudioState.Ready -> {
                        duration = mediaState.duration
                        _uiState.value = UiState.Ready
                    }
                }
            }
        }
    }

    fun onUiEvents(uiEvents: UiEvents) = viewModelScope.launch {
        when (uiEvents) {
            UiEvents.Backward -> audioServiceHandler.onPlayerEvents(PlayerEvent.Backward)
            UiEvents.Forward -> audioServiceHandler.onPlayerEvents(PlayerEvent.Forward)
            UiEvents.PlayPause -> audioServiceHandler.onPlayerEvents(PlayerEvent.PlayPause)
            UiEvents.Stop -> {
                audioServiceHandler.onPlayerEvents(PlayerEvent.Stop)
                isPlaying = false
            }

            UiEvents.SeekToPrevious -> audioServiceHandler.onPlayerEvents(PlayerEvent.SeekToPrevious)
            UiEvents.SeekToNext -> audioServiceHandler.onPlayerEvents(PlayerEvent.SeekToNext)
            is UiEvents.SeekTo -> audioServiceHandler.onPlayerEvents(
                PlayerEvent.SeekTo,
                seekPosition = ((duration * uiEvents.position) / 100f).toLong()
            )

            is UiEvents.SelectedAudioChange -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SelectedAudioChange,
                    selectedAudioIndex = uiEvents.index
                )
            }

            is UiEvents.UpdateProgress -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.UpdateProgress(uiEvents.newProgress)
                )
                progress = uiEvents.newProgress
            }

        }
    }

    private fun loadAudioData() {
        viewModelScope.launch {
            val audio = useCase.getAudioData()
            audioList = audio
            setMediaItems()
        }
    }

    private fun setMediaItems() {
        audioList.map { audio ->
            MediaItem.Builder()
                .setUri(audio.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setAlbumArtist(audio.artist)
                        .setDisplayTitle(audio.title)
                        .setSubtitle(audio.displayName)
                        .build()
                )
                .build()
        }.also {
            audioServiceHandler.setItemList(it)
        }
    }

    private fun calculateProgressValue(currentProgress: Long) {
        progress =
            if (currentProgress > 0) ((currentProgress.toFloat() / duration.toFloat()) * 100f)
            else 0f
        progressString = formatDuration(currentProgress)
    }

    private fun formatDuration(duration: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        viewModelScope.launch {
            audioServiceHandler.onPlayerEvents(PlayerEvent.Stop)
        }
        super.onCleared()
    }
}

sealed class UiState {
    data object Initial : UiState()
    data object Ready : UiState()
}

sealed class UiEvents {
    data object PlayPause : UiEvents()
    data object Stop : UiEvents()
    data class SelectedAudioChange(val index: Int) : UiEvents()
    data class SeekTo(val position: Float) : UiEvents()
    data object SeekToPrevious : UiEvents()
    data object SeekToNext : UiEvents()
    data object Backward : UiEvents()
    data object Forward : UiEvents()
    data class UpdateProgress(val newProgress: Float) : UiEvents()
}