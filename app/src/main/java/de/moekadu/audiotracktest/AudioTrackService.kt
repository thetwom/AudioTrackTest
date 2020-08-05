package de.moekadu.audiotracktest

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class AudioTrackService : Service() {

    inner class AudioTrackBinder : Binder() {
        val player
            get() = this@AudioTrackService.player
    }

    private val binder = AudioTrackBinder()
    private val player = Player()

    override fun onBind(intent: Intent?): IBinder? {
        Log.v("AudioTrackTest", "AudioTrackService.onBind")
        return binder
    }
}