package com.crskdev.jccp.domain.validators

import com.crskdev.jccp.domain.validators.book.BookField
import org.junit.Assert.*
import org.junit.Test

/**
 * Created by Cristian Pela on 27.11.2019.
 */
class RuleFailTest{


    @Test
    fun should_find_fails_for_fields() {

        val compound = RuleFail.Compound(listOf(
            RuleFail.Of(BookField.TITLE, 1),
            RuleFail.Unknown(),
            RuleFail.Compound(
                listOf(
                    RuleFail.Of(BookField.AUTHOR, 2),
                    RuleFail.Compound(listOf(
                        RuleFail.Of(BookField.TITLE, 3),
                        RuleFail.Of(BookField.AUTHOR, 4),
                        RuleFail.Unknown()
                    )),
                    RuleFail.Of(BookField.TITLE, 5),
                    RuleFail.None
            ))
        ))

        val failsForTitle = compound.findFailOfsFor(BookField.TITLE)
            .map { it.errorId }.sorted().toTypedArray()

        assertArrayEquals( arrayOf(1, 3, 5), failsForTitle)

        val failsForAuthor = compound.findFailOfsFor(BookField.AUTHOR)
            .map { it.errorId }.sorted().toTypedArray()

        assertArrayEquals(arrayOf(2, 4), failsForAuthor)

        assertArrayEquals(emptyArray(), compound.findFailOfsFor(BookField.GENRE).toTypedArray())

    }

}