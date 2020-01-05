package com.crskdev.jccp.domain.repository

import com.crskdev.jccp.domain.data.IndexDataSource
import com.crskdev.jccp.domain.data.book.BookDataSourceLocal
import com.crskdev.jccp.domain.data.book.BookDataSourceRemote
import com.crskdev.jccp.domain.data.book.BookFilterDataSource
import com.crskdev.jccp.domain.model.*
import com.crskdev.jccp.domain.testutils.TestDispatchers
import com.crskdev.jccp.domain.testutils.collectIntoList
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by Cristian Pela on 08.11.2019.
 */
@ExperimentalCoroutinesApi
@FlowPreview
class BookFilterDelegateImplTest {

    private val dispatchers = TestDispatchers()

    @Test
    fun should_select_the_correct_filter() = runBlockingTest {
        val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
        val bookFilterDataSource = mockk<BookFilterDataSource>()
        every {
            bookFilterDataSource.loadFilter()
        } returns BookFilter.None

        val bookFilterDelegate = BookFilterDelegateImpl(
            dispatchers,
            mockk(),
            bookDataSourceLocal,
            mockk(),
            bookFilterDataSource
        )
        coEvery {
            bookDataSourceLocal.observeAll(any())
        } coAnswers {
            val filter = arg<BookFilter.OrderBy>(0)
            val books = if (filter is BookFilter.OrderBy.DateUpdated) {
                listOf(
                    Book(Id(1, 1), "Title", "Foo", 2000, "action", date = Date(2, 2)),
                    Book(Id(2, 2), "Title", "Bar", 2001, "drama", date = Date(1, 1)),
                    Book(Id(3, 3), "Title", "Baz", 2002, "sf", date = Date(3, 3))
                )
            } else {
                emptyList()
            }
            flowOf(books)
        }

        coEvery {
            bookDataSourceLocal.observeAllScheduled(any())
        } returns flowOf(emptyList())

        val result = bookFilterDelegate.observeAll().collectIntoList(1).firstOrNull()

        assertTrue(result?.filter is BookFilter.OrderBy.DateUpdated)
        assertArrayEquals(
            listOf(
                Book(Id(3, 3), "Title", "Baz", 2002, "sf", date = Date(3, 3)),
                Book(Id(1, 1), "Title", "Foo", 2000, "action", date = Date(2, 2)),
                Book(Id(2, 2), "Title", "Bar", 2001, "drama", date = Date(1, 1))
            ).toTypedArray(),
            result?.books?.toTypedArray() ?: emptyArray()
        )
    }


    @Test
    fun should_ignore_the_delete_scheduled_and_use_upsert_scheduled_along_with_existent_books() =
        runBlockingTest {
            val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
            val bookFilterDataSource = mockk<BookFilterDataSource>()
            every {
                bookFilterDataSource.loadFilter()
            } returns BookFilter.None
            val bookFilterDelegate = BookFilterDelegateImpl(
                dispatchers,
                mockk(),
                bookDataSourceLocal,
                mockk(),
                bookFilterDataSource
            )
            coEvery {
                bookDataSourceLocal.observeAll(any())
            } coAnswers {
                val filter = arg<BookFilter.OrderBy>(0)
                val books = if (filter is BookFilter.OrderBy.DateUpdated) {
                    listOf(
                        Book(Id(1, 1), "Title", "Foo", 2000, "action", date = Date(2, 2)),
                        Book(Id(2, 2), "Title", "Bar", 2001, "drama", date = Date(1, 1)),
                        Book(Id(3, 3), "Title", "Baz", 2002, "sf", date = Date(3, 3))
                    )
                } else {
                    emptyList()
                }
                flowOf(books)
            }

            coEvery {
                bookDataSourceLocal.observeAllScheduled(any())
            } returns flowOf(
                listOf(
                    BookScheduled(
                        ScheduleOp.DELETE,
                        Book(Id(3, 3), "Title", "Baz", 2002, "sf", date = Date(3, 3))
                    ),
                    BookScheduled(
                        ScheduleOp.UPDATE,
                        Book(Id(2, 2), "Title2", "Bar", 2001, "drama", date = Date(1, 4))
                    ),
                    BookScheduled(
                        ScheduleOp.INSERT,
                        Book(Id(4, 0), "TitleNew", "New", 2001, "drama", date = Date(5, 5))
                    )
                )
            )

            val result = bookFilterDelegate.observeAll().collectIntoList(1).firstOrNull()

            assertTrue(result?.filter is BookFilter.OrderBy.DateUpdated)
            assertArrayEquals(
                listOf(
                    Book(Id(4, 0), "TitleNew", "New", 2001, "drama", date = Date(5, 5)),
                    Book(Id(2, 2), "Title2", "Bar", 2001, "drama", date = Date(1, 4)),
                    Book(Id(1, 1), "Title", "Foo", 2000, "action", date = Date(2, 2))
                ).toTypedArray(),
                result?.books?.toTypedArray() ?: emptyArray()
            )
        }

