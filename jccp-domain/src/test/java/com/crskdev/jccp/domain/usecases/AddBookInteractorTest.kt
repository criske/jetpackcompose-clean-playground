package com.crskdev.jccp.domain.usecases

import com.crskdev.jccp.domain.model.Book
import com.crskdev.jccp.domain.model.Id
import com.crskdev.jccp.domain.model.event.ErrorEvent
import com.crskdev.jccp.domain.repository.IBookRepository
import com.crskdev.jccp.domain.time.Time
import com.crskdev.jccp.domain.time.TimeProvider
import com.crskdev.jccp.domain.util.coroutines.EventBus
import com.crskdev.jccp.domain.validators.InputFormError
import com.crskdev.jccp.domain.validators.Rule
import com.crskdev.jccp.domain.validators.book.toForm
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

/**
 * Created by Cristian Pela on 25.11.2019.
 */
class AddBookInteractorTest {

    @Test
    fun should_validate_book() = runBlockingTest {
//        val repository = mockk<IBookRepository>()
//        val timeProvider = mockk<TimeProvider>()
//        every { timeProvider.getTime() } returns Time(0, 2019, 0, 0)
//        AddBookInteractor(repository, timeProvider)(Book(Id.common(0), "test", "", -1, "")
//                .toForm("ADD"))
    }
}