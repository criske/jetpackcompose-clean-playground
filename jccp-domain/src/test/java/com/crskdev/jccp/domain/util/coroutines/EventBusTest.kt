package com.crskdev.jccp.domain.util.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by Cristian Pela on 05.11.2019.
 */
@ExperimentalCoroutinesApi
class EventBusTest {

    data class TestEvent(val info: String) : EventBus.Event

    @Test
    fun should_listen_to_events() {

        val fooEvent = TestEvent("Foo")
        var called = false

        val eventBus = EventBus {
            assertEquals(fooEvent, it)
            called = true
        }

        runBlockingTest(eventBus) {
            coroutineContext[EventBus]?.sendEvent(fooEvent)
        }

        assertTrue("Listener was not called", called)
    }
}