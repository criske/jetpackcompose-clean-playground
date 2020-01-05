package com.crskdev.jccp.domain.data

import com.crskdev.jccp.domain.model.Result

/**
 * Created by Cristian Pela on 04.11.2019.
 */
object NotFoundError : Error("Not found")

fun <T> Result<T>.isNotFound() = exceptionOrNull()?.equals(NotFoundError) == true