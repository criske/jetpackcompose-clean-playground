package com.crskdev.jccp.system.sub

import com.crskdev.jccp.domain.model.BooksFiltered

/**
 * Created by Cristian Pela on 23.11.2019.
 */
interface Emitter<T> {
    fun subscribe(emitOnSubscribe: (() -> T)? = null, subscriber: Subscriber<T>): Subscription

    fun notifySubscribers(item: T)

}

class EmitterDelegate<T>: Emitter<T>{

    private val subscribers = mutableListOf<Subscriber<T>>()

    override fun subscribe(emitOnSubscribe: (() -> T)?, subscriber: Subscriber<T>): Subscription {
        emitOnSubscribe?.run {
            subscriber.invoke(this())
        }
        return SimpleSubscription(subscribers, subscriber)
    }

    override fun notifySubscribers(item: T) {
        subscribers.forEach { it.invoke(item) }
    }

}

typealias  Subscriber<T> = (T) -> Unit