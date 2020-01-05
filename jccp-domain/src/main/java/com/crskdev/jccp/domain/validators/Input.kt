package com.crskdev.jccp.domain.validators

/**
 * Created by Cristian Pela on 25.11.2019.
 */
data class Input(val field: Field, val value: Any?) {
    var rule: Rule = NO_RULE
    inline operator  fun <reified T> invoke() :T = value as T
}

internal class InputFormError(val formName: String, val fieldError: RuleFail) : Throwable()

internal infix fun Field.input(value: Any?): Input = Input(this, value)

interface Field

internal object Ignore : Field