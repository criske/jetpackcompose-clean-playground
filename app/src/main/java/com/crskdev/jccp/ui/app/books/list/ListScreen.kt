package com.crskdev.jccp.ui.app.books.list

import androidx.compose.*
import androidx.lifecycle.Observer
import androidx.ui.core.*
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.DrawImage
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Card
import androidx.ui.material.surface.Surface
import androidx.ui.res.stringResource
import com.crskdev.jccp.R
import com.crskdev.jccp.domain.model.Book
import com.crskdev.jccp.ui.app.books.upsert.EditBookRoute
import com.crskdev.jccp.ui.app.books.upsert.NewBookRoute
import com.crskdev.jccp.ui.compose.adapt.GlideImage
import com.crskdev.jccp.ui.compose.adapt.arch.ViewModelFactory
import com.crskdev.jccp.ui.compose.adapt.arch.lifeCycleOwner
import com.crskdev.jccp.ui.compose.extra.NavigatorContext
import com.crskdev.jccp.ui.compose.extra.VectorImageButton
import com.crskdev.jccp.ui.compose.extra.imageVectorResource
import com.crskdev.jccp.ui.compose.extra.thisRoute
import com.crskdev.jccp.ui.compose.fix.TopAppBarFixed

/**
 * Created by Cristian Pela on 11.11.2019.
 */
@Composable
fun ListScreen() {
    val thisRoute = +thisRoute<ListRoute>()
    val navigator = +ambient(NavigatorContext)

    ViewModelFactory(
        clazz = ListViewModel::class.java,
        key = thisRoute.toString(),
        factory = {
            ListViewModel(
                it.dependencies.dispatchers,
                it.dependencies.repository,
                UIOrderByFilterMapperImpl(uiOrderByMenuItemDrawables, uiOrderByMenuItemStrings)
            )
        }) { vm ->

        var state by +state { ListScreenState() }
        val lifecycleOwner = +lifeCycleOwner()

        +onActive {
            vm.booksLiveData.observe(lifecycleOwner, Observer {
                state = state.copy(books = it)
            })
            vm.menuLiveData.observe(lifecycleOwner, Observer {
                state = state.copy(menu = it)
            })
        }


        ListScreen(title = state.title,
            books = state.books,
            showingMenuItems = state.menu.showingMenuItems,
            overflowMenuItems = state.menu.overflowMenuItems,
            onAction = { action ->
                when (action) {
                    Action.New -> navigator.goTo(NewBookRoute())
                    is Action.Edit -> navigator.goTo(EditBookRoute.create(action.book))
                    is Action.Remove -> vm.remove(action.book)
                    is Action.Filter -> vm.toBookFilter(action.menuItem)
                }
            })


    }
}


private data class ListScreenState(val books: List<Book> = emptyList(),
                                   val title: String = "Books",
                                   val menu: Menu = Menu(emptyList(), emptyList()))

private sealed class Action {
    object New : Action()
    class Edit(val book: Book) : Action()
    class Remove(val book: Book) : Action()
    class Filter(val menuItem: MenuItem) : Action()
}

@Composable
private fun ListScreen(title: String,
                       books: List<Book>,
                       showingMenuItems: List<MenuItem>,
                       overflowMenuItems: List<MenuItem>,
                       onAction: (Action) -> Unit) {
    val showOverFlow = +state { false }
    Stack {
        FlexColumn {
            inflexible {
                TopAppBarFixed(
                    title = { Text(text = title, style = +themeTextStyle { body1 }) },
                    actionData = showingMenuItems
                ) { menuItem ->
                    val iconId = menuItem.icon?.value ?: throw Exception("Missing Icon")
                    when (menuItem) {
                        is DirectionMenuItem -> {
                            VectorImageButton(
                                id = iconId,
                                onClick = { onAction(Action.Filter(menuItem)) })
                        }
                        is DropDownMenuItem -> {
                            VectorImageButton(
                                id = iconId,
                                onClick = { showOverFlow.value = true })
                        }
                    }
                }
            }
            flexible(flex = 1f) {
                Stack {
                    expanded {
                        Align(alignment = Alignment.TopRight) {
                            BookList(list = books,
                                onBookSelected = { onAction(Action.Edit(it)) },
                                onRemove = { onAction(Action.Remove(it)) })
                        }
                    }
                    positioned(bottomInset = 16.dp, rightInset = 16.dp) {
                        FloatingActionButton(text = "+",
                            textStyle = +themeTextStyle { h6 },
                            onClick = { onAction(Action.New) })
                    }
                }
            }
        }
        if (showOverFlow.value) {
            OverflowMenu(items = overflowMenuItems,
                position = EdgeInsets(top = 40.dp, right = 20.dp),
                onDismiss = { showOverFlow.value = false }) {
                ListItem(
                    text = {
                        Text(text = +stringResource(it.text.value))
                    },
                    icon = {
                        it.takeIf { it.isSelected }?.let {
                            Container(width = 20.dp, height = 20.dp) {
                                DrawImage(
                                    +imageVectorResource(
                                        R.drawable.ic_baseline_check_24,
                                        +themeColor { onSurface },
                                        20.dp to 20.dp
                                    )
                                )
                            }
                        }
                    },
                    onClick = {
                        showOverFlow.value = false
                        onAction(Action.Filter(it))
                    })
            }
        }
    }

}

