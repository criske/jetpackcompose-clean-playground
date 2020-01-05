package com.crskdev.jccp.ui.app.books.list

import com.crskdev.jccp.ui.resources.DrawableRes
import com.crskdev.jccp.ui.resources.StringRes

/**
 * Created by Cristian Pela on 20.11.2019.
 */
sealed class MenuItem(val icon: DrawableRes?, val text: StringRes, var isSelected: Boolean = false)

sealed class FilterMenuItem(icon: DrawableRes?, text: StringRes, isHighlighted: Boolean) :
    MenuItem(icon, text, isHighlighted)

sealed class OrderByFilterMenuItem(icon: DrawableRes?, text: StringRes, isHighlighted: Boolean) :
    FilterMenuItem(icon, text, isHighlighted) {
    class Title(icon: DrawableRes?, text: StringRes, isHighlighted: Boolean = false) :
        OrderByFilterMenuItem(icon, text, isHighlighted)

    class Author(icon: DrawableRes?, text: StringRes, isHighlighted: Boolean = false) :
        OrderByFilterMenuItem(icon, text, isHighlighted)

    class DateUpdated(icon: DrawableRes?, text: StringRes, isHighlighted: Boolean = false) :
        OrderByFilterMenuItem(icon, text, isHighlighted)

    class DateCreated(icon: DrawableRes?, text: StringRes, isHighlighted: Boolean = false) :
        OrderByFilterMenuItem(icon, text, isHighlighted)
}

class DirectionMenuItem(icon: DrawableRes?, text: StringRes) : MenuItem(icon, text)

class DropDownMenuItem(icon: DrawableRes?, text: StringRes) : MenuItem(icon, text)







