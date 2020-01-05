package com.crskdev.jccp.domain.repository

import com.crskdev.jccp.domain.data.IndexDataSource
import com.crskdev.jccp.domain.data.NotFoundError
import com.crskdev.jccp.domain.data.book.BookDataSourceLocal
import com.crskdev.jccp.domain.data.book.BookDataSourceRemote
import com.crskdev.jccp.domain.model.*
import com.crskdev.jccp.domain.schedule.ScheduleManager
import com.crskdev.jccp.domain.testutils.TestDispatchers
import com.crskdev.jccp.domain.time.Time
import com.crskdev.jccp.domain.time.TimeProvider
import com.crskdev.jccp.domain.util.coroutines.EventBus
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import kotlin.contracts.ExperimentalContracts

/**
 * Created by Cristian Pela on 01.11.2019.
 */
@ExperimentalContracts
@ExperimentalCoroutinesApi
class BookRepositoryTest {

    private val dispatchers = TestDispatchers()

    //TODO: remove observe all test. Those are delegate to the BookFilterDelegate

//    @Test
//    fun should_fetch_remote_and_insert_local() = runBlockingTest {
//        val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
//        val bookDataSourceRemote = mockk<BookDataSourceRemote>(relaxed = true)
//        val indexDataSource = mockk<IndexDataSource>(relaxed = true)
//
//        val remoteResult = listOf(
//            Book(Id(remote = 1), "Title", "Foo", 2000, "action"),
//            Book(Id(remote = 2), "Title", "Bar", 2001, "drama"),
//            Book(Id(remote = 3), "Title", "Baz", 2002, "sf")
//        )
//        coEvery { indexDataSource.nextId() } returns 1
//        coEvery { bookDataSourceRemote.fetchAll(any()) } returns Result.success(remoteResult)
//
//        val bookRepository =
//            BookRepository(
//                dispatchers,
//                indexDataSource,
//                bookDataSourceLocal,
//                bookDataSourceRemote,
//                mockk(),
//                mockk()
//            )
//
//        bookRepository.requestFetchAll()
//
//        coVerify {
//            bookDataSourceLocal.clear()
//        }
//        var nextLocalId = 1;
//        coVerify {
//            bookDataSourceLocal.insert(remoteResult.map { it.copy(id = it.id.copy(local = nextLocalId++)) })
//        }
//    }

//    @Test
//    fun should_observe_the_books_joined_with_scheduled_books() = runBlockingTest {
//        val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
//        val bookRepository =
//            BookRepository(
//                dispatchers,
//                mockk(),
//                mockk(),
//                bookDataSourceLocal,
//                mockk(),
//                mockk(),
//                mockk()
//            )
//
//        coEvery {
//            bookDataSourceLocal.observeAll()
//        } returns flowOf(
//            listOf(
//                Book(Id(2, 2), "Title", "Foo", 1999, "sf"),
//                Book(Id(3, 3), "Title", "Foo", 1999, "sf")
//            )
//        )
//
//        coEvery {
//            bookDataSourceLocal.observeAllScheduled()
//        } returns flowOf(
//            listOf(
//                BookScheduled(ScheduleOp.INSERT, Book(Id(5, 0), "Title", "Foo", 1999, "sf")),
//                BookScheduled(ScheduleOp.UPDATE, Book(Id(1, 1), "Title#1", "Foo", 1999, "sf")),
//                BookScheduled(ScheduleOp.DELETE, Book(Id(4, 4), "Title", "Foo", 1999, "sf"))
//            )
//        )
//
//        val result = mutableListOf<List<Book>>()
//            .let {
//                bookRepository.observeAll().take(1).toList(it)
//            }.first().toTypedArray()
//
//        assertArrayEquals(
//            arrayOf(
//                Book(Id(1, 1), "Title#1", "Foo", 1999, "sf"),
//                Book(Id(2, 2), "Title", "Foo", 1999, "sf"),
//                Book(Id(3, 3), "Title", "Foo", 1999, "sf"),
//                Book(Id(5, 0), "Title", "Foo", 1999, "sf")
//            ), result
//        )
//
//    }