@Composable
fun BookList(list: List<Book>, onBookSelected: (Book) -> Unit = {}, onRemove: (Book) -> Unit = {}) {
    VerticalScroller {
        Column {
            list.forEach {
                Ripple(bounded = true) {
                    Clickable(onClick = { onBookSelected(it) }) {
                        BookListItem(book = it, onRemove = onRemove)
                    }
                }
                Opacity(0.08f) {
                    Divider(Spacing(top = 1.dp, bottom = 1.dp, left = 1.dp))
                }
            }
        }
    }
}

@Composable
private fun BookListItem(@Pivotal book: Book, onRemove: (Book) -> Unit = {}) {
    FlexRow(crossAxisAlignment = CrossAxisAlignment.Center, modifier = Spacing(8.dp)) {
        inflexible {
            Container(width = 100.dp, height = 100.dp) {
                book.thumbnail?.let {
                    Clip(shape = RoundedCornerShape(5.dp)) {
                        WithDensity {
                            GlideImage(path = it) {
                                override(100.dp.toIntPx().value)
                                    .centerCrop()
                            }
                        }
                    }
                }
            }
            WidthSpacer(width = 8.dp)
        }
        expanded(flex = 1.0f) {
            Column {
                Text(
                    text = book.title,
                    style = +themeTextStyle { subtitle1 },
                    softWrap = true,
                    modifier = Spacing(bottom = 8.dp)
                )
                FlexRow(
                    mainAxisSize = LayoutSize.Expand,
                    crossAxisAlignment = CrossAxisAlignment.Center
                ) {
                    flexible(flex = 1f) {
                        Column(crossAxisSize = LayoutSize.Expand) {
                            Text(text = book.author)
                            Text(text = book.genre, style = +themeTextStyle { subtitle2 })
                        }
                    }
                    inflexible {
                        VectorImageButton(
                            id = R.drawable.ic_baseline_delete_24,
                            tint = +themeColor { this.primary },
                            onClick = {
                                onRemove(book)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun <T> OverflowMenu(items: List<T>,
                             position: EdgeInsets,
                             backdropColor: Color = Color.Transparent,
                             onDismiss: () -> Unit = {},
                             actionDisplay: @Composable() (T) -> Unit = {}) {
    Clickable(onClick = onDismiss) {
        Surface(color = if (backdropColor != Color.Transparent) backdropColor.copy(alpha = 0.5f) else backdropColor) {
            Container(expanded = true) {
                Align(alignment = Alignment.TopLeft) {
                    Stack {
                        expanded { Container {} }
                        positioned(
                            topInset = position.top.takeIf { it > 0.dp },
                            rightInset = position.right.takeIf { it > 0.dp },
                            bottomInset = position.bottom.takeIf { it > 0.dp },
                            leftInset = position.left.takeIf { it > 0.dp }) {
                            Container(width = 200.dp) {
                                Card {
                                    Column {
                                        items.forEach {
                                            //TODO FIX: code gen error
                                            //OverflowMenuItem<T>(it, actionDisplay)
                                            Key(it) { actionDisplay(it) }
                                            Opacity(0.08f) {
                                                Divider(
                                                    Spacing(
                                                        top = 1.dp,
                                                        bottom = 1.dp,
                                                        left = 1.dp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> OverflowMenuItem(action: T, actionDisplay: @Composable() (T) -> Unit) {
    Key(action) { actionDisplay(action) }
    Opacity(0.08f) { Divider(Spacing(top = 1.dp, bottom = 1.dp, left = 1.dp)) }
}
