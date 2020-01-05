package com.crskdev.jccp.ui.resources

/**
 * Created by Cristian Pela on 20.11.2019.
 */
class StringRes(val value: Int, val args: List<Any> = emptyList()){
    companion object{
        val EMPTY = StringRes(-1)
    }
}