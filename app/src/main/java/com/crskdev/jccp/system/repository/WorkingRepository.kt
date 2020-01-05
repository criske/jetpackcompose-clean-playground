package com.crskdev.jccp.system.repository

import com.crskdev.jccp.domain.model.*
import com.crskdev.jccp.domain.util.extensions.sign
import com.crskdev.jccp.system.sub.Emitter
import com.crskdev.jccp.system.sub.EmitterDelegate
import com.crskdev.jccp.system.sub.Subscriber
import com.crskdev.jccp.system.sub.Subscription
import kotlin.properties.Delegates

/**
 * Created by Cristian Pela on 19.11.2019.
 */
class WorkingRepository(initial: List<Book>,
                        private val delegate: EmitterDelegate<BooksFiltered> = EmitterDelegate()) :
    Emitter<BooksFiltered> by delegate {

    private var books = initial

    var filter: BookFilter by Delegates.observable<BookFilter>(BookFilter.OrderBy.DateUpdated()) { _, _, new ->
        books = applyFilter(new, books)
        notifySubscribers(BooksFiltered(new, books))
    }

    override fun subscribe(emitOnSubscribe: (() -> BooksFiltered)?, subscriber: Subscriber<BooksFiltered>): Subscription =
        delegate.subscribe({ BooksFiltered(filter, applyFilter(filter, books)) }, subscriber)

    fun snapshot(): List<Book> = books

    fun update(book: Book) {
        val date = book.date.copy(updatedAt = System.currentTimeMillis())
        val updatedBooks = books.filter { it.id.local != book.id.local } +
                book.copy(date = date)
        books = applyFilter(filter, updatedBooks)
        notifySubscribers(BooksFiltered(filter, books))
    }

    fun add(book: Book) {
        val date = System.currentTimeMillis().let { Date(it, it) }
        val id = nextId()
        val newBook = book.copy(id = Id.common(id), date = date)
        books = applyFilter(filter, books + newBook)
        notifySubscribers(BooksFiltered(filter, books))
    }

    fun remove(book: Book) {
        books = books.filter { it.id.local != book.id.local }
        notifySubscribers(BooksFiltered(filter, books))
    }

    fun find(localId: Int): Book? = books.find { it.id.local == localId }

    private fun nextId() = books.maxBy { it.id.local }?.id?.local?.plus(1) ?: 1

    private fun applyFilter(filter: BookFilter, books: List<Book>): List<Book> {
        return when (filter) {
            is BookFilter.OrderBy.Title -> books.sortedWith(Comparator { o1, o2 ->
                filter.asc.sign() * o1.title.toLowerCase().compareTo(o2.title.toLowerCase())
            })
            is BookFilter.OrderBy.Author -> books.sortedWith(Comparator { o1, o2 ->
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
    }
}

