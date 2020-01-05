package com.crskdev.jccp.ui.resources

import android.content.res.Resources

/**
 * Created by Cristian Pela on 27.11.2019.
 */
interface StringResTranslator {
    fun translate(stringRes: StringRes): String
}

class AndroidStringResTranslator(private val resources: Resources) : StringResTranslator {
    override fun translate(stringRes: StringRes): String =
        stringRes.run {
            if (args.isEmpty()) {
                resources.getString(value)
            } else {
                resources.getString(value, *args.toTypedArray())
            }
        }
}