    @Test
    fun should_insert_without_remote_schedule() = runBlockingTest {
        val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
        val bookDataSourceRemote = mockk<BookDataSourceRemote>(relaxed = true)
        val indexDataSource = mockk<IndexDataSource>(relaxed = true)
        val timeProvider = mockk<TimeProvider>()

        coEvery { indexDataSource.nextId() } returns 1
        coEvery { bookDataSourceRemote.insert(any()) } returns Result.success(
            UpsertResponse(2, Date(1, 1))
        )
        every { timeProvider.getTime() } returns Time(1, 0, 0, 0)

        val bookRepository =
            BookRepository(
                dispatchers,
                indexDataSource,
                mockk(),
                bookDataSourceLocal,
                bookDataSourceRemote,
                mockk(),
                timeProvider
            )
        val book = Book(title = "Title", author = "Foo", year = 1000, genre = "foo");

        bookRepository.save(book)

        val expected = book.copy(id = Id(1, 2), date = Date(1, 1))

        coVerify {
            bookDataSourceLocal.schedule(
                book.copy(id = Id(1, 0), date = Date(1, 1)),
                ScheduleOp.INSERT
            )
            bookDataSourceLocal.deleteScheduled(book.id.local)
            bookDataSourceLocal.insert(expected)
        }
    }

    @Test
    fun should_insert_with_remote_schedule() = runBlockingTest {
        val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
        val bookDataSourceRemote = mockk<BookDataSourceRemote>(relaxed = true)
        val indexDataSource = mockk<IndexDataSource>(relaxed = true)
        val scheduleManager = mockk<ScheduleManager>(relaxed = true)
        val timeProvider = mockk<TimeProvider>()

        coEvery { indexDataSource.nextId() } returns 1
        coEvery { bookDataSourceRemote.insert(any()) } returns Result.failure(Error())
        every { timeProvider.getTime() } returns Time(1, 0, 0, 0)

        val bookRepository =
            BookRepository(
                dispatchers,
                indexDataSource,
                mockk(),
                bookDataSourceLocal,
                bookDataSourceRemote,
                scheduleManager,
                timeProvider
            )
        val book = Book(title = "Title", author = "Foo", year = 1000, genre = "foo")

        bookRepository.save(book)

        val expected = book.copy(id = Id(1, 0), date = Date(1, 1))

        coVerify(exactly = 0) {
            bookDataSourceLocal.deleteScheduled(book.id.local)
            bookDataSourceLocal.insert(expected)
        }

        coVerify {
            bookDataSourceLocal.schedule(expected, ScheduleOp.INSERT)
        }

        verify(exactly = 1) {
            scheduleManager.run()
        }
    }


    @Test
    fun should_update_with_remote_schedule() = runBlockingTest(EventBus {
        println(it)
    }) {
        val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
        val bookDataSourceRemote = mockk<BookDataSourceRemote>(relaxed = true)
        val scheduleManager = mockk<ScheduleManager>(relaxed = true)
        val timeProvider = mockk<TimeProvider>()
        val bookRepository =
            BookRepository(
                dispatchers,
                mockk(),
                mockk(),
                bookDataSourceLocal,
                bookDataSourceRemote,
                scheduleManager,
                timeProvider
            )

        coEvery {
            bookDataSourceRemote.update(any())
        } returns Result.failure(Error())

        every { timeProvider.getTime() } returns Time(2, 0, 0, 0)

        every {
            bookDataSourceLocal.findScheduled(1)
        } returns Result.failure<BookScheduled>(Error()) as Result<BookScheduled>

        val book = Book(Id(1, 1), "title", "foo", 1999, "sf")
        bookRepository.save(book)

        coVerify(exactly = 0) {
            bookDataSourceLocal.insert(book);
            bookDataSourceLocal.deleteScheduled(book.id.local)
        }
        coVerify {
            bookDataSourceRemote.update(any())
            bookDataSourceLocal.schedule(
                book.copy(date = book.date.copy(updatedAt = 2)),
                ScheduleOp.UPDATE
            );
        }
        verify(exactly = 1) {
            scheduleManager.run()
        }
    }


