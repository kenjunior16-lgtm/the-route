package com.theroute.app.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.theroute.app.engine.Vibe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 5-second on-device mic sample for Vibe Check.
 *  The buffer is analysed and discarded; nothing is recorded to a file
 *  or sent to a server. */
object VibeRecorder {
    suspend fun sampleBpm(): Double? = withContext(Dispatchers.IO) {
        val rate = 44100
        val channel = AudioFormat.CHANNEL_IN_MONO
        val format = AudioFormat.ENCODING_PCM_16BIT
        val minBuf = AudioRecord.getMinBufferSize(rate, channel, format)
        if (minBuf <= 0) return@withContext null
        val rec = try {
            AudioRecord(MediaRecorder.AudioSource.MIC, rate, channel, format, minBuf * 2)
        } catch (_: Exception) { return@withContext null }
        try {
            if (rec.state != AudioRecord.STATE_INITIALIZED) return@withContext null
            rec.startRecording()
            val total = rate * 5
            val buf = ShortArray(total)
            var read = 0
            while (read < total) {
                val n = rec.read(buf, read, total - read)
                if (n <= 0) break
                read += n
            }
            rec.stop()
            if (read < rate) null else Vibe.estimateBpm(buf.copyOf(read), rate)
        } catch (_: Exception) { null } finally {
            try { rec.release() } catch (_: Exception) {}
        }
    }
}
