package com.crskdev.jccp.ui.app.books.list

import com.crskdev.jccp.R
import com.crskdev.jccp.domain.model.BookFilter
import com.crskdev.jccp.system.sub.Emitter
import com.crskdev.jccp.system.sub.EmitterDelegate
import com.crskdev.jccp.system.sub.Subscriber
import com.crskdev.jccp.system.sub.Subscription
import com.crskdev.jccp.ui.app.books.list.UIOrderByFilterIDs.AUTHOR_DRW
import com.crskdev.jccp.ui.app.books.list.UIOrderByFilterIDs.AUTHOR_STR
import com.crskdev.jccp.ui.app.books.list.UIOrderByFilterIDs.DATE_CREATED_STR
import com.crskdev.jccp.ui.app.books.list.UIOrderByFilterIDs.DATE_DRW
import com.crskdev.jccp.ui.app.books.list.UIOrderByFilterIDs.DATE_UPDATED_STR
import com.crskdev.jccp.ui.app.books.list.UIOrderByFilterIDs.DIRECTION_ASC_DRW
import com.crskdev.jccp.ui.app.books.list.UIOrderByFilterIDs.DIRECTION_ASC_STR
import com.crskdev.jccp.ui.app.books.list.UIOrderByFilterIDs.DIRECTION_BI_DRW
import com.crskdev.jccp.ui.app.books.list.UIOrderByFilterIDs.DIRECTION_BI_STR
import com.crskdev.jccp.ui.app.books.list.UIOrderByFilterIDs.DIRECTION_DESC_DRW
import com.crskdev.jccp.ui.app.books.list.UIOrderByFilterIDs.DIRECTION_DESC_STR
import com.crskdev.jccp.ui.app.books.list.UIOrderByFilterIDs.DROP_DOWN_DRW
import com.crskdev.jccp.ui.app.books.list.UIOrderByFilterIDs.DROP_DOWN_STR
import com.crskdev.jccp.ui.app.books.list.UIOrderByFilterIDs.TITLE_DRW
import com.crskdev.jccp.ui.app.books.list.UIOrderByFilterIDs.TITLE_STR
import com.crskdev.jccp.ui.resources.DrawableRes
import com.crskdev.jccp.ui.resources.StringRes
import kotlin.properties.Delegates

interface UIOrderByFilterMapper: Emitter<Menu> {

    var currentFilter: BookFilter.OrderBy

    fun toBookFilter(menuItem: MenuItem): BookFilter.OrderBy?
}


class UIOrderByFilterMapperImpl(private val drawables: Map<Int, DrawableRes>,
                                private val strings: Map<Int, StringRes>,
                                private val delegate: EmitterDelegate<Menu> = EmitterDelegate()) :
    UIOrderByFilterMapper, Emitter<Menu> by delegate {

    override var currentFilter: BookFilter.OrderBy
            by Delegates.observable<BookFilter.OrderBy>(BookFilter.OrderBy.DateUpdated()) { _, _, new ->
                notifySubscribers(createMenu(new))
            }

    override fun subscribe(emitOnSubscribe: (() -> Menu)?, subscriber: Subscriber<Menu>): Subscription =
        delegate.subscribe({ createMenu(currentFilter) }, subscriber)

    override fun toBookFilter(menuItem: MenuItem): BookFilter.OrderBy? =
        when (menuItem) {
            is DirectionMenuItem -> {
                currentFilter = !currentFilter
                currentFilter
            }
            is OrderByFilterMenuItem.Title -> BookFilter.OrderBy.Title(currentFilter.asc)
            is OrderByFilterMenuItem.Author -> BookFilter.OrderBy.Author(currentFilter.asc)
            is OrderByFilterMenuItem.DateUpdated -> BookFilter.OrderBy.DateUpdated(currentFilter.asc)
            is OrderByFilterMenuItem.DateCreated -> BookFilter.OrderBy.DateCreated(currentFilter.asc)
            is DropDownMenuItem -> null
        }

    private fun createMenu(filter: BookFilter.OrderBy): Menu  {

        val overflowMenuItems = listOf(
            OrderByFilterMenuItem.Title(
                drawables[TITLE_DRW],
                strings[TITLE_STR] ?: StringRes.EMPTY,
                filter is BookFilter.OrderBy.Title
            ),
            OrderByFilterMenuItem.Author
                (
                drawables[AUTHOR_DRW],
                strings[AUTHOR_STR] ?: StringRes.EMPTY,
                filter is BookFilter.OrderBy.Author
            ),
            OrderByFilterMenuItem.DateCreated(
                drawables[DATE_DRW],
                strings[DATE_CREATED_STR] ?: StringRes.EMPTY,
                filter is BookFilter.OrderBy.DateCreated
            ),
            OrderByFilterMenuItem.DateUpdated(
                drawables[DATE_DRW],
                strings[DATE_UPDATED_STR] ?: StringRes.EMPTY,
                filter is BookFilter.OrderBy.DateUpdated
            )
        )

        val showingMenuItems =filter.asc.let {
            val dirIcon = if (it) DIRECTION_ASC_DRW else DIRECTION_DESC_DRW
            listOf(
                DirectionMenuItem(
                    drawables[dirIcon],
                    strings[DIRECTION_BI_STR] ?: StringRes.EMPTY
                ),
                DropDownMenuItem(
                    drawables[DROP_DOWN_DRW],
                    strings[DROP_DOWN_STR] ?: StringRes.EMPTY
                )
            )
        }

        return  Menu(showingMenuItems, overflowMenuItems)
    }

}

class Menu(val showingMenuItems: List<MenuItem>, val overflowMenuItems: List<MenuItem>)

object UIOrderByFilterIDs {
    const val DROP_DOWN_DRW = 0
    const val DROP_DOWN_STR = 1

    const val DIRECTION_BI_DRW = 2
    const val DIRECTION_BI_STR = 3
    const val DIRECTION_ASC_DRW = 4
    const val DIRECTION_ASC_STR = 5
    const val DIRECTION_DESC_DRW = 6
    const val DIRECTION_DESC_STR = 7

    const val TITLE_DRW = 8
    const val TITLE_STR = 9
    const val AUTHOR_DRW = 10
    const val AUTHOR_STR = 11
    const val DATE_DRW = 12
    const val DATE_CREATED_STR = 13
    const val DATE_UPDATED_STR = 14
}


val uiOrderByMenuItemDrawables = mapOf(
    DIRECTION_BI_DRW to DrawableRes(R.drawable.ic_baseline_swap_vert_24),
    DIRECTION_ASC_DRW to DrawableRes(R.drawable.ic_baseline_arrow_upward_24),
    DIRECTION_DESC_DRW to DrawableRes(R.drawable.ic_baseline_arrow_downward_24),
    DROP_DOWN_DRW to DrawableRes(R.drawable.ic_baseline_more_vert_24)
)
val uiOrderByMenuItemStrings = mapOf<Int, StringRes>(
    TITLE_STR to StringRes(R.string.title),
    AUTHOR_STR to StringRes(R.string.author),
    DATE_CREATED_STR to StringRes(R.string.date_created),
    DATE_UPDATED_STR to StringRes(R.string.date_updated),
    DIRECTION_ASC_STR to StringRes(R.string.filter_dir_asc),
    DIRECTION_DESC_STR to StringRes(R.string.filter_dir_desc)
)