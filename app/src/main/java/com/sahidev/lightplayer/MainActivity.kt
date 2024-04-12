package com.sahidev.lightplayer

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import com.sahidev.lightplayer.audio.presentation.AudioRoute
import com.sahidev.lightplayer.core.player.service.LightAudioService
import com.sahidev.lightplayer.ui.theme.LightPlayerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                0
            )
        }

        setContent {
            LightPlayerTheme {
                AudioRoute(startAudioService = { startAudioService() })
            }
        }
    }

    private fun startAudioService() {
        val intent = Intent(applicationContext, LightAudioService::class.java).also {
            it.action = LightAudioService.Actions.START.toString()
        }
        startService(intent)
    }
}