package com.crskdev.jccp.domain.validators.book

import com.crskdev.jccp.domain.model.Book
import com.crskdev.jccp.domain.model.Id
import com.crskdev.jccp.domain.validators.RuleFail
import org.junit.Test

import org.junit.Assert.*

/**
 * Created by Cristian Pela on 25.11.2019.
 */
class BookValidatorKtTest {

    @Test
    fun should_pass_validation() {
        assertTrue(Book(Id.common(0), "foo", "bar", 2019, "action")
            .toForm("Form")
            .let { BookValidator(it, 2019) }
            == RuleFail.Compound.NO_FORM_ERROR
        )
    }
}