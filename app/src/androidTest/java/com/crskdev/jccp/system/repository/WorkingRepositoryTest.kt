package com.crskdev.jccp.system.repository

import com.crskdev.jccp.domain.model.Book
import com.crskdev.jccp.domain.model.BookFilter
import com.crskdev.jccp.domain.model.Date
import com.crskdev.jccp.domain.model.Id
import org.junit.Assert.*
import org.junit.Test

/**
 * Created by Cristian Pela on 24.11.2019.
 */
class WorkingRepositoryTest2{


    @Test
    fun add() {
        val data = listOf(
            Book(Id(4, 0), "At", "Ba", 2001, "drama", date = Date(5, 5)),
            Book(Id(2, 2), "Bt", "Ca", 2001, "drama", date = Date(1, 4)),
            Book(Id(5, 5), "Gt", "Fa", 2000, "action", date = Date(2, 2)),
            Book(Id(1, 1), "Ct", "Aa", 2000, "action", date = Date(2, 2))
        )
        val repo = WorkingRepository(INITIAL_LIST)
        repo.filter = BookFilter.OrderBy.Author()
        repo.add( Book(Id(6, 0), "Fffff", "Ffffff", 2001, "drama", date = Date(5, 5)))
        println(repo.snapshot().map { it.author })
    }

}