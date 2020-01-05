package com.crskdev.jccp.system.sub

/**
 * Created by Cristian Pela on 23.11.2019.
 */
interface Subscription{
    fun clear()
}

class SimpleSubscription<T>(private val subscribers: MutableList<Subscriber<T>>,
                                            private val subscriber: Subscriber<T>): Subscription{
    init { subscribers.add(subscriber) }

    override fun clear(){
        subscribers.remove(subscriber)
    }
}

class CompositeSubscription: Subscription{

    private val subscriptions = mutableListOf<Subscription>()

    operator fun plusAssign(subscription: Subscription){
        subscriptions.add(subscription)
    }

    override fun clear() {
        subscriptions.forEach {
            it.clear()
        }
    }
}