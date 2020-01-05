package com.crskdev.jccp.domain.repository

import com.crskdev.jccp.domain.data.IndexDataSource
import com.crskdev.jccp.domain.data.book.BookDataSourceLocal
import com.crskdev.jccp.domain.data.book.BookDataSourceRemote
import com.crskdev.jccp.domain.data.book.BookFilterDataSource
import com.crskdev.jccp.domain.model.*
import com.crskdev.jccp.domain.util.coroutines.AbstractDispatchers
import com.crskdev.jccp.domain.util.extensions.sign
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

/**
 * Created by Cristian Pela on 08.11.2019.
 */

interface BookFilterDelegate {
    fun observeAll(): Flow<BooksFiltered>
    suspend fun fetchAll(filter: BookFilter.OrderBy = BookFilter.OrderBy.DateUpdated()): Result<Unit>
}

@FlowPreview
@ExperimentalCoroutinesApi
class BookFilterDelegateImpl(
    private val dispatchers: AbstractDispatchers,
    private val indexDataSource: IndexDataSource,
    private val bookDataSourceLocal: BookDataSourceLocal,
    private val bookDataSourceRemote: BookDataSourceRemote,
    private val bookFilterDataSource: BookFilterDataSource) : BookFilterDelegate {

    private val filterChannel = ConflatedBroadcastChannel<BookFilter.OrderBy>()

    init {
        val filter = bookFilterDataSource.loadFilter()
            .takeIf { it is BookFilter.OrderBy } as BookFilter.OrderBy?
            ?: BookFilter.OrderBy.DateUpdated()
        filterChannel.offer(filter)
    }

    override fun observeAll(): Flow<BooksFiltered> =
        filterChannel.asFlow().flatMapConcat { filter ->
            val booksFlow = bookDataSourceLocal.observeAll(filter)
            val booksScheduledFlow = bookDataSourceLocal
                .observeAllScheduled(filter)
            booksFlow.combine(booksScheduledFlow) { books, booksScheduled ->
                //filter from local the book that are scheduled
                val booksFromLocal = books.filter { b ->
                    !booksScheduled.any { bs -> bs.book.id == b.id }
                }
                //skip the scheduled books with op delete
                val booksFromScheduled = booksScheduled
                    .filter { it.op != ScheduleOp.DELETE }
                    .map { it.book }
                //join and filter them
                applyFilter(BooksFiltered(filter, booksFromLocal + booksFromScheduled ))
            }
        }.flowOn(dispatchers.Default())

    override suspend fun fetchAll(filter: BookFilter.OrderBy): Result<Unit> =
        coroutineScope {
            //signal to the offline data first, to avoid wait dead times on ux
            filterChannel.offer(filter)
            bookFilterDataSource.saveFilter(filter)

            val remoteResult =
                withContext(dispatchers.IO()) { bookDataSourceRemote.fetchAll(filter) }
            if (remoteResult.isSuccess) {
                var nextLocalId = withContext(dispatchers.Unconfined()) {
                    indexDataSource.nextId()
                }
                bookDataSourceLocal.clear()
                bookDataSourceLocal.insert(remoteResult
                    .getOrThrow()
                    .map { it.copy(id = it.id.copy(local = nextLocalId++)) })
                Result.success(Unit)
            } else {
                Result.failure(remoteResult.exceptionOrNull() ?: Error("Unknown Error"))
            }
        }
}

internal fun applyFilter(bf: BooksFiltered): BooksFiltered {
    val (filter, books) = bf
    val filteredBooks = when (filter) {
        is BookFilter.OrderBy.Title -> books.sortedWith(Comparator { o1, o2 ->
            filter.asc.sign() * o1.title.toLowerCase().compareTo(o2.title.toLowerCase())
        })
        is BookFilter.OrderBy.Author  -> books.sortedWith(Comparator { o1, o2 ->
            filter.asc.sign() * o1.author.toLowerCase().compareTo(o2.author.toLowerCase())
        })
        is BookFilter.OrderBy.DateCreated -> books.sortedWith(Comparator { o1, o2 ->
            filter.asc.sign() * o1.date.createdAt.compareTo(o2.date.createdAt)
        })
        is BookFilter.OrderBy.DateUpdated -> books.sortedWith(Comparator { o1, o2 ->
            filter.asc.sign() * o1.date.updatedAt.compareTo(o2.date.updatedAt)
        })
        else -> books
    }
    return BooksFiltered(filter, filteredBooks)
}