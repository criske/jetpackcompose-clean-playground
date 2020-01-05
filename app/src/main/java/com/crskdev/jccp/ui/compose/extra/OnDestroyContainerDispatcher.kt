package com.crskdev.jccp.ui.compose.extra

import androidx.compose.Ambient

/**
 * Created by Cristian Pela on 07.11.2019.
 */
val OnDestroyContext = Ambient.of<OnDestroyContainerDispatcher>()

class OnDestroyContainerDispatcher {

    private val listeners = mutableListOf<OnDestroyListener>()

    fun onDestroy(listener: OnDestroyListener) {
        listeners.add(listener)
    }

    fun dispatch() {
        listeners.forEach {
            it.invoke()
        }
    }

    fun removeListener(listener: OnDestroyListener) {
        listeners.remove(listener)
    }
}

typealias OnDestroyListener = () -> Unit
