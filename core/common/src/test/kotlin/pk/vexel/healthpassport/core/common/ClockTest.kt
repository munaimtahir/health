package pk.vexel.healthpassport.core.common

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ClockTest {
    @Test
    fun clockReturnsDeterministicTime() {
        val expected = Instant.parse("2026-01-01T00:00:00Z")
        assertEquals(expected, AppClock { expected }.now())
    }
}

