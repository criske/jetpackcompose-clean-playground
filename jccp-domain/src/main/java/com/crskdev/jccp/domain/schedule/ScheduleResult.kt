package com.crskdev.jccp.domain.schedule

/**
 * Created by Cristian Pela on 04.11.2019.
 */
enum class ScheduleResult(private val value: Int) {
    SUCCESS(1), RETRY(0), ABORT(-1);

    operator fun plus(other: ScheduleResult): ScheduleResult =
        if (other.value == -1) {
            this
        } else {
            when (this.value.and(other.value)) {
                1 -> SUCCESS
                0 -> RETRY
                else -> throw Exception("Invalid result")
            }
        }

}

