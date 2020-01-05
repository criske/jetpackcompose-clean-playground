package com.crskdev.jccp.domain.model.event

import com.crskdev.jccp.domain.util.coroutines.EventBus

/**
 * Created by Cristian Pela on 05.11.2019.
 */
sealed class InfoEvent: EventBus.Event{
    object Loading: InfoEvent()
    object Done: InfoEvent()
    sealed class Message: InfoEvent(){
        object Fetched: Message()
        object Inserted: Message()
        object Updated: Message()
        object Deleted: Message()
        sealed class Scheduled: Message(){
            object Insert: Scheduled()
            object Update: Scheduled()
            object Delete: Scheduled()
        }
        object Synchronized: Message()
    }
}