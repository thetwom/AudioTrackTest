package de.moekadu.audiotracktest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton

class MainActivity : AppCompatActivity() {

    val player = Player()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val minBufferSizeButton = findViewById<RadioButton>(R.id.minimim_buffer_size)
        val twoTimesMinBufferSizeButton = findViewById<RadioButton>(R.id.two_minimim_buffer_size)
        val fivehundredMillisButton = findViewById<RadioButton>(R.id.fivehundred_millis)
        val thousandMillisButton = findViewById<RadioButton>(R.id.thousand_millis)
        val stopButton = findViewById<RadioButton>(R.id.stop_button)
        stopButton.isChecked = true

        minBufferSizeButton.setOnClickListener { player.play(1) }
        twoTimesMinBufferSizeButton.setOnClickListener { player.play(2) }
        fivehundredMillisButton.setOnClickListener { player.play(-1, 500f) }
        thousandMillisButton.setOnClickListener { player.play(-1, 1000f) }
        stopButton.setOnClickListener { player.stop() }
    }
}