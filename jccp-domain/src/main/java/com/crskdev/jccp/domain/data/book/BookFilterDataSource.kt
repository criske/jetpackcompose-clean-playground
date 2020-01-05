package com.crskdev.jccp.domain.data.book

import com.crskdev.jccp.domain.model.BookFilter

/**
 * Created by Cristian Pela on 08.11.2019.
 */
interface BookFilterDataSource{

    fun loadFilter(): BookFilter

    fun saveFilter(filter: BookFilter)

}