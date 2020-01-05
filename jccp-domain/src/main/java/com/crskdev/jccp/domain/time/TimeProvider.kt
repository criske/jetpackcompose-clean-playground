package com.crskdev.jccp.domain.time

import java.util.*

/**
 * Created by Cristian Pela on 08.11.2019.
 */
interface TimeProvider {
    fun getTime(): Time

    operator fun <N:Number> invoke(timeScope: Time.()-> N): N =
        getTime().run(timeScope)
}

data class Time(val timeMillis: Long, val year: Int, val month: Int, val day: Int)

class DefaultTimeProvider : TimeProvider {
    override fun getTime(): Time {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        return Time(
            now,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
}