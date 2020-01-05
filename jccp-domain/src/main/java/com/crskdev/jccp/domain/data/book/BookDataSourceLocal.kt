package com.crskdev.jccp.domain.data.book

import com.crskdev.jccp.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Created by Cristian Pela on 01.11.2019.
 */

interface BookDataSourceLocal {
    fun observeAll(filter: BookFilter.OrderBy = BookFilter.OrderBy.DateUpdated()): Flow<List<Book>>
    fun observeAllScheduled(filter: BookFilter.OrderBy = BookFilter.OrderBy.DateUpdated()): Flow<List<BookScheduled>>
    fun clear(): Unit
    fun insert(books: List<Book>): Unit
    fun insert(book: Book): Long
    fun schedule(book: Book, op: ScheduleOp)
    fun find(localId: Int): Result<Book>
    fun findScheduled(localId: Int): Result<BookScheduled>
    fun findAllScheduled(): List<BookScheduled>
    fun delete(localId: Int)
    fun deleteScheduled(localId: Int): Boolean
}

