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

    private val sampleRate = 44100
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
        // Kept optional for voice cues
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
     * Synthesizes smooth audio samples in real-time with anti-click windowing
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
                val attackSamples = (sampleRate * 0.005).toInt().coerceAtLeast(1)
                val releaseSamples = (sampleRate * 0.015).toInt().coerceAtLeast(1)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    var sampleValue = generator(t)

                    // Anti-click attack and release envelope smoothing
                    if (i < attackSamples) {
                        sampleValue *= (i.toDouble() / attackSamples)
                    } else if (i > numSamples - releaseSamples) {
                        sampleValue *= ((numSamples - i).toDouble() / releaseSamples)
                    }

                    samples[i] = (sampleValue.coerceIn(-1.0, 1.0) * (Short.MAX_VALUE * 0.85)).toInt().toShort()
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
                // Safely handle audio buffer state
            }
        }
    }

    // ==========================================
    // 🔀 SUBTLE CUP SHUFFLING & SLIDE SOUNDS
    // ==========================================

    fun playShuffle(mode: GameMode = GameMode.CLASSIC, swapIndex: Int = 0) {
        when (mode) {
            GameMode.CLASSIC -> {
                // Subtle Wooden Felt Glide: Warm aerodynamic whoosh with soft table friction
                playTone(0.14) { t ->
                    val progress = t / 0.14
                    val env = sin(progress * PI) // Bell-curve envelope for natural motion
                    val whoosh = sin(2.0 * PI * (220.0 + sin(progress * PI) * 80.0) * t)
                    val feltFriction = (sin(2.0 * PI * 580.0 * t) * 0.12 + sin(2.0 * PI * 1120.0 * t) * 0.06)
                    (whoosh * 0.7 + feltFriction) * env * 0.28
                }
                triggerVibrate(10, 40)
            }
            GameMode.TIME_ATTACK -> {
                // Subtle Cyber Velocity Glide: Crisp, energetic electronic swipe
                playTone(0.10) { t ->
                    val progress = t / 0.10
                    val env = sin(progress * PI)
                    val sweepFreq = 480.0 + (progress * 520.0)
                    val synth = sin(2.0 * PI * sweepFreq * t) + sin(2.0 * PI * (sweepFreq * 1.5) * t) * 0.2
                    synth * env * 0.26
                }
                triggerVibrate(8, 50)
            }
            GameMode.ENDLESS -> {
                // Subtle Deep Ceramic/Obsidian Glide: Low resonant acoustic glide
                playTone(0.16) { t ->
                    val progress = t / 0.16
                    val env = sin(progress * PI)
                    val lowResonance = sin(2.0 * PI * 175.0 * t)
                    val ceramicOvertone = sin(2.0 * PI * 520.0 * t) * 0.18 + sin(2.0 * PI * 860.0 * t) * 0.08
                    (lowResonance * 0.75 + ceramicOvertone) * env * 0.30
                }
                triggerVibrate(12, 45)
            }
            GameMode.PERFECT_RUN -> {
                // Subtle Crystal Glass Sweep: Pure gentle shimmer glide
                playTone(0.13) { t ->
                    val progress = t / 0.13
                    val env = sin(progress * PI)
                    val crystal1 = sin(2.0 * PI * 880.0 * t) // A5
                    val crystal2 = sin(2.0 * PI * 1320.0 * t) * 0.25 // E6
                    val chimeOver = sin(2.0 * PI * 2640.0 * t) * 0.10 // E7
                    (crystal1 + crystal2 + chimeOver) * env * 0.22
                }
                triggerVibrate(8, 35)
            }
            GameMode.DAILY_CHALLENGE -> {
                // Subtle Golden Relic Glide: Warm resonant brass slide
                playTone(0.15) { t ->
                    val progress = t / 0.15
                    val env = sin(progress * PI)
                    val goldTone = sin(2.0 * PI * 330.0 * t)
                    val bellHarmonic = sin(2.0 * PI * 990.0 * t) * 0.15
                    (goldTone + bellHarmonic) * env * 0.28
                }
                triggerVibrate(11, 45)
            }
            GameMode.TUTORIAL -> {
                // Gentle, soft instructional whoosh
                playTone(0.18) { t ->
                    val progress = t / 0.18
                    val env = sin(progress * PI)
                    val softGlide = sin(2.0 * PI * 280.0 * t) + sin(2.0 * PI * 560.0 * t) * 0.15
                    softGlide * env * 0.24
                }
                triggerVibrate(8, 30)
            }
        }
    }

    // ==========================================
    // 🏆 SUBTLE & REWARDING WIN SOUNDS
    // ==========================================

    fun playWin(mode: GameMode = GameMode.CLASSIC) {
        DebugLogger.i("AUDIO", "playWin called for mode=$mode")
        when (mode) {
            GameMode.CLASSIC -> {
                // Triumphant, harmonic, warm major chord progression (C5 -> E5 -> G5 -> C6)
                playTone(0.65) { t ->
                    val env = exp(-t * 3.5)
                    val note = when {
                        t < 0.12 -> sin(2.0 * PI * 523.25 * t) // C5
                        t < 0.24 -> sin(2.0 * PI * 659.25 * t) // E5
                        t < 0.36 -> sin(2.0 * PI * 783.99 * t) // G5
                        else -> {
                            // Harmonious chord ring-out (C6 + E6 + G6)
                            sin(2.0 * PI * 1046.50 * t) * 0.7 +
                            sin(2.0 * PI * 1318.51 * t) * 0.45 +
                            sin(2.0 * PI * 1567.98 * t) * 0.3
                        }
                    }
                    val sparkle = if (t > 0.36) sin(2.0 * PI * 3135.96 * t) * 0.08 else 0.0
                    (note + sparkle) * env * 0.32
                }
                triggerPatternVibrate(longArrayOf(0, 35, 45, 50, 45, 90))
            }
            GameMode.TIME_ATTACK -> {
                // Crisp futuristic arpeggiated synth victory chime
                playTone(0.55) { t ->
                    val env = exp(-t * 4.2)
                    val step = (t * 12.0).toInt().coerceIn(0, 5)
                    val freqs = doubleArrayOf(587.33, 739.99, 880.00, 1174.66, 1479.98, 1760.00)
                    val f = freqs[step]
                    val synth = sin(2.0 * PI * f * t) + sin(2.0 * PI * (f * 2.0) * t) * 0.2
                    synth * env * 0.30
                }
                triggerPatternVibrate(longArrayOf(0, 25, 25, 25, 25, 60))
            }
            GameMode.ENDLESS -> {
                // Deep resonant triumph with rich warm harmonics
                playTone(0.70) { t ->
                    val env = exp(-t * 3.0)
                    val fundamental = sin(2.0 * PI * 261.63 * t) // C4
                    val fifth = sin(2.0 * PI * 392.00 * t) * 0.7 // G4
                    val octave = sin(2.0 * PI * 523.25 * t) * 0.5 // C5
                    val topChime = if (t > 0.2) sin(2.0 * PI * 1046.50 * t) * 0.4 else 0.0
                    (fundamental + fifth + octave + topChime) * env * 0.28
                }
                triggerPatternVibrate(longArrayOf(0, 45, 35, 65, 35, 100))
            }
            GameMode.PERFECT_RUN -> {
                // Sparkling crystalline glockenspiel celebration
                playTone(0.75) { t ->
                    val env = exp(-t * 2.8)
                    val step = (t * 8.0).toInt().coerceIn(0, 5)
                    val freqs = doubleArrayOf(1046.50, 1318.51, 1567.98, 2093.00, 2637.02, 3135.96)
                    val f = freqs[step]
                    val chime = sin(2.0 * PI * f * t) + sin(2.0 * PI * (f * 2.0) * t) * 0.18
                    chime * env * 0.28
                }
                triggerPatternVibrate(longArrayOf(0, 20, 25, 20, 25, 35, 25, 80))
            }
            GameMode.DAILY_CHALLENGE -> {
                // Majestic golden bell fanfare
                playTone(0.80) { t ->
                    val env = exp(-t * 2.6)
                    val base = when {
                        t < 0.18 -> sin(2.0 * PI * 440.0 * t) // A4
                        t < 0.36 -> sin(2.0 * PI * 554.37 * t) // C#5
                        t < 0.54 -> sin(2.0 * PI * 659.25 * t) // E5
                        else -> sin(2.0 * PI * 880.0 * t) * 0.8 + sin(2.0 * PI * 1108.73 * t) * 0.5 + sin(2.0 * PI * 1318.51 * t) * 0.4
                    }
                    val shimmer = sin(2.0 * PI * 2217.46 * t) * 0.12
                    (base + shimmer) * env * 0.28
                }
                triggerPatternVibrate(longArrayOf(0, 40, 35, 50, 35, 110))
            }
            GameMode.TUTORIAL -> {
                // Soft, uplifting praise arpeggio (C5 -> E5 -> G5 -> C6)
                playTone(0.60) { t ->
                    val env = exp(-t * 3.2)
                    val step = (t * 7.0).toInt().coerceIn(0, 3)
                    val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
                    val f = freqs[step]
                    val tone = sin(2.0 * PI * f * t) + sin(2.0 * PI * (f * 2.0) * t) * 0.15
                    tone * env * 0.28
                }
                triggerPatternVibrate(longArrayOf(0, 25, 25, 25, 25, 60))
            }
        }
    }

    // ==========================================
    // ❌ SUBTLE & GENTLE LOSE SOUNDS
    // ==========================================

    fun playLose(mode: GameMode = GameMode.CLASSIC) {
        DebugLogger.i("AUDIO", "playLose called for mode=$mode")
        when (mode) {
            GameMode.CLASSIC -> {
                // Soft, gentle descending minor progression (E5 -> D5 -> C5 -> A4)
                playTone(0.42) { t ->
                    val env = exp(-t * 4.2)
                    val step = (t * 8.0).toInt().coerceIn(0, 3)
                    val freqs = doubleArrayOf(659.25, 587.33, 523.25, 440.00)
                    val f = freqs[step]
                    val wave = sin(2.0 * PI * f * t) + sin(2.0 * PI * (f * 0.5) * t) * 0.2
                    wave * env * 0.25
                }
                triggerPatternVibrate(longArrayOf(0, 50, 30, 70))
            }
            GameMode.TIME_ATTACK -> {
                // Soft low-pass cyber power-down sweep
                playTone(0.35) { t ->
                    val env = exp(-t * 5.0)
                    val freq = (480.0 - t * 280.0).coerceAtLeast(140.0)
                    val synth = sin(2.0 * PI * freq * t) + sin(2.0 * PI * (freq * 0.5) * t) * 0.25
                    synth * env * 0.26
                }
                triggerPatternVibrate(longArrayOf(0, 60, 25, 60))
            }
            GameMode.ENDLESS -> {
                // Subtle warm gong fade with low resonance
                playTone(0.50) { t ->
                    val env = exp(-t * 3.8)
                    val lowNote = sin(2.0 * PI * (220.0 - t * 100.0).coerceAtLeast(90.0) * t)
                    val warmHarmonic = sin(2.0 * PI * 330.0 * t) * exp(-t * 8.0) * 0.3
                    (lowNote + warmHarmonic) * env * 0.26
                }
                triggerPatternVibrate(longArrayOf(0, 70, 35, 90))
            }
            GameMode.PERFECT_RUN -> {
                // Soft crystal glass tone fading out gently
                playTone(0.38) { t ->
                    val env = exp(-t * 6.0)
                    val crystalTone = sin(2.0 * PI * (880.0 - t * 350.0).coerceAtLeast(300.0) * t)
                    crystalTone * env * 0.22
                }
                triggerPatternVibrate(longArrayOf(0, 40, 25, 50))
            }
            GameMode.DAILY_CHALLENGE -> {
                // Warm temple bell descending minor cadence
                playTone(0.48) { t ->
                    val env = exp(-t * 3.8)
                    val bell = sin(2.0 * PI * (349.23 - t * 130.0).coerceAtLeast(150.0) * t)
                    val undertone = sin(2.0 * PI * 220.0 * t) * 0.25
                    (bell + undertone) * env * 0.25
                }
                triggerPatternVibrate(longArrayOf(0, 50, 30, 70))
            }
            GameMode.TUTORIAL -> {
                // Gentle encouraging soft tone
                playTone(0.36) { t ->
                    val env = exp(-t * 4.0)
                    val tone = sin(2.0 * PI * 392.0 * t) * 0.6 + sin(2.0 * PI * 329.63 * t) * 0.4
                    tone * env * 0.22
                }
                triggerPatternVibrate(longArrayOf(0, 30, 25, 40))
            }
        }
    }

    // ==========================================
    // 🔘 SUBTLE UI CLICKS, COIN REVEAL & TACTILE SOUNDS
    // ==========================================

    fun playTap() {
        // Ultra-crisp subtle tactile click
        playTone(0.03) { t ->
            val env = exp(-t * 120.0)
            sin(2.0 * PI * 1100.0 * t) * env * 0.25
        }
        triggerVibrate(6, 35)
    }

    fun playCupMove() {
        playShuffle(GameMode.CLASSIC)
    }

    fun playCupLand() {
        // Soft, organic wooden cup landing tap on cushioned table surface
        playTone(0.07) { t ->
            val env = exp(-t * 60.0)
            val baseThud = sin(2.0 * PI * 160.0 * t) * 0.7
            val woodClick = sin(2.0 * PI * 680.0 * t) * 0.25
            (baseThud + woodClick) * env * 0.28
        }
        triggerVibrate(8, 40)
    }

    fun playCoinReveal() {
        // Sparkling crystalline harmonic arpeggio with golden shimmer
        playTone(0.38) { t ->
            val env = exp(-t * 6.5)
            val f1 = 1174.66 // D6
            val f2 = 1479.98 // F#6
            val f3 = 1760.00 // A6
            val f4 = 2349.32 // D7

            val s1 = sin(2.0 * PI * f1 * t) * if (t < 0.08) 1.0 else exp(-(t - 0.08) * 7.0)
            val s2 = if (t >= 0.06) sin(2.0 * PI * f2 * (t - 0.06)) * exp(-(t - 0.06) * 7.0) else 0.0
            val s3 = if (t >= 0.12) sin(2.0 * PI * f3 * (t - 0.12)) * exp(-(t - 0.12) * 7.0) else 0.0
            val s4 = if (t >= 0.18) sin(2.0 * PI * f4 * (t - 0.18)) * exp(-(t - 0.18) * 5.5) else 0.0
            val shimmer = sin(2.0 * PI * 3520.0 * t) * 0.06

            (s1 * 0.6 + s2 * 0.7 + s3 * 0.8 + s4 * 0.9 + shimmer) * env * 0.30
        }
        triggerVibrate(15, 60)
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
