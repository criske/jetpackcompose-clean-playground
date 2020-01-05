@file:Suppress("FunctionName")

package com.crskdev.jccp.domain.validators.book

import com.crskdev.jccp.domain.validators.*

/**
 * Created by Cristian Pela on 25.11.2019.
 */
fun BookValidator(form: BookInputForm, currentYear: Int): RuleFail.Compound =
    Validator.check(
        form.title.apply {
            rule = EmptyStringRule(value, RuleFail.Of(field, BookFieldErrorIds.TITLE))
        },
        form.author.apply {
            rule = EmptyStringRule(value, RuleFail.Of(field, BookFieldErrorIds.AUTHOR))
        },
        form.year.apply {
            rule = YearRule(this, currentYear)
        },
        form.genre.apply {
            rule = EmptyStringRule(value, RuleFail.Of(field, BookFieldErrorIds.GENRE))
        }
    )

private fun YearRule(input: Input, currentYear: Int) = object : Rule {

    override fun check(): RuleFail {
        val (field, value) = input
        try {
            require(value != null && value is Int)
        } catch (ex: Exception) {
            return RuleFail.Unknown(ex)
        }
        val fails = mutableListOf<RuleFail.Of>()
        if (value < 0)
            fails.add(RuleFail.Of(field, BookFieldErrorIds.YEAR_NEGATIVE))
        if (value < 1900)
            fails.add(RuleFail.Of(field, BookFieldErrorIds.YEAR_LESS_1900))
        if (value > currentYear)
            fails.add(
                RuleFail.Of(field, BookFieldErrorIds.YEAR_LARGER_CURRENT, listOf(currentYear))
            )

        return if (fails.isEmpty()) {
            RuleFail.None
        } else {
            RuleFail.Compound(fails)
        }
    }
}