    @Test
    fun should_update_with_no_remote_schedule() = runBlockingTest {
        val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
        val bookDataSourceRemote = mockk<BookDataSourceRemote>(relaxed = true)
        val timeProvider = mockk<TimeProvider>()
        val bookRepository =
            BookRepository(
                dispatchers,
                mockk(),
                mockk(),
                bookDataSourceLocal,
                bookDataSourceRemote,
                mockk(),
                timeProvider
            )

        coEvery {
            bookDataSourceRemote.update(any())
        } returns Result.success(UpsertResponse(1, Date(1, 2)))

        every {
            bookDataSourceLocal.findScheduled(1)
        } returns Result.failure(Error())

        every {
            timeProvider.getTime()
        } returns Time(1, 0, 0, 0)

        val book = Book(Id(1, 1), "title", "foo", 1999, "sf")
        bookRepository.save(book)

        coVerify {
            bookDataSourceRemote.update(book)
        }
        coVerify {
            bookDataSourceLocal.deleteScheduled(book.id.local)
            bookDataSourceLocal.insert(book.copy(date = Date(1, 2)))
        }
    }

    @Test
    fun should_update_with_no_remote_schedule_and_the_book_is_already_scheduled_for_insert() =
        runBlockingTest {
            val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
            val bookDataSourceRemote = mockk<BookDataSourceRemote>(relaxed = true)
            val timeProvider = mockk<TimeProvider>()
            val bookRepository =
                BookRepository(
                    dispatchers,
                    mockk(),
                    mockk(),
                    bookDataSourceLocal,
                    bookDataSourceRemote,
                    mockk(),
                    timeProvider
                )

            coEvery {
                bookDataSourceRemote.insert(any())
            } returns Result.success(UpsertResponse(1, Date(1, 2)))

            every {
                timeProvider.getTime()
            } returns Time(1, 0, 0, 0)

            val book = Book(Id(1, 0), "title", "foo", 1999, "sf")
            coEvery {
                bookDataSourceLocal.findScheduled(1)
            } returns Result.success(BookScheduled(ScheduleOp.INSERT, book))

            bookRepository.save(book)

            coVerify {
                bookDataSourceRemote.insert(book)
                bookDataSourceLocal.deleteScheduled(book.id.local)
                bookDataSourceLocal.insert(
                    book.copy(
                        id = book.id.copy(remote = 1),
                        date = Date(1, 2)
                    )
                )
            }
        }

    @Test
    fun should_update_with_remote_schedule_and_the_book_is_already_scheduled_for_insert() =
        runBlockingTest {
            val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
            val bookDataSourceRemote = mockk<BookDataSourceRemote>(relaxed = true)
            val scheduleManager = mockk<ScheduleManager>(relaxed = true)
            val timeProvider = mockk<TimeProvider>()
            val bookRepository =
                BookRepository(
                    dispatchers,
                    mockk(),
                    mockk(),
                    bookDataSourceLocal,
                    bookDataSourceRemote,
                    scheduleManager,
                    timeProvider
                )

            coEvery {
                bookDataSourceRemote.insert(any())
            } returns Result.failure(Error())

            every {
                timeProvider.getTime()
            } returns Time(1, 0, 0, 0)

            val book = Book(Id(1, 0), "title", "foo", 1999, "sf", date = Date(1, 1))
            coEvery {
                bookDataSourceLocal.findScheduled(1)
            } returns Result.success(BookScheduled(ScheduleOp.INSERT, book))

            bookRepository.save(book)

            coVerify {
                bookDataSourceLocal.schedule(book.copy(date = Date(1, 1)), ScheduleOp.INSERT);
            }

            verify(exactly = 1) {
                scheduleManager.run()
            }
        }

