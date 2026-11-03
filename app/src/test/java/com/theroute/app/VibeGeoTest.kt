package com.theroute.app

import com.theroute.app.engine.Geo
import com.theroute.app.engine.Vibe
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class VibeGeoTest {
    @Test fun distance200mRule() {
        // ~100m apart in New Orleans should pass, ~1km should not.
        assertTrue(Geo.withinMetres(29.95, -90.07, 29.9509, -90.07, 200.0))
        assertFalse(Geo.withinMetres(29.95, -90.07, 29.96, -90.07, 200.0))
    }
    @Test fun vibeBpmFromSyntheticBeats() {
        val rate = 44100
        // 120 BPM = 0.5s per beat, 5 seconds = 10 beats.
        val samples = ShortArray(rate * 5) { i ->
            val t = i.toDouble() / rate
            val beat = (t % 0.5) < 0.05
            if (beat) 20000 else 0
        }
        val bpm = Vibe.estimateBpm(samples, rate)
        assertNotNull(bpm)
        assertTrue("bpm=$bpm", bpm!! in 100.0..140.0)
        assertEquals("🔥", Vibe.iconFor(130.0))
        assertEquals("🎺", Vibe.iconFor(80.0))
    }
    @Test fun vibeTooQuietReturnsNull() {
        assertNull(Vibe.estimateBpm(ShortArray(44100) { 0 }, 44100))
    }
}
