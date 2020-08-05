package de.moekadu.audiotracktest

import android.content.Context
import android.media.*
import android.os.Handler
import android.util.Log
import kotlin.math.PI
import kotlin.math.sin

class Player {

    private var audioTrack : AudioTrack? = null
    private var updatePeriod = 0
    private var writtenFrames = 0
    private var buffer = FloatArray(0)

    interface RoutingInfoListener {
        fun onRoutingChanged(info : String)
    }

    var routingInfoListener : RoutingInfoListener? = null

    fun play() : String {
        audioTrack?.stop()

        val sampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC)
        val bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
        val updatePeriodFraction = 2
        // Divide by 4 to get buffer size in float
        updatePeriod = bufferSizeInBytes / 4 / updatePeriodFraction
        buffer = FloatArray(updatePeriod)
        writtenFrames = 0

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSizeInBytes)
            .build()

        track.addOnRoutingChangedListener(
            object: AudioRouting.OnRoutingChangedListener {
                override fun onRoutingChanged(router: AudioRouting?) {
                    val device = router?.routedDevice
                    val info = "product name = ${device?.productName}, type = ${device?.type}"
                    routingInfoListener?.onRoutingChanged(info)
                }
            },
            null
        )

        track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack?) { }

            override fun onPeriodicNotification(track: AudioTrack?) {
                writeSineToAudioTrack()
            }

        })
        audioTrack = track
        track.flush()
        track.play()
        track.positionNotificationPeriod = updatePeriod
        // write several update periods to audioTrack to fill the complete buffer, afterwards
        // our periodic notification listener will fill the buffer.
        for(i in 0 until updatePeriodFraction)
            writeSineToAudioTrack()
        return "sample rate = $sampleRate, buffer size in bytes = $bufferSizeInBytes"
    }

    fun stop() {
        audioTrack?.stop()
        audioTrack = null
    }

    private fun writeSineToAudioTrack() {
        val track = audioTrack ?: return
        val frequency = 220f
        val sampleRate = track.sampleRate
//        Log.v("AudioTrackTest", "writtenFrames = $writtenFrames, update period = $updatePeriod")

        for (i in buffer.indices) {
            val time = (writtenFrames + i).toFloat() / sampleRate.toFloat()
            buffer[i] = sin(2f * PI.toFloat() * frequency * time)
        }

        track.write(buffer, 0, buffer.size, AudioTrack.WRITE_NON_BLOCKING)
        writtenFrames += buffer.size
    }
}