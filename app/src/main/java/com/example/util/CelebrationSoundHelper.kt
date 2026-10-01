package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.example.R
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Application-wide Singleton Audio Helper for task and celebration sound effects.
 * Uses a single cached SoundPool instance and handles media services defensively.
 */
object CelebrationSoundHelper {
    private const val TAG = "CelebrationSound"
    private var appContext: Context? = null
    private var soundPool: SoundPool? = null
    private var popSoundId: Int = 0
    private var isPopLoaded: Boolean = false
    private val isInitializing = AtomicBoolean(false)
    private var celebrationPlayer: MediaPlayer? = null

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
        ensureSoundPool()
    }

    private fun ensureSoundPool() {
        if (soundPool != null || isInitializing.getAndSet(true)) return
        val ctx = appContext ?: return
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val sp = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build()

            sp.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0 && sampleId == popSoundId) {
                    isPopLoaded = true
                }
            }

            popSoundId = sp.load(ctx, R.raw.confetti_pop, 1)
            soundPool = sp
        } catch (t: Throwable) {
            Log.w(TAG, "Audio hardware not available: ${t.message}")
            soundPool = null
        } finally {
            isInitializing.set(false)
        }
    }

    fun playConfettiPop() {
        try {
            ensureSoundPool()
            if (isPopLoaded && popSoundId != 0) {
                soundPool?.play(popSoundId, 0.85f, 0.85f, 1, 0, 1.0f)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Cannot play pop sound: ${t.message}")
        }
    }

    fun playDailyCelebration(onCompletion: () -> Unit = {}) {
        val ctx = appContext ?: return
        stopDailyCelebration()
        try {
            celebrationPlayer = MediaPlayer.create(ctx, R.raw.daily_completion_celebration)?.apply {
                setOnCompletionListener {
                    stopDailyCelebration()
                    onCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    stopDailyCelebration()
                    true
                }
                start()
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Cannot play celebration melody: ${t.message}")
        }
    }

    fun stopDailyCelebration() {
        try {
            celebrationPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.reset()
                player.release()
            }
        } catch (t: Throwable) {
            // Ignored
        } finally {
            celebrationPlayer = null
        }
    }
}
