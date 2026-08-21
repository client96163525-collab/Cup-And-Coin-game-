package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class GameAudioEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

    var isSoundEnabled: Boolean = true
    var isVibrationEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val sampleRate = 44100

    private fun playTone(
        durationSec: Double,
        generator: (timeSec: Double) -> Double
    ) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val numSamples = (durationSec * sampleRate).toInt()
                val samples = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val sampleValue = generator(t).coerceIn(-1.0, 1.0)
                    samples[i] = (sampleValue * Short.MAX_VALUE).toInt().toShort()
                }

                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(samples.size * 2)

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()

                // Release track after playback completes
                launch {
                    kotlinx.coroutines.delay((durationSec * 1000).toLong() + 100)
                    try {
                        audioTrack.stop()
                        audioTrack.release()
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {
                // Audio synthesis fallback safety
            }
        }
    }

    fun playCupMove() {
        // Subtle smooth whoosh
        playTone(0.12) { t ->
            val env = exp(-t * 25.0)
            val freq = 220.0 + sin(t * 40.0) * 80.0
            sin(2.0 * PI * freq * t) * env * 0.35
        }
    }

    fun playCupLand() {
        // Satisfying wooden cup tap/drop
        playTone(0.09) { t ->
            val env = exp(-t * 55.0)
            val base = sin(2.0 * PI * 180.0 * t)
            val click = sin(2.0 * PI * 750.0 * t) * 0.4
            (base + click) * env * 0.45
        }
        triggerVibrate(15, 60)
    }

    fun playCoinReveal() {
        // Sparkling crystalline arpeggio (C6, E6, G6, C7)
        playTone(0.4) { t ->
            val env = exp(-t * 7.0)
            val f1 = 1046.5 // C6
            val f2 = 1318.5 // E6
            val f3 = 1567.98 // G6
            val f4 = 2093.0 // C7

            val s1 = sin(2.0 * PI * f1 * t) * if (t < 0.1) 1.0 else exp(-(t - 0.1) * 8.0)
            val s2 = if (t >= 0.08) sin(2.0 * PI * f2 * (t - 0.08)) * exp(-(t - 0.08) * 8.0) else 0.0
            val s3 = if (t >= 0.16) sin(2.0 * PI * f3 * (t - 0.16)) * exp(-(t - 0.16) * 8.0) else 0.0
            val s4 = if (t >= 0.24) sin(2.0 * PI * f4 * (t - 0.24)) * exp(-(t - 0.24) * 6.0) else 0.0

            (s1 + s2 + s3 + s4) * env * 0.35
        }
        triggerVibrate(25, 100)
    }

    fun playTap() {
        playTone(0.04) { t ->
            val env = exp(-t * 90.0)
            sin(2.0 * PI * 920.0 * t) * env * 0.3
        }
        triggerVibrate(10, 40)
    }

    fun playTabSwitch() {
        // Crisp, snappy, modern synth bubble sound
        playTone(0.08) { t ->
            val env = exp(-t * 50.0)
            val freq = 587.33 + sin(t * 150.0) * 350.0 // Snappy D5 base
            sin(2.0 * PI * freq * t) * env * 0.25
        }
        triggerVibrate(6, 45)
    }

    fun playComboMultiplier() {
        // High-energy rising arcade pitch-bend sound
        playTone(0.35) { t ->
            val env = exp(-t * 8.0)
            val freq = 600.0 + (t * 2200.0) // Swift upward sweep
            (sin(2.0 * PI * freq * t) + sin(2.0 * PI * (freq * 1.5) * t) * 0.3) * env * 0.25
        }
        triggerPatternVibrate(longArrayOf(0, 30, 20, 30, 20, 50))
    }

    fun playThemeUnlock() {
        // Majestic cosmic ambient sweeping chord progression
        playTone(0.7) { t ->
            val env = exp(-t * 2.5)
            // Luminous golden major chord sweep
            val baseFreq = 523.25 // C5
            val c1 = sin(2.0 * PI * baseFreq * t)
            val c2 = sin(2.0 * PI * (baseFreq * 1.25) * t) // E5
            val c3 = sin(2.0 * PI * (baseFreq * 1.5) * t) // G5
            val c4 = sin(2.0 * PI * (baseFreq * 1.875) * t) // B5
            (c1 + c2 + c3 + c4) * 0.2 * env
        }
        triggerPatternVibrate(longArrayOf(0, 40, 40, 40, 40, 100))
    }

    fun playJackpot() {
        // A cascading waterfall of sparkling crystal arpeggios
        playTone(0.8) { t ->
            val env = exp(-t * 3.0)
            val step = (t * 8.0).toInt()
            val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98, 2093.00, 2637.02)
            val freq = if (step < freqs.size) freqs[step] else freqs.last()
            sin(2.0 * PI * freq * t) * env * 0.35
        }
        triggerPatternVibrate(longArrayOf(0, 50, 50, 50, 50, 50, 50, 150))
    }

    fun playWin() {
        // Triumphant fanfare chord progression
        playTone(0.65) { t ->
            val env = exp(-t * 3.5)
            val chord1 = if (t < 0.2) {
                sin(2.0 * PI * 523.25 * t) + sin(2.0 * PI * 659.25 * t) + sin(2.0 * PI * 783.99 * t)
            } else if (t < 0.4) {
                sin(2.0 * PI * 659.25 * t) + sin(2.0 * PI * 783.99 * t) + sin(2.0 * PI * 1046.5 * t)
            } else {
                sin(2.0 * PI * 1046.5 * t) * 1.5 + sin(2.0 * PI * 1318.5 * t) + sin(2.0 * PI * 1567.98 * t)
            }
            (chord1 * 0.25) * env
        }
        triggerPatternVibrate(longArrayOf(0, 40, 50, 80, 50, 120))
    }

    fun playLose() {
        // Subtle descending suspense minor tone
        playTone(0.45) { t ->
            val env = exp(-t * 5.0)
            val freq = (380.0 - t * 240.0).coerceAtLeast(100.0)
            (sin(2.0 * PI * freq * t) + sin(2.0 * PI * (freq * 0.5) * t) * 0.5) * env * 0.35
        }
        triggerPatternVibrate(longArrayOf(0, 80, 40, 120))
    }

    fun playLevelUp() {
        playTone(0.5) { t ->
            val env = exp(-t * 4.0)
            val f = if (t < 0.15) 587.33 else if (t < 0.3) 880.0 else 1174.66
            sin(2.0 * PI * f * t) * env * 0.4
        }
        triggerPatternVibrate(longArrayOf(0, 30, 40, 60, 40, 100))
    }

    fun playCountdownTick() {
        playTone(0.03) { t ->
            val env = exp(-t * 120.0)
            sin(2.0 * PI * 1200.0 * t) * env * 0.3
        }
        triggerVibrate(8, 30)
    }

    private fun triggerVibrate(durationMs: Long, amplitude: Int = 100) {
        if (!isVibrationEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun triggerPatternVibrate(timings: LongArray) {
        if (!isVibrationEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitudes = IntArray(timings.size) { index -> if (index % 2 == 1) 180 else 0 }
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            }
        } catch (_: Exception) {}
    }
}
