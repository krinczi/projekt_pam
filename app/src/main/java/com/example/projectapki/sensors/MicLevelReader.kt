package com.example.projectapki.sensors

import android.content.Context
import android.media.MediaRecorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.log10
import kotlin.math.max

object MicLevelReader {
    fun observeApproxDb(context: Context): Flow<Double> = flow {
        val outFile = context.cacheDir.resolve("mic_temp.3gp")

        val recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outFile.absolutePath)
        }

        recorder.prepare()
        recorder.start()

        try {
            while (true) {
                val amp = max(1, recorder.maxAmplitude)
                val db = 20.0 * log10(amp.toDouble())
                emit(db)
                delay(250)
            }
        } finally {
            try { recorder.stop() } catch (_: Exception) {}
            recorder.release()
            outFile.delete()
        }
    }
}
