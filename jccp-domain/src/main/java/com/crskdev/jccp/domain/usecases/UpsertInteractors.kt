@file:Suppress("FunctionName")

package com.crskdev.jccp.domain.usecases

import com.crskdev.jccp.domain.model.Book
import com.crskdev.jccp.domain.model.event.ErrorEvent
import com.crskdev.jccp.domain.repository.IBookRepository
import com.crskdev.jccp.domain.time.TimeProvider
import com.crskdev.jccp.domain.util.coroutines.EventBus
import com.crskdev.jccp.domain.validators.InputFormError
import com.crskdev.jccp.domain.validators.RuleFail
import com.crskdev.jccp.domain.validators.book.BookValidator
import com.crskdev.jccp.domain.validators.book.toBook
import com.crskdev.jccp.domain.validators.book.toForm
import kotlinx.coroutines.coroutineScope

/**
 * Created by Cristian Pela on 25.11.2019.
 */
class AddBookInteractor(private val repository: IBookRepository,
                        private val timeProvider: TimeProvider) {

    suspend operator fun invoke(formName: String, book: Book) = coroutineScope {
        UpsertInteractor(formName, book, repository, timeProvider)
    }
}

class UpdateBookInteractor(private val repository: IBookRepository,
                           private val timeProvider: TimeProvider) {

    suspend operator fun invoke(formName: String, book: Book, original: Book) = coroutineScope {
        if (book != original) {
            UpsertInteractor(formName, book, repository, timeProvider)
        }
    }
}

private suspend fun UpsertInteractor(formName: String, book: Book, repository: IBookRepository, timeProvider: TimeProvider) =
    coroutineScope {
        val bookForm = book.toForm(formName)
        val validation = BookValidator(bookForm, timeProvider.getTime().year)
        if (validation == RuleFail.Compound.NO_FORM_ERROR) {
            repository.save(bookForm.toBook())
        } else {
            coroutineContext[EventBus]
                ?.sendEvent(ErrorEvent(InputFormError(bookForm.formName, validation)))
        }
    }