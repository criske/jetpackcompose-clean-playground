package com.crskdev.jccp.ui.compose.fix

import androidx.compose.ambient
import androidx.compose.effectOf
import androidx.compose.onActive
import com.crskdev.jccp.ui.app.AppAmbient

/**
 * Created by Cristian Pela on 11.11.2019.
 */
fun onDispose2(block: () -> Unit) = effectOf<Unit> {
    val appAmbient = +ambient(AppAmbient)
    +onActive {
        appAmbient.onDestroyContainerDispatcher.onDestroy(block)
        onDispose {
            block.invoke()
            appAmbient.onDestroyContainerDispatcher.removeListener(block)
        }
    }
}