package com.crskdev.jccp.domain.testutils

import com.crskdev.jccp.domain.util.coroutines.AbstractDispatchers
import kotlinx.coroutines.Dispatchers

/**
 * Created by Cristian Pela on 01.11.2019.
 */
class TestDispatchers :
    AbstractDispatchers {
    override fun IO() = Dispatchers.Unconfined

    override fun Main() = Dispatchers.Unconfined

    override fun Unconfined()  = Dispatchers.Unconfined

    override fun Default() = Dispatchers.Unconfined
}