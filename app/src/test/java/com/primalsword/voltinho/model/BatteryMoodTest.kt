package com.primalsword.voltinho.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryMoodTest {
    @Test
    fun `maps battery thresholds consistently`() {
        assertEquals(BatteryMood.CELEBRATING, BatteryMood.from(100, charging = true, full = true))
        assertEquals(BatteryMood.CHARGING, BatteryMood.from(10, charging = true, full = false))
        assertEquals(BatteryMood.ENERGETIC, BatteryMood.from(80, charging = false, full = false))
        assertEquals(BatteryMood.CONTENT, BatteryMood.from(50, charging = false, full = false))
        assertEquals(BatteryMood.TIRED, BatteryMood.from(20, charging = false, full = false))
        assertEquals(BatteryMood.CRITICAL, BatteryMood.from(19, charging = false, full = false))
    }
}
