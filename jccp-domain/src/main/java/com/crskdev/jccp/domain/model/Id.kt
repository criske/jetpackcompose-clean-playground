package com.crskdev.jccp.domain.model

/**
 * Created by Cristian Pela on 01.11.2019.
 */
data class Id(val local: Int = 0, val remote: Int = 0) {

    companion object {
        val NONE = Id()
        fun common(id: Int) = Id(id)
    }

    operator fun invoke(local: Int = 0, remote: Int = 0) = Id(local, remote)

    override fun toString(): String ="[ID] local: $local, remote: $remote"
}