    @Test
    fun should_change_filters() = runBlockingTest {
        val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
        val bookDataSourceRemote = mockk<BookDataSourceRemote>(relaxed = true)
        val bookFilterDataSource = mockk<BookFilterDataSource>(relaxed = true)
        val indexDataSource = mockk<IndexDataSource>()
        every {
            bookFilterDataSource.loadFilter()
        } returns BookFilter.None
        val bookFilterDelegate = BookFilterDelegateImpl(
            dispatchers,
            indexDataSource,
            bookDataSourceLocal,
            bookDataSourceRemote,
            bookFilterDataSource
        )
        coEvery {
            indexDataSource.nextId()
        } returns 1
        coEvery {
            bookDataSourceLocal.observeAll(any())
        } returns flowOf(emptyList())
        coEvery {
            bookDataSourceRemote.fetchAll(any())
        } returns Result.success(emptyList())
        coEvery {
            bookDataSourceLocal.observeAllScheduled(any())
        } returns flowOf(emptyList())

        launch {
            bookFilterDelegate.observeAll().collectIndexed { index, value ->
                when (index) {
                    0 -> assertTrue(
                        "Filter By DateUpdated",
                        value.filter is BookFilter.OrderBy.DateUpdated
                    )
                    1 -> assertTrue("Filter By Title", value.filter is BookFilter.OrderBy.Title)
                    2 -> assertTrue("Filter By Author", value.filter is BookFilter.OrderBy.Author)
                    3 -> assertTrue(
                        "Filter By DateCreated",
                        value.filter is BookFilter.OrderBy.DateCreated
                    )
                    4 -> assertTrue(
                        "Filter By DateUpdated",
                        value.filter is BookFilter.OrderBy.DateUpdated
                    )
                    else -> coroutineContext[Job]?.cancelAndJoin()
                }
            }
        }

        bookFilterDelegate.fetchAll(BookFilter.OrderBy.Title())
        bookFilterDelegate.fetchAll(BookFilter.OrderBy.Author())
        bookFilterDelegate.fetchAll(BookFilter.OrderBy.DateCreated())
        bookFilterDelegate.fetchAll(BookFilter.OrderBy.DateUpdated())
        //force to close the flow : the fifth emitted item will cancel the job
        bookFilterDelegate.fetchAll(BookFilter.OrderBy.DateUpdated())

        coVerify(exactly = 5) {
            bookDataSourceLocal.clear()
        }

    }

    @Test
    fun should_change_filters_when_no_remote_response() = runBlockingTest {
        val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
        val bookDataSourceRemote = mockk<BookDataSourceRemote>(relaxed = true)
        val bookFilterDataSource = mockk<BookFilterDataSource>(relaxed = true)
        val indexDataSource = mockk<IndexDataSource>()
        every {
            bookFilterDataSource.loadFilter()
        } returns BookFilter.None
        val bookFilterDelegate = BookFilterDelegateImpl(
            dispatchers,
            indexDataSource,
            bookDataSourceLocal,
            bookDataSourceRemote,
            bookFilterDataSource
        )
        coEvery {
            indexDataSource.nextId()
        } returns 1
        coEvery {
            bookDataSourceLocal.observeAll(any())
        } returns flowOf(emptyList())
        coEvery {
            bookDataSourceRemote.fetchAll(any())
        } returns Result.failure(Error())
        coEvery {
            bookDataSourceLocal.observeAllScheduled(any())
        } returns flowOf(emptyList())

        launch {
            bookFilterDelegate.observeAll().collectIndexed { index, value ->
                when (index) {
                    0 -> assertTrue(
                        "Filter By DateUpdated",
                        value.filter is BookFilter.OrderBy.DateUpdated
                    )
                    1 -> assertTrue("Filter By Title", value.filter is BookFilter.OrderBy.Title)
                    2 -> assertTrue("Filter By Author", value.filter is BookFilter.OrderBy.Author)
                    3 -> assertTrue(
                        "Filter By DateCreated",
                        value.filter is BookFilter.OrderBy.DateCreated
                    )
                    4 -> assertTrue(
                        "Filter By DateUpdated",
                        value.filter is BookFilter.OrderBy.DateUpdated
                    )
                    else -> coroutineContext[Job]?.cancelAndJoin()
                }
            }
        }


        bookFilterDelegate.fetchAll(BookFilter.OrderBy.Title())
        bookFilterDelegate.fetchAll(BookFilter.OrderBy.Author())
        bookFilterDelegate.fetchAll(BookFilter.OrderBy.DateCreated())
        bookFilterDelegate.fetchAll(BookFilter.OrderBy.DateUpdated())
        //force to close the flow : the fifth emitted item will cancel the job
        bookFilterDelegate.fetchAll(BookFilter.OrderBy.DateUpdated())

        coVerify(exactly = 0) {
            bookDataSourceLocal.clear()
        }

    }

    @Test
    fun should_apply_filter() {
        val data = listOf(
            Book(Id(4, 0), "At", "Ba", 2001, "drama", date = Date(5, 5)),
            Book(Id(2, 2), "Bt", "Ca", 2001, "drama", date = Date(1, 4)),
            Book(Id(5, 5), "Gt", "Fa", 2000, "action", date = Date(2, 2)),
            Book(Id(1, 1), "Ct", "Aa", 2000, "action", date = Date(2, 2))
        )

        val f: (BookFilter.OrderBy, List<Book>, (Book) -> String) -> Array<String> =
            { filter, books, field ->
                applyFilter(BooksFiltered(filter, books))
                    .books
                    .map(field)
                    .toTypedArray()
            }

        assertArrayEquals(f(BookFilter.OrderBy.Author(), data) { it. author}, arrayOf("Aa", "Ba", "Ca", "Fa"))
        assertArrayEquals(f(BookFilter.OrderBy.Title(), data) { it. title}, arrayOf("At", "Bt", "Ct", "Gt"))
    }

}