package com.crskdev.jccp.ui.compose.extra

import androidx.compose.Composable

/**
 * Created by Cristian Pela on 07.11.2019.
 */

object MultiAmbient{
    @Composable
    fun Provider(ambients: List<AmbientProvider<*>>, content: @Composable() () -> Unit) {
        ambients.fold(content, { current, ambient ->
            { ambient(current) }
        }).invoke()
    }
}


typealias  AmbientProvider<T> = @Composable() (@Composable() () -> Unit) -> Unit