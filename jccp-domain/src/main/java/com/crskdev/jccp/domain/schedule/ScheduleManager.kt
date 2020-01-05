package com.crskdev.jccp.domain.schedule

/**
 * Created by Cristian Pela on 06.11.2019.
 */
abstract class ScheduleManager {

    protected val ID = "JCCP-SCHEDULED-WORK"

    abstract fun run()
}