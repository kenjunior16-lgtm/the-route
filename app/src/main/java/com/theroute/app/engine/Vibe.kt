package com.theroute.app.engine

/**
 * On-device BPM estimate from a short audio sample. The sample itself is
 * never stored or sent anywhere; only the resulting BPM number is kept.
 * Peak detection: smooth the amplitude envelope, find peaks above a
 * relative threshold, take the median peak-to-peak interval.
 */
object Vibe {
    fun estimateBpm(samples: ShortArray, sampleRate: Int): Double? {
        if (samples.isEmpty() || sampleRate <= 0) return null
        // Envelope: absolute value, smoothed over ~50ms windows.
        val window = maxOf(1, sampleRate / 20)
        val env = mutableListOf<Double>()
        var i = 0
        while (i < samples.size) {
            var sum = 0.0; var n = 0
            val end = minOf(samples.size, i + window)
            for (j in i until end) { sum += kotlin.math.abs(samples[j].toDouble()); n++ }
            env.add(if (n > 0) sum / n else 0.0)
            i = end
        }
        val max = env.maxOrNull() ?: return null
        if (max < 200.0) return null // too quiet to call a beat
        val threshold = max * 0.45
        val peaks = mutableListOf<Int>()
        for (k in 1 until env.size - 1) {
            if (env[k] > threshold && env[k] >= env[k - 1] && env[k] >= env[k + 1]) {
                if (peaks.isEmpty() || k - peaks.last() >= 4) peaks.add(k)
            }
        }
        if (peaks.size < 3) return null
        val intervals = peaks.zipWithNext { a, b -> (b - a).toDouble() }
        val median = intervals.sorted()[intervals.size / 2]
        if (median <= 0) return null
        val secondsPerWindow = window.toDouble() / sampleRate
        val secondsPerBeat = median * secondsPerWindow
        val bpm = 60.0 / secondsPerBeat
        return if (bpm in 40.0..240.0) bpm else null
    }

    fun iconFor(bpm: Double?): String = when {
        bpm == null -> "🎵"
        bpm < 90 -> "🎺"
        bpm > 120 -> "🔥"
        else -> "🎵"
    }
    fun labelFor(bpm: Double?): String = when {
        bpm == null -> "No clear beat yet — try again closer to the band."
        bpm < 90 -> "Dirge / slow vibe"
        bpm > 120 -> "Buckjump / high-energy vibe"
        else -> "Steady second line vibe"
    }
}
