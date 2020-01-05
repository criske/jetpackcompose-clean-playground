package com.crskdev.jccp.domain.data

import kotlinx.coroutines.flow.Flow

/**
 * Created by Cristian Pela on 01.11.2019.
 */

interface IndexDataSource {
    //todo: make this un-suspended?
    suspend fun nextId(): Int
}
