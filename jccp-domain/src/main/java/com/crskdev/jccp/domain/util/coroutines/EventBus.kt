@file:Suppress("unused")

package com.crskdev.jccp.domain.util.coroutines

import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Created by Cristian Pela on 05.11.2019.
 */
class EventBus(listener: EventBusListener = {}) :
    AbstractCoroutineContextElement(EventBus) {

    companion object Key : CoroutineContext.Key<EventBus>
    interface Event

    private val listeners = CopyOnWriteArrayList<EventBusListener>().apply {
        addIfAbsent(listener)
    }

    fun addListener(listener: EventBusListener) {
        listeners.addIfAbsent(listener)
    }

    fun removeListener(listener: EventBusListener) {
        listeners.remove(listener)
    }

    fun clear() {
        listeners.clear()
    }

    fun sendEvent(event: Event) {
        listeners.forEach {
            it(event)
        }
    }
}

typealias EventBusListener = (EventBus.Event) -> Unit