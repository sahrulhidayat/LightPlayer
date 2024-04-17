package com.sahidev.lightplayer.core.player.service

import android.content.Context
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class LightAudioServiceHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer
) : Player.Listener {

    private val _audioState: MutableStateFlow<LightAudioState> =
        MutableStateFlow(LightAudioState.Initial)
    val audioState: StateFlow<LightAudioState> = _audioState.asStateFlow()

    private var job: Job? = null

    init {
        exoPlayer.addListener(this)
    }

    fun addMediaItem(mediaItem: MediaItem) {
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    fun setItemList(mediaItems: List<MediaItem>) {
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
    }

    suspend fun onPlayerEvents(
        playerEvent: PlayerEvent,
        selectedAudioIndex: Int = -1,
        seekPosition: Long = 0L
    ) {
        when (playerEvent) {
            PlayerEvent.Backward -> exoPlayer.seekBack()
            PlayerEvent.Forward -> exoPlayer.seekForward()
            PlayerEvent.PlayPause -> playOrPause()
            PlayerEvent.SeekTo -> exoPlayer.seekTo(seekPosition)
            PlayerEvent.SeekToPrevious -> exoPlayer.seekToPrevious()
            PlayerEvent.SeekToNext -> exoPlayer.seekToNext()
            PlayerEvent.SelectedAudioChange -> {
                when (selectedAudioIndex) {
                    exoPlayer.currentMediaItemIndex -> playOrPause()
                    else -> {
                        exoPlayer.seekToDefaultPosition(selectedAudioIndex)
                        _audioState.value = LightAudioState.Playing(
                            isPlaying = true
                        )
                        exoPlayer.playWhenReady = true
                        startProgressUpdate()
                    }
                }
            }

            PlayerEvent.Stop -> {
                exoPlayer.stop()
                _audioState.value = LightAudioState.Playing(
                    isPlaying = false
                )
                stopProgressUpdate()
            }

            is PlayerEvent.UpdateProgress -> {
                exoPlayer.seekTo(
                    (exoPlayer.duration * playerEvent.newProgress).toLong()
                )
            }
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> {
                startAudioService()
                _audioState.value = LightAudioState.Buffering(exoPlayer.currentPosition)
            }

            ExoPlayer.STATE_READY -> {
                _audioState.value = LightAudioState.Ready(exoPlayer.duration)
            }

            ExoPlayer.STATE_ENDED -> {}

            ExoPlayer.STATE_IDLE -> {}
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _audioState.value = LightAudioState.Playing(isPlaying = isPlaying)
        _audioState.value = LightAudioState.CurrentPlaying(exoPlayer.currentMediaItemIndex)
        if (isPlaying) {
            CoroutineScope(Dispatchers.Main).launch {
                startProgressUpdate()
            }
        } else {
            stopProgressUpdate()
        }
    }

    override fun onTracksChanged(tracks: Tracks) {
        _audioState.value = LightAudioState.CurrentPlaying(exoPlayer.currentMediaItemIndex)
    }

    private suspend fun playOrPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            _audioState.value = LightAudioState.Playing(isPlaying = false)
            stopProgressUpdate()
        } else {
            exoPlayer.play()
            _audioState.value = LightAudioState.Playing(isPlaying = true)
            startProgressUpdate()
        }
    }

    private suspend fun startProgressUpdate() = job.run {
        while (true) {
            delay(500)
            _audioState.value = LightAudioState.Progress(exoPlayer.currentPosition)
        }
    }

    private fun stopProgressUpdate() {
        job?.cancel()
        _audioState.value = LightAudioState.Playing(isPlaying = false)
    }

    private fun startAudioService() {
        val intent = Intent(context, LightAudioService::class.java).also {
            it.action = LightAudioService.Actions.START.toString()
        }
        context.startService(intent)
    }
}

sealed class PlayerEvent {
    data object PlayPause : PlayerEvent()
    data object SelectedAudioChange : PlayerEvent()
    data object Backward : PlayerEvent()
    data object SeekToPrevious : PlayerEvent()
    data object SeekToNext : PlayerEvent()
    data object Forward : PlayerEvent()
    data object SeekTo : PlayerEvent()
    data object Stop : PlayerEvent()
    data class UpdateProgress(val newProgress: Float) : PlayerEvent()
}

sealed class LightAudioState {
    data object Initial : LightAudioState()
    data class Ready(val duration: Long) : LightAudioState()
    data class Progress(val progress: Long) : LightAudioState()
    data class Buffering(val progress: Long) : LightAudioState()
    data class Playing(val isPlaying: Boolean) : LightAudioState()
    data class CurrentPlaying(val mediaItemIndex: Int) : LightAudioState()
}