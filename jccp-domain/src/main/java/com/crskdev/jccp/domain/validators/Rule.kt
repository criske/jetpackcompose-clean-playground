package com.crskdev.jccp.domain.validators

/**
 * Created by Cristian Pela on 25.11.2019.
 */

interface Rule {
    fun check(): RuleFail
}

sealed class RuleFail(throwable: Throwable?) : Throwable(throwable) {
    object None : RuleFail(null)
    class Unknown(throwable: Throwable? = null) : RuleFail(throwable)
    data class Of(val field: Field, val errorId: Int, val errorArgs: List<Any> = emptyList()) :
        RuleFail(null)

    class Compound(val fails: List<RuleFail>) : RuleFail(null) {
        companion object {
            val NO_FORM_ERROR = RuleFail.Compound(emptyList())
        }

        fun findFailOfsFor(field: Field): List<RuleFail.Of> {
            val collector = mutableListOf<RuleFail.Of>()
            fun collectFailsForField(compound: Compound) {
                compound.fails.forEach {
                    when (it) {
                        is Of -> {
                            if (it.field == field) {
                                collector.add(it)
                            }
                        }
                        is Compound -> collectFailsForField(it)
                        else -> { }
                    }
                }
            }
            collectFailsForField(this)
            return collector
        }

        fun passes(): Boolean = fails.isEmpty()
    }
}

internal val NO_RULE = object : Rule {
    override fun check(): RuleFail = RuleFail.None
}

internal class EmptyStringRule(private val value: Any?, private val fail: RuleFail) : Rule {
    override fun check(): RuleFail {
        try {
            require(value != null && value is String)
        } catch (ex: Exception) {
            return RuleFail.Unknown(ex)
        }
        return if (value.isBlank()) {
            fail
        } else {
            RuleFail.None
        }
    }
}