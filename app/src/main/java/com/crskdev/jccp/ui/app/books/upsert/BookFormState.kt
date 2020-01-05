package com.crskdev.jccp.ui.app.books.upsert

import android.os.Parcelable
import com.crskdev.jccp.domain.model.Book
import com.crskdev.jccp.domain.model.Date
import com.crskdev.jccp.domain.model.Id
import kotlinx.android.parcel.Parcelize
import java.sql.Struct

@Parcelize
data class BookFormState(
    val idLocal: Int = 0,
    val idRemote: Int = 0,
    val title: String = "",
    val author: String = "",
    val genre: String = "",
    val year: String = "2019",
    val thumbnail: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) : Parcelable

fun BookFormState.toBook(): Book = Book(
    Id(idLocal, idRemote),
    title, author,
    year.toInt(),
    genre,
    thumbnail,
    Date(createdAt, updatedAt))