    @Test
    fun should_not_update_with_schedule_if_entry_was_removed_on_remote() =
        runBlockingTest {
            val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
            val bookDataSourceRemote = mockk<BookDataSourceRemote>(relaxed = true)
            val timeProvider = mockk<TimeProvider>()
            val bookRepository =
                BookRepository(
                    dispatchers,
                    mockk(),
                    mockk(),
                    bookDataSourceLocal,
                    bookDataSourceRemote,
                    mockk(),
                    timeProvider
                )

            every {
                timeProvider.getTime()
            } returns Time(1, 0, 0, 0)

            coEvery {
                bookDataSourceRemote.update(any())
            } returns Result.failure(NotFoundError)

            val book = Book(Id(1, 1), "title", "foo", 1999, "sf")
            coEvery {
                bookDataSourceLocal.findScheduled(1)
            } returns Result.failure(Error())

            bookRepository.save(book)

            coVerify {
                bookDataSourceLocal.schedule(book.copy(date = Date(0, 1)), ScheduleOp.UPDATE);
                bookDataSourceLocal.deleteScheduled(book.id.local)
            }
        }

    @Test
    fun should_delete_having_remote_id_not_scheduled() = runBlockingTest {
        val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
        val bookDataSourceRemote = mockk<BookDataSourceRemote>(relaxed = true)
        val bookRepository =
            BookRepository(
                dispatchers,
                mockk(),
                mockk(),
                bookDataSourceLocal,
                bookDataSourceRemote,
                mockk(),
                mockk()
            )

        coEvery {
            bookDataSourceRemote.delete(any())
        } returns Result.success(Unit)

        val book = Book(Id(1, 1), "title", "foo", 1999, "sf")

        bookRepository.delete(book)

        coVerify {
            bookDataSourceLocal.delete(1)
            bookDataSourceLocal.schedule(book, ScheduleOp.DELETE)
            bookDataSourceLocal.deleteScheduled(1)
        }

    }

    @Test
    fun should_delete_having_remote_id_scheduled() = runBlockingTest {
        val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
        val bookDataSourceRemote = mockk<BookDataSourceRemote>(relaxed = true)
        val scheduleManager = mockk<ScheduleManager>(relaxed = true)
        val bookRepository =
            BookRepository(
                dispatchers,
                mockk(),
                mockk(),
                bookDataSourceLocal,
                bookDataSourceRemote,
                scheduleManager,
                mockk()
            )

        coEvery {
            bookDataSourceRemote.delete(any())
        } returns Result.failure(Error())

        val book = Book(Id(1, 1), "title", "foo", 1999, "sf")

        bookRepository.delete(book)

        coVerify {
            bookDataSourceLocal.delete(1)
            bookDataSourceLocal.schedule(book, ScheduleOp.DELETE)
        }

        coVerify(exactly = 0) {
            bookDataSourceLocal.deleteScheduled(1)
        }

        verify(exactly = 1) {
            scheduleManager.run()
        }

    }

    @Test
    fun should_delete_not_having_remote_id() = runBlockingTest {
        val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
        val bookDataSourceRemote = mockk<BookDataSourceRemote>(relaxed = true)
        val bookRepository =
            BookRepository(
                dispatchers,
                mockk(),
                mockk(),
                bookDataSourceLocal,
                bookDataSourceRemote,
                mockk(),
                mockk()
            )

        val book = Book(Id(1, 0), "title", "foo", 1999, "sf")

        bookRepository.delete(book)

        coVerify {
            bookDataSourceLocal.delete(1)
            bookDataSourceLocal.deleteScheduled(1)
        }

        coVerify(exactly = 0) {
            bookDataSourceLocal.schedule(book, ScheduleOp.DELETE)
            bookDataSourceRemote.delete(any())
        }

    }
}