package com.crskdev.jccp.ui.compose.adapt.arch

import androidx.compose.*
import androidx.lifecycle.*
import com.crskdev.jccp.ui.app.AppAmbient
import com.crskdev.jccp.ui.compose.fix.onDispose2

/**
 * Created by Cristian Pela on 07.11.2019.
 */

fun lifeCycleOwner() = effectOf<LifecycleOwner> {
    var registry: LifecycleRegistry? = null
    val owner = LifecycleOwner { registry!! }
    +onActive {
        registry = LifecycleRegistry(owner).apply {
            currentState = Lifecycle.State.STARTED
        }
    }
    +onDispose2 {
        registry?.currentState = Lifecycle.State.DESTROYED
    }
    +memo { owner }
}

fun <T> observe(liveData: LiveData<T>) = effectOf<T?> {
    val lifecycleOwner = +lifeCycleOwner()
    var state by +state<T?> { null }
    +onActive {
        liveData.observe(lifecycleOwner, Observer {
            state = it
        })
    }
    state
}

fun <T> observe(liveData: LiveData<T>, initial: T) = effectOf<T> {
    val lifecycleOwner = +lifeCycleOwner()
    var state by +state<T> { initial }
    +onActive {
        liveData.observe(lifecycleOwner, Observer {
            state = it
        })
    }
    state
}


@Composable
fun <T> LiveDataSub(lifecycleOwner: LifecycleOwner, liveData: LiveData<T>, onEmit : (T) -> Unit) {
    +onActive {
        liveData.observe(lifecycleOwner) {
            onEmit(it)
        }
    }
}


