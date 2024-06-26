package com.sahidev.lightplayer.core.utils

import kotlin.math.floor

object Formatter {
    fun timeStampToDuration(position: Long): String {
        val totalSeconds = floor(position / 1E3).toInt()
        val minutes = totalSeconds / 60
        val remainingSeconds = totalSeconds - (minutes * 60)
        return if (position < 0) "--:--" else "%d:%02d".format(minutes, remainingSeconds)
    }
}
