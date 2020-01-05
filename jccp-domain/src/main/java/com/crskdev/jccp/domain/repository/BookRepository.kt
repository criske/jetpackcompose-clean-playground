package com.crskdev.jccp.domain.repository
/**
 * Created by Cristian Pela on 01.11.2019.
 */
import com.crskdev.jccp.domain.data.IndexDataSource
import com.crskdev.jccp.domain.data.NotFoundError
import com.crskdev.jccp.domain.data.book.BookDataSourceLocal
import com.crskdev.jccp.domain.data.book.BookDataSourceRemote
import com.crskdev.jccp.domain.model.*
import com.crskdev.jccp.domain.model.event.ErrorEvent
import com.crskdev.jccp.domain.model.event.InfoEvent
import com.crskdev.jccp.domain.schedule.ScheduleManager
import com.crskdev.jccp.domain.time.TimeProvider
import com.crskdev.jccp.domain.util.coroutines.AbstractDispatchers
import com.crskdev.jccp.domain.util.coroutines.EventBus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.contracts.ExperimentalContracts

interface IBookRepository {

    interface Observer {
        fun onChange(booksFiltered: BooksFiltered)
    }

    fun observeAll(): Flow<BooksFiltered>

    suspend fun observeAllFromChannel(observer: Observer)

    suspend fun requestFetchAll(filter: BookFilter.OrderBy = BookFilter.OrderBy.DateUpdated()): Unit

    suspend fun save(book: Book): Unit

    suspend fun delete(book: Book): Unit
}

@FlowPreview
@ExperimentalContracts
@ExperimentalCoroutinesApi
class BookRepository(
    private val dispatchers: AbstractDispatchers,
    private val indexDataSource: IndexDataSource,
    private val filterDelegate: BookFilterDelegate,
    private val bookDataSourceLocal: BookDataSourceLocal,
    private val bookDataSourceRemote: BookDataSourceRemote,
    private val scheduleManager: ScheduleManager,
    private val timeProvider: TimeProvider
) : IBookRepository {

    override fun observeAll(): Flow<BooksFiltered> = filterDelegate.observeAll()

    override suspend fun observeAllFromChannel(observer: IBookRepository.Observer) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun requestFetchAll(filter: BookFilter.OrderBy): Unit =
        coroutineScope {
            sendEvent(InfoEvent.Loading)
            val result = withContext(dispatchers.Unconfined()) {
                filterDelegate.fetchAll(filter)
            }
            sendEvent(InfoEvent.Done)
            if(result.isFailure){
                sendEvent(ErrorEvent(result.exceptionOrNull()))
            }else{
                sendEvent(InfoEvent.Message.Fetched)
            }
            Unit
        }

    override suspend fun save(book: Book): Unit = coroutineScope {
        if (book.id.local == 0) {
            internalSave(book)
        } else {
            internalUpdate(book)
        }
        Unit
    }

    override suspend fun delete(book: Book): Unit = coroutineScope {
        require(book.id.local > 0)
        bookDataSourceLocal.delete(book.id.local)
        if (book.id.remote > 0) {
            bookDataSourceLocal.schedule(book, ScheduleOp.DELETE)
            sendEvent(InfoEvent.Loading)
            val result = withContext(dispatchers.IO()) {
                bookDataSourceRemote.delete(book.id.remote)
            }
            sendEvent(InfoEvent.Done)
            if (result.isSuccess) {
                sendEvent(InfoEvent.Message.Deleted)
                bookDataSourceLocal.deleteScheduled(book.id.local)
            } else {
                sendEvent(InfoEvent.Message.Scheduled.Delete)
                scheduleManager.run()
            }
        } else {
            bookDataSourceLocal.deleteScheduled(book.id.local)
        }
        Unit
    }

    private suspend fun internalUpdate(book: Book): Unit = coroutineScope {
        require(book.id.local > 0)
        //schedule first
        val prevScheduledOp = bookDataSourceLocal.findScheduled(book.id.local).getOrNull()?.op
        val op = if (book.id.remote == 0 || prevScheduledOp?.equals(ScheduleOp.INSERT) == true) {
            ScheduleOp.INSERT
        } else {
            ScheduleOp.UPDATE
        }
        bookDataSourceLocal.delete(book.id.local)
        bookDataSourceLocal.schedule(
            book.copy(date = book.date.copy(updatedAt = timeProvider.getTime().timeMillis)),
            op
        )

        //try remote update
        sendEvent(InfoEvent.Loading)
        val remoteResult = withContext(dispatchers.IO()) {
            if (book.id.remote == 0) {
                bookDataSourceRemote.insert(book)
            } else {
                bookDataSourceRemote.update(book)
            }
        }
        sendEvent(InfoEvent.Done)

        if (remoteResult.isSuccess) {
            sendEvent(InfoEvent.Message.Updated)
            bookDataSourceLocal.deleteScheduled(book.id.local)
            val upsertResponse = remoteResult.getOrThrow()
            bookDataSourceLocal.insert(
                book.copy(
                    id = book.id.copy(remote = upsertResponse.id),
                    date = upsertResponse.date
                )
            )
        } else {
            val cause = remoteResult.exceptionOrNull()
            if (cause?.equals(NotFoundError) == true) {
                sendEvent(ErrorEvent(NotFoundError))
                bookDataSourceLocal.deleteScheduled(book.id.local)
            } else {
                sendEvent(InfoEvent.Message.Scheduled.Update)
                scheduleManager.run()
            }
        }
        Unit
    }


    private suspend fun internalSave(book: Book) = coroutineScope {
        require(book.id.local == 0)
        val nextLocalId = withContext(dispatchers.Unconfined()) {
            indexDataSource.nextId()
        }

        //schedule first
        val now = timeProvider.getTime().timeMillis
        bookDataSourceLocal.schedule(
            book.copy(id = book.id(nextLocalId), date = Date(now, now)),
            ScheduleOp.INSERT
        )

        //try remote
        sendEvent(InfoEvent.Loading)
        val remoteIdResult = withContext(dispatchers.IO()) {
            bookDataSourceRemote.insert(book)
        }
        sendEvent(InfoEvent.Done)

        if (remoteIdResult.isSuccess) {
            bookDataSourceLocal.deleteScheduled(book.id.local)
            val upsertResponse = remoteIdResult.getOrThrow()
            bookDataSourceLocal
                .insert(
                    book.copy(
                        id = Id(nextLocalId, upsertResponse.id),
                        date = upsertResponse.date
                    )
                )
        } else {
            sendEvent(InfoEvent.Message.Scheduled.Insert)
            scheduleManager.run()
        }
    }

    private suspend fun sendEvent(event: EventBus.Event) = coroutineScope {
        coroutineContext[EventBus]?.sendEvent(event)
    }
}