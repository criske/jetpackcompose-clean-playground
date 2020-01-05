package com.crskdev.jccp.domain.model.event

import com.crskdev.jccp.domain.util.coroutines.EventBus

/**
 * Created by Cristian Pela on 05.11.2019.
 */
class ErrorEvent(val throwable: Throwable?): EventBus.Event