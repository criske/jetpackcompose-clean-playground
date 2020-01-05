package com.crskdev.jccp.domain.schedule

import com.crskdev.jccp.domain.data.book.BookDataSourceLocal
import com.crskdev.jccp.domain.data.book.BookDataSourceRemote
import com.crskdev.jccp.domain.model.*
import com.crskdev.jccp.domain.schedule.book.BookScheduler
import com.crskdev.jccp.domain.testutils.TestDispatchers
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Cristian Pela on 04.11.2019.
 */
@ExperimentalCoroutinesApi
class BookSchedulerTest {

    private val dispatchers = TestDispatchers()

    @Test
    fun should_success_when_all_scheduled_were_done() = runBlockingTest {
        val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
        val bookDataSourceRemote = mockk<BookDataSourceRemote>(relaxed = true)
        val scheduler = BookScheduler(
            dispatchers,
            bookDataSourceLocal,
            bookDataSourceRemote
        )

        every {
            bookDataSourceLocal.findAllScheduled()
        } returns listOf(
            BookScheduled(ScheduleOp.INSERT, Book(Id(1, 0), "a", "a1", 1, "a2")),
            BookScheduled(ScheduleOp.INSERT, Book(Id(2, 0), "b", "b1", 1, "b2")),
            BookScheduled(ScheduleOp.UPDATE, Book(Id(3, 3), "c", "c1", 1, "c2")),
            BookScheduled(ScheduleOp.DELETE, Book(Id(4, 4), "d", "d1", 1, "d2"))
        )

        coEvery {
            bookDataSourceRemote.delete(any())
        } returns Result.success(Unit)

        val nextRemoteId = AtomicInteger(0)
        coEvery {
            bookDataSourceRemote.insert(any())
        } coAnswers {
            Result.success(UpsertResponse(nextRemoteId.incrementAndGet(), Date(1, 1)))
        }

        coEvery {
            bookDataSourceRemote.update(any())
        } coAnswers {
            val book = arg<Book>(0)
            Result.success(UpsertResponse(book.id.remote, Date(book.date.createdAt, 2)))
        }

        coEvery {
            bookDataSourceRemote.delete(any())
        } returns Result.success(Unit)

        assertEquals(ScheduleResult.SUCCESS, scheduler.execute())

        verify {
            bookDataSourceLocal.insert(Book(Id(1, 1), "a", "a1", 1, "a2", date = Date(1, 1)))
            bookDataSourceLocal.insert(Book(Id(2, 2), "b", "b1", 1, "b2", date = Date(1, 1)))
            bookDataSourceLocal.insert(Book(Id(3, 3), "c", "c1", 1, "c2", date = Date(0, 2)))
            bookDataSourceLocal.deleteScheduled(1)
            bookDataSourceLocal.deleteScheduled(2)
            bookDataSourceLocal.deleteScheduled(3)
            bookDataSourceLocal.deleteScheduled(4)
        }
    }

    @Test
    fun should_retry_when_at_least_one_scheduled_failed() = runBlockingTest {
        val bookDataSourceLocal = mockk<BookDataSourceLocal>(relaxed = true)
        val bookDataSourceRemote = mockk<BookDataSourceRemote>(relaxed = true)
        val scheduler = BookScheduler(
            dispatchers,
            bookDataSourceLocal,
            bookDataSourceRemote
        )

        every {
            bookDataSourceLocal.findAllScheduled()
        } returns listOf(
            BookScheduled(ScheduleOp.INSERT, Book(Id(1, 0), "a", "a1", 1, "a2")),
            BookScheduled(ScheduleOp.INSERT, Book(Id(2, 0), "b", "b1", 1, "b2")),
            BookScheduled(ScheduleOp.UPDATE, Book(Id(3, 3), "c", "c1", 1, "c2")),
            BookScheduled(ScheduleOp.DELETE, Book(Id(4, 4), "d", "d1", 1, "d2"))
        )

        coEvery {
            bookDataSourceRemote.delete(any())
        } returns Result.success(Unit)

        coEvery {
            bookDataSourceRemote.insert(any())
        } returns Result.failure(Error())

        coEvery {
            bookDataSourceRemote.update(any())
        } coAnswers {
            val book = arg<Book>(0)
            Result.success(UpsertResponse(book.id.remote, Date(book.date.createdAt, 2)))
        }

        coEvery {
            bookDataSourceRemote.delete(any())
        } returns Result.success(Unit)

        val execute = scheduler.execute()
        assertEquals(ScheduleResult.RETRY, execute)

        verify(exactly = 2) {
            bookDataSourceLocal.deleteScheduled(any())
        }

    }

}