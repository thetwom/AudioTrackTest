package de.moekadu.audiotracktest

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.RadioButton
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    val player = Player().apply {
        routingInfoListener = object: Player.RoutingInfoListener {
            override fun onRoutingChanged(info: String) {
                textViews[0]?.text = "${textViews[0]?.text ?: ""}\n$info"
            }
        }
    }

    var playerInService : Player? = null

    var textViews = Array<TextView?>(2) {null}

    val serviceConnection = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.v("AudioTrackTest", "MainActivity.onServiceDisconnected")
            playerInService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.v("AudioTrackTest", "MainActivity.onServiceConnected (activity context)")
            playerInService = (service as AudioTrackService.AudioTrackBinder).player
            playerInService?.routingInfoListener = object: Player.RoutingInfoListener {
                override fun onRoutingChanged(info: String) {
                    textViews[1]?.text = "${textViews[1]?.text ?: ""}\n$info"
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val stopButton = findViewById<RadioButton>(R.id.stop)
        val playActivity = findViewById<RadioButton>(R.id.play_activity)
        val playService = findViewById<RadioButton>(R.id.play_service)

        textViews[0] = findViewById(R.id.textView)
        textViews[1] = findViewById(R.id.textView2)

        val contextService = applicationContext
        val serviceIntent = Intent(contextService, AudioTrackService::class.java)
        contextService.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        stopButton.isChecked = true

        stopButton.setOnClickListener {
            stopPlaying()
        }

        playActivity.setOnClickListener {
            Log.v("AudioTrackTest", "MainActivity: playActivity")
            stopPlaying()
            val info = player.play()
            textViews[0]?.text = "info (activity):\n$info"
        }
        playService.setOnClickListener {
            Log.v("AudioTrackTest", "MainActivity: playServiceActivityContext")
            stopPlaying()
            val info = playerInService?.play() ?: ""
            textViews[1]?.text = "info (service):\n$info"
        }
    }

    private fun stopPlaying() {
        player.stop()
        playerInService?.stop()
    }
}