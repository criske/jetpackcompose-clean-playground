package com.crskdev.jccp.domain.schedule.book

import com.crskdev.jccp.domain.data.book.BookDataSourceLocal
import com.crskdev.jccp.domain.data.book.BookDataSourceRemote
import com.crskdev.jccp.domain.data.isNotFound
import com.crskdev.jccp.domain.model.Book
import com.crskdev.jccp.domain.model.Id
import com.crskdev.jccp.domain.model.ScheduleOp
import com.crskdev.jccp.domain.model.getOrThrow
import com.crskdev.jccp.domain.schedule.ScheduleResult
import com.crskdev.jccp.domain.schedule.Scheduler
import com.crskdev.jccp.domain.util.coroutines.AbstractDispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Created by Cristian Pela on 04.11.2019.
 */
class BookScheduler(
    private val dispatchers: AbstractDispatchers,
    private val bookDataSourceLocal: BookDataSourceLocal,
    private val bookDataSourceRemote: BookDataSourceRemote): Scheduler{

    override suspend fun execute(): ScheduleResult = coroutineScope {
        bookDataSourceLocal.findAllScheduled().fold(ScheduleResult.SUCCESS) { acc, curr ->
            acc + when (curr.op) {
                ScheduleOp.INSERT -> doInsert(curr.book)
                ScheduleOp.UPDATE -> doUpdate(curr.book)
                ScheduleOp.DELETE -> doDelete(curr.book.id)
            }
        }
    }

    private suspend fun doInsert(book: Book): ScheduleResult = coroutineScope {
        var result = ScheduleResult.SUCCESS
        val remoteResult = withContext(dispatchers.IO()) {
            bookDataSourceRemote.insert(book)
        }
        if (remoteResult.isSuccess) {
            bookDataSourceLocal.deleteScheduled(book.id.local)
            val upsertResponse = remoteResult.getOrThrow()
            bookDataSourceLocal.insert(book.copy(id = book.id.copy(remote = upsertResponse.id), date = upsertResponse.date))
        } else {
            result = ScheduleResult.RETRY
        }
        result
    }

    private suspend fun doUpdate(book: Book): ScheduleResult = coroutineScope {
        require(book.id.remote > 0)
        val remoteResult = withContext(dispatchers.IO()) {
            bookDataSourceRemote.update(book)
        }
        when {
            remoteResult.isSuccess -> {
                bookDataSourceLocal.deleteScheduled(book.id.local)
                val upsertResponse = remoteResult.getOrThrow()
                bookDataSourceLocal.insert(book.copy(date = upsertResponse.date))
                ScheduleResult.SUCCESS
            }
            remoteResult.isNotFound() -> {
                bookDataSourceLocal.deleteScheduled(book.id.local)
                ScheduleResult.ABORT
            }
            else -> ScheduleResult.RETRY
        }
    }

    private suspend fun doDelete(id: Id): ScheduleResult = coroutineScope {
        val remoteResult = withContext(dispatchers.IO()) {
            bookDataSourceRemote.delete(id.remote)
        }
        when {
            remoteResult.isSuccess -> ScheduleResult.SUCCESS
            remoteResult.isNotFound() -> ScheduleResult.ABORT
            else -> ScheduleResult.RETRY
        }.apply {
            if (this != ScheduleResult.RETRY) {
                bookDataSourceLocal.deleteScheduled(id.local)
            }
        }
    }
}