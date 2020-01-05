package com.crskdev.jccp.ui.app.books.list

import com.crskdev.jccp.domain.model.BookFilter
import com.crskdev.jccp.ui.resources.StringRes
import org.junit.Test

import org.junit.Assert.*

/**
 * Created by Cristian Pela on 20.11.2019.
 */
class UIOrderByFilterMapperImplTest {

    @Test
    fun toBookFilter() {
        val mapper = UIOrderByFilterMapperImpl(emptyMap(), emptyMap())
        mapper.currentFilter = BookFilter.OrderBy.Title()
        var expect = mapper.toBookFilter(DirectionMenuItem(null, StringRes.EMPTY))
        assertTrue(expect is BookFilter.OrderBy.Title)
        assertFalse(expect!!.asc)
        expect = mapper.toBookFilter(OrderByFilterMenuItem.Author(null, StringRes.EMPTY))
        assertTrue(expect is BookFilter.OrderBy.Author)
        assertFalse(expect!!.asc)
    }
}