package com.crskdev.jccp.domain.model

/**
 * Created by Cristian Pela on 08.11.2019.
 */
data class Date(val createdAt: Long, val updatedAt: Long){
    companion object {
        val NONE = Date(0, 0)
    }
}