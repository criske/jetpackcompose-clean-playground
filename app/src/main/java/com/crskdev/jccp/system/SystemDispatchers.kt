package com.crskdev.jccp.system

import com.crskdev.jccp.domain.util.coroutines.AbstractDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Created by Cristian Pela on 11.11.2019.
 */
object SystemDispatchers : AbstractDispatchers {

    override fun IO(): CoroutineDispatcher = Dispatchers.IO

    override fun Main(): CoroutineDispatcher = Dispatchers.Main

    override fun Unconfined(): CoroutineDispatcher = Dispatchers.Unconfined

    override fun Default(): CoroutineDispatcher = Dispatchers.Default
}