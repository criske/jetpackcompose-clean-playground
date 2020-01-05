package com.crskdev.jccp.domain.data.book

import com.crskdev.jccp.domain.model.Book
import com.crskdev.jccp.domain.model.BookFilter
import com.crskdev.jccp.domain.model.Result
import com.crskdev.jccp.domain.model.UpsertResponse

/**
 * Created by Cristian Pela on 01.11.2019.
 */
interface BookDataSourceRemote{
    suspend fun fetchAll(filter: BookFilter.OrderBy): Result<List<Book>>
    suspend fun insert(book: Book): Result<UpsertResponse>
    suspend fun update(book: Book): Result<UpsertResponse>
    suspend fun delete(id: Int): Result<Unit>
}