package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import com.example.model.GameMode
import com.example.util.DebugLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class GameAudioEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

    var isSoundEnabled: Boolean = true
    var isVibrationEnabled: Boolean = true

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val sampleRate = 22050
    private var sharedTrack: AudioTrack? = null
    private val audioLock = Any()

    init {
        initSharedTrack()
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.US)
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        isTtsReady = true
                    }
                }
            }
        } catch (_: Throwable) {
            tts = null
        }
    }

    fun speakVoice(text: String, pitch: Float = 1.0f, speechRate: Float = 1.0f) {
        if (!isSoundEnabled) return
        try {
            if (isTtsReady && tts != null) {
                tts?.setPitch(pitch)
                tts?.setSpeechRate(speechRate)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "GameVoice_${System.currentTimeMillis()}")
                } else {
                    @Suppress("DEPRECATION")
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
                }
            }
        } catch (_: Throwable) {}
    }

    private fun initSharedTrack() {
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(sampleRate * 2)

            sharedTrack = AudioTrack.Builder()
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
                .setBufferSizeInBytes(minBuf)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            if (sharedTrack?.state == AudioTrack.STATE_INITIALIZED) {
                sharedTrack?.play()
            }
        } catch (_: Throwable) {
            sharedTrack = null
        }
    }

    /**
     * Synthesizes audio samples in real-time and streams via reusable AudioTrack
     */
    private fun playTone(
        durationSec: Double,
        generator: (timeSec: Double) -> Double
    ) {
        if (!isSoundEnabled) return

        scope.launch {
            try {
                val numSamples = (durationSec * sampleRate).toInt().coerceAtLeast(1)
                val samples = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val sampleValue = generator(t).coerceIn(-1.0, 1.0)
                    samples[i] = (sampleValue * Short.MAX_VALUE).toInt().toShort()
                }

                synchronized(audioLock) {
                    var track = sharedTrack
                    if (track == null || track.state != AudioTrack.STATE_INITIALIZED) {
                        initSharedTrack()
                        track = sharedTrack
                    }
                    if (track != null && track.state == AudioTrack.STATE_INITIALIZED) {
                        if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                            try { track.play() } catch (_: Throwable) {}
                        }
                        track.write(samples, 0, samples.size)
                    }
                }
            } catch (_: Throwable) {
                // Ignore any audio error safely
            }
        }
    }

    // ==========================================
    // 🔀 MODE-SPECIFIC SHUFFLE SOUNDS
    // ==========================================

    fun playShuffle(mode: GameMode = GameMode.CLASSIC, swapIndex: Int = 0) {
        when (mode) {
            GameMode.CLASSIC -> {
                // Classic Acoustic Wooden Whoosh & Cup Friction
                playTone(0.13) { t ->
                    val env = exp(-t * 22.0)
                    val baseFreq = 240.0 + sin(t * 35.0) * 90.0
                    val woodTexture = sin(2.0 * PI * 680.0 * t) * 0.18
                    (sin(2.0 * PI * baseFreq * t) + woodTexture) * env * 0.38
                }
                triggerVibrate(12, 50)
            }
            GameMode.TIME_ATTACK -> {
                // Snappy Laser Hyper-Speed Electric Whoosh
                playTone(0.09) { t ->
                    val env = exp(-t * 32.0)
                    val freq = 520.0 + (t * 2400.0) // Rapid pitch sweep up
                    val synth = sin(2.0 * PI * freq * t) + sin(2.0 * PI * (freq * 2.0) * t) * 0.25
                    synth * env * 0.42
                }
                triggerVibrate(8, 70)
            }
            GameMode.ENDLESS -> {
                // Deep Reverberant Metallic Echo Shuffle
                playTone(0.16) { t ->
                    val env = exp(-t * 16.0)
                    val lowBase = sin(2.0 * PI * 160.0 * t)
                    val metallicOvertone = sin(2.0 * PI * 480.0 * t) * 0.35 + sin(2.0 * PI * 960.0 * t) * 0.15
                    (lowBase + metallicOvertone) * env * 0.4
                }
                triggerVibrate(15, 65)
            }
            GameMode.PERFECT_RUN -> {
                // Crystalline Glass Pure Glide with high shimmer
                playTone(0.12) { t ->
                    val env = exp(-t * 24.0)
                    val crystal1 = sin(2.0 * PI * 784.0 * t) // G5
                    val crystal2 = sin(2.0 * PI * 1174.66 * t) * 0.3 // D6
                    val sparkle = sin(2.0 * PI * 2093.0 * t) * 0.15 // C7
                    (crystal1 + crystal2 + sparkle) * env * 0.35
                }
                triggerVibrate(10, 45)
            }
            GameMode.DAILY_CHALLENGE -> {
                // Majestic Mystical Ancient Golden Vessel Shuffle
                playTone(0.15) { t ->
                    val env = exp(-t * 18.0)
                    val goldFreq = 349.23 + sin(t * 50.0) * 120.0 // F4 base with vibrato
                    val gongTone = sin(2.0 * PI * 880.0 * t) * 0.2
                    (sin(2.0 * PI * goldFreq * t) + gongTone) * env * 0.4
                }
                triggerVibrate(14, 60)
            }
            GameMode.TUTORIAL -> {
                // Crystal Clear, Gentle Educational Whoosh
                playTone(0.20) { t ->
                    val env = exp(-t * 14.0)
                    val tone = sin(2.0 * PI * (320.0 + sin(t * 20.0) * 40.0) * t)
                    val chime = sin(2.0 * PI * 640.0 * t) * 0.2
                    (tone + chime) * env * 0.35
                }
                triggerVibrate(10, 40)
            }
        }
    }

    // ==========================================
    // 🏆 MODE-SPECIFIC WIN SOUNDS
    // ==========================================

    fun playWin(mode: GameMode = GameMode.CLASSIC) {
        DebugLogger.i("AUDIO", "playWin called for mode=$mode")
        speakVoice("You Win!", pitch = 1.15f, speechRate = 1.05f)
        when (mode) {
            GameMode.CLASSIC -> {
                // Triumphant Warm Brass Fanfare Progression (C5 -> E5 -> G5 -> C6)
                playTone(0.70) { t ->
                    val env = exp(-t * 3.2)
                    val chord = when {
                        t < 0.18 -> sin(2.0 * PI * 523.25 * t) + sin(2.0 * PI * 659.25 * t) * 0.7 // C5, E5
                        t < 0.36 -> sin(2.0 * PI * 659.25 * t) + sin(2.0 * PI * 783.99 * t) * 0.8 // E5, G5
                        else -> sin(2.0 * PI * 1046.50 * t) * 1.2 + sin(2.0 * PI * 1318.51 * t) * 0.8 + sin(2.0 * PI * 1567.98 * t) * 0.6 // C6 + E6 + G6
                    }
                    (chord * 0.28) * env
                }
                triggerPatternVibrate(longArrayOf(0, 40, 50, 80, 50, 120))
            }
            GameMode.TIME_ATTACK -> {
                // High-Octane Hyper-Pop Arcade Synth Victory
                playTone(0.55) { t ->
                    val env = exp(-t * 4.0)
                    val step = (t * 14.0).toInt()
                    val freqs = doubleArrayOf(587.33, 739.99, 880.00, 1174.66, 1479.98, 1760.00, 2349.32)
                    val f = if (step < freqs.size) freqs[step] else freqs.last()
                    val synth = sin(2.0 * PI * f * t) + sin(2.0 * PI * (f * 1.5) * t) * 0.35
                    synth * env * 0.35
                }
                triggerPatternVibrate(longArrayOf(0, 30, 30, 30, 30, 60, 30, 100))
            }
            GameMode.ENDLESS -> {
                // Epic Survival Resonance & Rising Power Surge
                playTone(0.75) { t ->
                    val env = exp(-t * 2.8)
                    val subBass = sin(2.0 * PI * 130.81 * t) * 1.3 // C3 sub
                    val midBrass = sin(2.0 * PI * 392.00 * t) // G4
                    val topPower = if (t > 0.25) sin(2.0 * PI * 783.99 * t) * 0.9 else 0.0
                    (subBass + midBrass + topPower) * 0.26 * env
                }
                triggerPatternVibrate(longArrayOf(0, 60, 40, 80, 40, 150))
            }
            GameMode.PERFECT_RUN -> {
                // Sparkling Celestial Glockenspiel & Crystal Chime Cascade
                playTone(0.80) { t ->
                    val env = exp(-t * 2.5)
                    val step = (t * 10.0).toInt()
                    val chimeFreqs = doubleArrayOf(1046.50, 1318.51, 1567.98, 2093.00, 2637.02, 3135.96)
                    val f = if (step < chimeFreqs.size) chimeFreqs[step] else chimeFreqs.last()
                    val shimmer = sin(2.0 * PI * f * t) + sin(2.0 * PI * (f * 2.0) * t) * 0.25
                    shimmer * env * 0.32
                }
                triggerPatternVibrate(longArrayOf(0, 25, 30, 25, 30, 40, 30, 120))
            }
            GameMode.DAILY_CHALLENGE -> {
                // Royal Golden Grand Fanfare & Golden Bells Celebration
                playTone(0.85) { t ->
                    val env = exp(-t * 2.6)
                    val base = when {
                        t < 0.20 -> sin(2.0 * PI * 440.0 * t) + sin(2.0 * PI * 554.37 * t) // A4, C#5
                        t < 0.40 -> sin(2.0 * PI * 554.37 * t) + sin(2.0 * PI * 659.25 * t) // C#5, E5
                        else -> sin(2.0 * PI * 880.0 * t) * 1.3 + sin(2.0 * PI * 1108.73 * t) * 0.9 + sin(2.0 * PI * 1318.51 * t) * 0.7
                    }
                    val sparkle = sin(2.0 * PI * 2217.46 * t) * 0.2
                    (base + sparkle) * 0.25 * env
                }
                triggerPatternVibrate(longArrayOf(0, 50, 40, 60, 40, 160))
            }
            GameMode.TUTORIAL -> {
                // Uplifting, Encouraging Educational Melody (C5 -> E5 -> G5 -> C6 Arpeggio)
                playTone(0.70) { t ->
                    val env = exp(-t * 3.0)
                    val step = (t * 8.0).toInt()
                    val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
                    val f = if (step < freqs.size) freqs[step] else freqs.last()
                    val tone = sin(2.0 * PI * f * t) + sin(2.0 * PI * (f * 2.0) * t) * 0.2
                    tone * env * 0.35
                }
                triggerPatternVibrate(longArrayOf(0, 30, 30, 30, 30, 70))
            }
        }
    }

    // ==========================================
    // ❌ MODE-SPECIFIC LOSE SOUNDS
    // ==========================================

    fun playLose(mode: GameMode = GameMode.CLASSIC) {
        DebugLogger.i("AUDIO", "playLose called for mode=$mode")
        speakVoice("You Lose!", pitch = 0.85f, speechRate = 0.95f)
        when (mode) {
            GameMode.CLASSIC -> {
                // Smooth Acoustic Descending Minor Glissando
                playTone(0.48) { t ->
                    val env = exp(-t * 4.5)
                    val freq = (360.0 - t * 220.0).coerceAtLeast(110.0)
                    val wave = sin(2.0 * PI * freq * t) + sin(2.0 * PI * (freq * 0.5) * t) * 0.4
                    wave * env * 0.36
                }
                triggerPatternVibrate(longArrayOf(0, 70, 40, 110))
            }
            GameMode.TIME_ATTACK -> {
                // Electronic Dual Detuned Warning Buzzer Plunge
                playTone(0.38) { t ->
                    val env = exp(-t * 6.0)
                    val f1 = (580.0 - t * 380.0).coerceAtLeast(120.0)
                    val f2 = f1 * 1.05 // Slight detune for harsh buzzer bite
                    val buzzer = (sin(2.0 * PI * f1 * t) + sin(2.0 * PI * f2 * t) * 0.8)
                    buzzer * env * 0.4
                }
                triggerPatternVibrate(longArrayOf(0, 100, 30, 100))
            }
            GameMode.ENDLESS -> {
                // Dramatic Deep Sub-Bass Plunge & Heavy Gong Fade
                playTone(0.60) { t ->
                    val env = exp(-t * 3.5)
                    val deepSub = sin(2.0 * PI * (220.0 - t * 165.0).coerceAtLeast(45.0) * t) * 1.5
                    val gongClang = sin(2.0 * PI * 440.0 * t) * exp(-t * 12.0) * 0.6
                    (deepSub + gongClang) * env * 0.38
                }
                triggerPatternVibrate(longArrayOf(0, 120, 50, 160))
            }
            GameMode.PERFECT_RUN -> {
                // Delicate Crystal Glass Tone Dampened to Silence
                playTone(0.42) { t ->
                    val env = exp(-t * 8.0)
                    val crystalChime = sin(2.0 * PI * (1200.0 - t * 800.0).coerceAtLeast(200.0) * t)
                    crystalChime * env * 0.32
                }
                triggerPatternVibrate(longArrayOf(0, 50, 30, 70))
            }
            GameMode.DAILY_CHALLENGE -> {
                // Mystical Temple Dusk Gong Descending Minor Cadence
                playTone(0.55) { t ->
                    val env = exp(-t * 4.0)
                    val bell = sin(2.0 * PI * (330.0 - t * 180.0).coerceAtLeast(90.0) * t)
                    val shadow = sin(2.0 * PI * 185.0 * t) * 0.4
                    (bell + shadow) * env * 0.35
                }
                triggerPatternVibrate(longArrayOf(0, 80, 40, 120))
            }
            GameMode.TUTORIAL -> {
                // Soft, Gentle Encouraging Tone
                playTone(0.40) { t ->
                    val env = exp(-t * 4.0)
                    val softTone = sin(2.0 * PI * 261.63 * t) * 0.8 + sin(2.0 * PI * 220.00 * t) * 0.5
                    softTone * env * 0.30
                }
                triggerPatternVibrate(longArrayOf(0, 40, 30, 50))
            }
        }
    }

    // ==========================================
    // 🔘 INTERACTIVE UI CLICKS & REWARD SOUNDS
    // ==========================================

    fun playTap() {
        // Ultra-crisp, snappy 0.035s responsive click
        playTone(0.035) { t ->
            val env = exp(-t * 110.0)
            sin(2.0 * PI * 980.0 * t) * env * 0.35
        }
        triggerVibrate(8, 45)
    }

    fun playCupMove() {
        playShuffle(GameMode.CLASSIC)
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

    fun playTabSwitch() {
        // Crisp, snappy, modern synth bubble sound
        playTone(0.08) { t ->
            val env = exp(-t * 50.0)
            val freq = 587.33 + sin(t * 150.0) * 350.0
            sin(2.0 * PI * freq * t) * env * 0.25
        }
        triggerVibrate(6, 45)
    }

    fun playComboMultiplier() {
        // High-energy rising arcade pitch-bend sound
        playTone(0.35) { t ->
            val env = exp(-t * 8.0)
            val freq = 600.0 + (t * 2200.0)
            (sin(2.0 * PI * freq * t) + sin(2.0 * PI * (freq * 1.5) * t) * 0.3) * env * 0.25
        }
        triggerPatternVibrate(longArrayOf(0, 30, 20, 30, 20, 50))
    }

    fun playThemeUnlock() {
        // Majestic cosmic ambient sweeping chord progression
        playTone(0.7) { t ->
            val env = exp(-t * 2.5)
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

    fun playCountdownTick() {
        playTone(0.03) { t ->
            val env = exp(-t * 120.0)
            sin(2.0 * PI * 1200.0 * t) * env * 0.3
        }
        triggerVibrate(8, 30)
    }

    fun playRewardClaim() {
        speakVoice("Claim Your Bonus!", pitch = 1.1f, speechRate = 1.0f)
        // Triumphant bonus reward claim fanfare ("Claim Your Bonus!")
        playTone(0.75) { t ->
            val env = exp(-t * 3.0)
            val step = (t * 6.0).toInt()
            val freqs = doubleArrayOf(587.33, 739.99, 880.0, 1174.66, 1479.98, 1760.0) // D5, F#5, A5, D6, F#6, A6
            val f = if (step < freqs.size) freqs[step] else freqs.last()
            val chime = sin(2.0 * PI * f * t) + sin(2.0 * PI * (f * 1.5) * t) * 0.3
            chime * env * 0.4
        }
        triggerPatternVibrate(longArrayOf(0, 40, 40, 40, 40, 120))
    }

    fun playWheelTick() {
        // Authentic wooden peg ticking sound matching lucky wheel spin speed
        playTone(0.025) { t ->
            val env = exp(-t * 180.0)
            val click = sin(2.0 * PI * 1400.0 * t) * 0.5 + sin(2.0 * PI * 420.0 * t) * 0.3
            click * env * 0.45
        }
        triggerVibrate(6, 40)
    }

    fun playCoinCollect() {
        // Crisp coin collection chime
        playTone(0.3) { t ->
            val env = exp(-t * 10.0)
            val f1 = 987.77 // B5
            val f2 = 1318.5 // E6
            val s1 = sin(2.0 * PI * f1 * t)
            val s2 = if (t >= 0.08) sin(2.0 * PI * f2 * (t - 0.08)) * exp(-(t - 0.08) * 10.0) else 0.0
            (s1 + s2) * env * 0.4
        }
        triggerVibrate(20, 80)
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
