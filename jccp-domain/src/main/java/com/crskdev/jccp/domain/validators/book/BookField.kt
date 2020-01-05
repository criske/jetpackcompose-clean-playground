package com.crskdev.jccp.domain.validators.book

import com.crskdev.jccp.domain.validators.Field

/**
 * Created by Cristian Pela on 25.11.2019.
 */
enum class BookField : Field{
    TITLE, AUTHOR, GENRE, YEAR
}

object BookFieldErrorIds{
    const val TITLE = 0
    const val AUTHOR = 1
    const val GENRE = 2
    const val YEAR_NEGATIVE = 3
    const val YEAR_LESS_1900 = 4
    const val YEAR_LARGER_CURRENT = 5
}