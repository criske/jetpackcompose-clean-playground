package com.crskdev.jccp.domain.util.extensions

/**
 * Created by Cristian Pela on 11.11.2019.
 */
fun Boolean.sign() = when (this) {
    true -> 1
    false -> -1
}