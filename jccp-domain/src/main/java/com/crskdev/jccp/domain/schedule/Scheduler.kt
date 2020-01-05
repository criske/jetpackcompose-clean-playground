package com.crskdev.jccp.domain.schedule

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Created by Cristian Pela on 06.11.2019.
 */
interface Scheduler {
    suspend fun execute(): ScheduleResult
}

suspend fun scheduleAll(vararg schedulers: Scheduler) = coroutineScope {
    schedulers
        .map { async { it.execute() } }
        .awaitAll()
        .fold(ScheduleResult.SUCCESS) { acc, curr -> acc + curr }
}