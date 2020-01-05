package com.crskdev.jccp.domain.model

/**
 * Created by Cristian Pela on 01.11.2019.
 */

data class Book(
    val id: Id = Id.NONE,
    val title: String,
    val author: String,
    val year: Int,
    val genre: String,
    val thumbnail: String? = null,
    val date: Date = Date.NONE
)

data class BookScheduled(val op: ScheduleOp, val book: Book)

enum class ScheduleOp { INSERT, UPDATE, DELETE }

sealed class BookFilter {
    sealed class OrderBy(val asc: Boolean = true) : BookFilter() {
        class Title(asc: Boolean = true) : OrderBy(asc) {
            override fun not(): OrderBy = Title(!asc)
        }

        class Author(asc: Boolean = true) : OrderBy(asc) {
            override fun not(): OrderBy = Author(!asc)
        }

        class DateCreated(asc: Boolean = false) : OrderBy(asc) {
            override fun not(): OrderBy = DateCreated(!asc)
        }

        class DateUpdated(asc: Boolean = false) : OrderBy(asc) {
            override fun not(): OrderBy = DateUpdated(!asc)
        }

        abstract operator fun not(): OrderBy
    }

    object None : BookFilter()

    override fun toString(): String = this.javaClass.simpleName
}

data class BooksFiltered(val filter: BookFilter, val books: List<Book>)
