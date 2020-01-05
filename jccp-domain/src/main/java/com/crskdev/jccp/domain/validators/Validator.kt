package com.crskdev.jccp.domain.validators

/**
 * Created by Cristian Pela on 25.11.2019.
 */
internal object Validator {
    fun check(vararg inputs: Input): RuleFail.Compound =
        inputs.map { it.rule.check() }
            .filter { it != RuleFail.None && it != RuleFail.Compound.NO_FORM_ERROR }
            .fold(mutableListOf<RuleFail>()) { acc, curr -> acc.apply { add(curr) } }
            .let {
                if(it.isEmpty()) RuleFail.Compound.NO_FORM_ERROR else RuleFail.Compound(it)
            }
}