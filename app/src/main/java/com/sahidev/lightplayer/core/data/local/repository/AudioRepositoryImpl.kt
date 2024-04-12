package com.sahidev.lightplayer.core.data.local.repository

import com.sahidev.lightplayer.core.data.local.ContentResolverHelper
import com.sahidev.lightplayer.core.data.local.model.Audio
import com.sahidev.lightplayer.core.domain.AudioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AudioRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolverHelper
) : AudioRepository {
    override suspend fun getAudioData(): List<Audio> = withContext(Dispatchers.IO) {
        contentResolver.getAudioData()
    }
}