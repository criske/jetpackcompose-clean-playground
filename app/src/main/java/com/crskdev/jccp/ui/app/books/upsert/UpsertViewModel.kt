package com.crskdev.jccp.ui.app.books.upsert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.crskdev.jccp.R
import com.crskdev.jccp.domain.model.Book
import com.crskdev.jccp.domain.time.DefaultTimeProvider
import com.crskdev.jccp.domain.validators.RuleFail
import com.crskdev.jccp.domain.validators.RuleFail.Compound.Companion.NO_FORM_ERROR
import com.crskdev.jccp.domain.validators.book.BookField
import com.crskdev.jccp.domain.validators.book.BookFieldErrorIds
import com.crskdev.jccp.domain.validators.book.BookValidator
import com.crskdev.jccp.domain.validators.book.toForm
import com.crskdev.jccp.system.repository.WorkingRepository
import com.crskdev.jccp.ui.resources.StringRes
import com.crskdev.jccp.ui.resources.StringResTranslator

/**
 * Created by Cristian Pela on 19.11.2019.
 */
class UpsertViewModel(
    private val repository: WorkingRepository,
    private val stringResTranslator: StringResTranslator,
    private val androidErrorMapper: Map<Int, Int> = defaultAndroidErrorMapper) : ViewModel() {

    private val formErrorLiveData: LiveData<RuleFail.Compound> = MutableLiveData(NO_FORM_ERROR)

    val fieldErrorsLiveData: LiveData<Map<BookField, List<String>>> =
        Transformations.map(formErrorLiveData) { compound ->
            BookField.values().fold(mutableMapOf<BookField, List<String>>()) { map, field ->
                map.apply {
                    val translatedErrors = compound.findFailOfsFor(field)
                        .map {
                            val errorResId = androidErrorMapper[it.errorId]
                                ?: throw Exception("Error String Res Not Found for ID ${it.errorId}")
                            stringResTranslator.translate(StringRes(errorResId, it.errorArgs))
                        }
                    put(field, translatedErrors)
                }
            }
        }

    private val timeProvider = DefaultTimeProvider()

    fun add(book: Book, onSuccess: () -> Unit = {}) {
        upsertValidation(book) {
            onSuccess()
            repository.add(it)
        }
    }

    fun update(book: Book, onSuccess: () -> Unit = {}) {
        upsertValidation(book) {
            onSuccess()
            repository.update(book)
        }
    }

    private fun errConsume() {
        require(formErrorLiveData is MutableLiveData)
        formErrorLiveData.value = NO_FORM_ERROR
    }

    private inline fun upsertValidation(book: Book, onSuccess: (Book) -> Unit): Unit {
        require(formErrorLiveData is MutableLiveData)
        val validation = BookValidator(book.toForm("ADD"), timeProvider { year })
        if (validation.passes()) {
            errConsume()
            onSuccess(book)
        } else {
            formErrorLiveData.value = validation
        }
    }

}


private val defaultAndroidErrorMapper = mapOf<Int, Int>(
    BookFieldErrorIds.AUTHOR to R.string.err_author_text_empty,
    BookFieldErrorIds.TITLE to R.string.err_title_text_empty,
    BookFieldErrorIds.GENRE to R.string.err_genre_text_empty,
    BookFieldErrorIds.YEAR_LARGER_CURRENT to R.string.err_year_larger_current,
    BookFieldErrorIds.YEAR_NEGATIVE to R.string.err_year_negative,
    BookFieldErrorIds.YEAR_LESS_1900 to R.string.err_less_1900
)