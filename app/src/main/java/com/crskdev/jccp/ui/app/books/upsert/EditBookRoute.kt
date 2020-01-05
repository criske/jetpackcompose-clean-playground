package com.crskdev.jccp.ui.app.books.upsert

import android.os.Bundle
import androidx.core.os.bundleOf
import com.crskdev.jccp.domain.model.Book
import com.crskdev.jccp.ui.compose.extra.Route
import kotlinx.android.parcel.Parcelize

@Parcelize
class EditBookRoute(override val args: Bundle, override var uiSavedState: BookFormState) :
    Route<BookFormState>() {

    companion object {
        fun create(book: Book) =
            BookFormState(
                book.id.local,
                book.id.remote,
                book.title,
                book.author,
                book.genre,
                book.year.toString(),
                book.thumbnail,
                book.date.createdAt,
                book.date.updatedAt
            ).let { bfs -> EditBookRoute(bundleOf("EDIT_BOOK" to bfs), bfs) }
    }
}