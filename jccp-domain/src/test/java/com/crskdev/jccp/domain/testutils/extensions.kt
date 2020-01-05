package com.crskdev.jccp.domain.testutils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext

/**
 * Created by Cristian Pela on 08.11.2019.
 */
@ExperimentalCoroutinesApi
suspend  fun <T> Flow<T>.collectIntoList(n: Int) = coroutineScope {
    withContext(Dispatchers.Unconfined) {
        this@collectIntoList.take(1).toList(mutableListOf<T>())
    }
}