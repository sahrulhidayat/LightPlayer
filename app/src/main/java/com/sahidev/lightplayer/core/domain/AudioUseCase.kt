package com.sahidev.lightplayer.core.domain

import com.sahidev.lightplayer.core.data.local.model.Audio

class AudioUseCaseImpl(
    private val repository: AudioRepository
) : AudioUseCase {
    override suspend fun getAudioData(): List<Audio> {
        return repository.getAudioData()
    }
}

interface AudioUseCase {
    suspend fun getAudioData(): List<Audio>

}