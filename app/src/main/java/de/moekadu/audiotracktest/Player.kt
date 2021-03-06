package de.moekadu.audiotracktest

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

class Player {


    private var audioTrack : AudioTrack? = null
    private var updatePeriod = 0
    private var writtenFrames = 0
    private var buffer = FloatArray(0)

    fun play(bufferSizeMultiple : Int, bufferSizeMillis : Float = -1f) {
        stop()

        val sampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC)
        var bufferSizeInBytes = 0

        if (bufferSizeMillis > 0f) {
            val bufferSizeInFrames = (bufferSizeMillis * sampleRate / 1000.0f).roundToInt()
            bufferSizeInBytes = bufferSizeInFrames * 4
        }
        else{
            val minBufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
            bufferSizeInBytes = bufferSizeMultiple * minBufferSizeInBytes
        }


        Log.v("AudioTrackTest", "Player.play: buufferSizeInyBytes = $bufferSizeInBytes")
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

        track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack?) { }

            override fun onPeriodicNotification(track: AudioTrack?) {
                Log.v("AudioTrackTest", "Player.onPeriodicNotification: headPosition = ${track?.playbackHeadPosition}")
                writeSineToAudioTrack()
            }

        })
        audioTrack = track
        track.flush()

        track.positionNotificationPeriod = updatePeriod
        track.play()

        // write several update periods to audioTrack to fill the complete buffer, afterwards
        // our periodic notification listener will fill the buffer.
        for(i in 0 until updatePeriodFraction)
            writeSineToAudioTrack()
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