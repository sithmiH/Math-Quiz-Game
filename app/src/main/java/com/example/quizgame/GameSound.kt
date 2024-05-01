package com.example.quizgame

import android.content.Context
import android.media.MediaPlayer

class GameSound(private val context: Context) {

    private var media_player: MediaPlayer? = null

    fun play_sound(resourceId: Int) {
        // Release any existing MediaPlayer instance
        media_player?.release()

        // Create a new MediaPlayer instance
        media_player = MediaPlayer.create(context, resourceId)

        // Start playing the sound
        media_player?.start()

        // Release the MediaPlayer resources when the sound playback is complete
        media_player?.setOnCompletionListener {
            media_player?.release()
            media_player = null
        }
    }

    fun stop_sound() {
        media_player?.stop()
        media_player?.release()
        media_player = null
    }
}