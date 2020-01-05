package com.crskdev.jccp.domain.schedule

import com.crskdev.jccp.domain.schedule.ScheduleResult.*
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by Cristian Pela on 04.11.2019.
 */
class ScheduleResultTest {

    @Test
    fun plus() {
        assertEquals(RETRY, RETRY + SUCCESS)
        assertEquals(SUCCESS, SUCCESS + SUCCESS + SUCCESS)
        assertEquals(RETRY, SUCCESS + SUCCESS + SUCCESS + RETRY + ABORT)
        assertEquals(SUCCESS, SUCCESS + SUCCESS + SUCCESS + ABORT)
        assertEquals(RETRY, RETRY + SUCCESS + SUCCESS + RETRY)
        assertEquals(ABORT, ABORT + ABORT)
    }
}