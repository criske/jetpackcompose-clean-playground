package com.crskdev.jccp.system.repository

/**
 * Created by Cristian Pela on 11.11.2019.
 */
import com.crskdev.jccp.domain.model.*
import com.crskdev.jccp.domain.repository.IBookRepository
import com.crskdev.jccp.domain.util.extensions.sign
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.atomic.AtomicInteger

val INITIAL_LIST = listOf(
    Book(
        Id.common(1),
        "Dune",
        "Frank Herbert",
        2006,
        "fiction",
        "https://images-na.ssl-images-amazon.com/images/I/81BJ3OD3J-L.jpg",
        Date(System.currentTimeMillis(), System.currentTimeMillis())
    ),
    Book(
        Id.common(2),
        "Wrecking Ball (Diary of a Wimpy Kid Book 14)",
        "Jeff Kinney",
        2006,
        "fiction",
        "https://images-na.ssl-images-amazon.com/images/I/51S-kDF-fXL._SX340_BO1,204,203,200_.jpg",
        Date(System.currentTimeMillis(), System.currentTimeMillis())
    ),
    Book(
        Id.common(3),
        "Strange Planet",
        "Nathan W. Pyle",
        2019,
        "travel",
        "https://images-na.ssl-images-amazon.com/images/I/61zb65rJ5OL._AC_UL200_SR200,200_.jpg",
        Date(System.currentTimeMillis(), System.currentTimeMillis())
    ),
    Book(
        Id.common(4),
        "Guts",
        "Raina Telgemeier",
        2019,
        "health, fitness & dieting",
        "https://images-na.ssl-images-amazon.com/images/I/51rmgUYwLJL._AC_UL200_SR200,200_.jpg",
        Date(System.currentTimeMillis(), System.currentTimeMillis())
    ),
    Book(
        Id.common(5),
        "Where the Crawdads Sing",
        "Delia Owens",
        2018,
        "drama",
        "https://images-na.ssl-images-amazon.com/images/I/81WWiiLgEyL._AC_UL200_SR200,200_.jpg",
        Date(System.currentTimeMillis(), System.currentTimeMillis())
    ),
    Book(
        Id.common(6),
        "Talking to Strangers: What We Should Know About the People We Don't Know",
        "Malcolm Gladwell ",
        2018,
        "social",
        "https://images-na.ssl-images-amazon.com/images/I/41plY9%2B1OrL._SX342_.jpg",
        Date(System.currentTimeMillis(), System.currentTimeMillis())
    )
)

@FlowPreview
@ExperimentalCoroutinesApi
class DummyBookRepository(initial: List<Book> = INITIAL_LIST) : IBookRepository {

    private val idGen = AtomicInteger(0)

    private val booksChannel =
        ConflatedBroadcastChannel(BooksFiltered(BookFilter.OrderBy.DateUpdated(), initial))

    override fun observeAll(): Flow<BooksFiltered> {
//        return booksChannel.asFlow()
//            .map { bf ->applyFilter(bf) }
        TODO()
    }

    override suspend fun observeAllFromChannel(observer: IBookRepository.Observer) = coroutineScope {
        booksChannel.consumeEach {
            observer.onChange(applyFilter(it))
        }
        Unit
    }

    private fun applyFilter(bf: BooksFiltered): BooksFiltered {
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

    override suspend fun requestFetchAll(filter: BookFilter.OrderBy) {
        booksChannel.send(BooksFiltered(filter, booksChannel.value.books))
    }

    override suspend fun save(book: Book) = coroutineScope {
        val value = booksChannel.value
        val books = value.books +
                if (book.id.local == 0) book.copy(
                    id = book.id
                        .copy(local = idGen.incrementAndGet())
                ) else book
        booksChannel.send(BooksFiltered(value.filter, books))
        Unit
    }

    override suspend fun delete(book: Book) {
        val value = booksChannel.value
        val books = value.books.filter { it.id != book.id }
        booksChannel.send(BooksFiltered(value.filter, books))
    }

}
