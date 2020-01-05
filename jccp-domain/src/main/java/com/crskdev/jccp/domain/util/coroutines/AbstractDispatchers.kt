package com.crskdev.jccp.domain.util.coroutines

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Created by Cristian Pela on 01.11.2019.
 */
interface AbstractDispatchers {
    fun IO(): CoroutineDispatcher
    fun Main(): CoroutineDispatcher
    fun Unconfined(): CoroutineDispatcher
    fun Default(): CoroutineDispatcher
}