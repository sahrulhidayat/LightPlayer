package com.sahidev.lightplayer.core.domain

import com.sahidev.lightplayer.core.data.local.model.Audio

interface AudioRepository {
    suspend fun getAudioData(): List<Audio>
}