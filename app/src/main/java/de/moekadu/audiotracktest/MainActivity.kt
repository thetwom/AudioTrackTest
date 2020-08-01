package de.moekadu.audiotracktest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    val player = Player()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.play_button)
        val stopButton = findViewById<Button>(R.id.stop_button)

        startButton.setOnClickListener { player.play() }
        stopButton.setOnClickListener { player.stop() }
    }
}