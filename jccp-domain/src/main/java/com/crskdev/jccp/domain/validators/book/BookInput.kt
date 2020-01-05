package com.crskdev.jccp.domain.validators.book

import com.crskdev.jccp.domain.model.Book
import com.crskdev.jccp.domain.validators.Ignore
import com.crskdev.jccp.domain.validators.Input
import com.crskdev.jccp.domain.validators.input

/**
 * Created by Cristian Pela on 25.11.2019.
 */
 class BookInputForm(
    val formName: String,
    val id: Input,
    val author: Input,
    val title: Input,
    val year: Input,
    val genre: Input,
    val thumbnail: Input,
    val date: Input
)

 fun Book.toForm(formName: String): BookInputForm =
    BookInputForm(
        formName,
        Ignore input id,
        BookField.AUTHOR input author,
        BookField.TITLE input title,
        BookField.YEAR input year,
        BookField.GENRE input genre,
        Ignore input thumbnail,
        Ignore input date
    )

internal fun BookInputForm.toBook(): Book = run {
    Book(id(), author(), title(), year(), genre(), thumbnail(), date())
}