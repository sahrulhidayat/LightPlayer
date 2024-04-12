package com.sahidev.lightplayer.core.di

import android.content.Context
import com.sahidev.lightplayer.core.data.local.ContentResolverHelper
import com.sahidev.lightplayer.core.data.local.repository.AudioRepositoryImpl
import com.sahidev.lightplayer.core.domain.AudioRepository
import com.sahidev.lightplayer.core.domain.AudioUseCase
import com.sahidev.lightplayer.core.domain.AudioUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideContext(
        @ApplicationContext context: Context,
    ): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideAudioRepository(
        contentResolver: ContentResolverHelper
    ): AudioRepository {
        return AudioRepositoryImpl(contentResolver)
    }

    @Provides
    @Singleton
    fun provideAudioUseCase(
        repository: AudioRepository
    ): AudioUseCase {
        return AudioUseCaseImpl(repository)
    }
}