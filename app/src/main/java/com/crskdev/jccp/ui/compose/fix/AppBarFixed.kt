package com.crskdev.jccp.ui.compose.fix

import androidx.compose.Composable
import androidx.compose.unaryPlus
import androidx.ui.core.*
import androidx.ui.engine.geometry.Shape
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.SimpleImage
import androidx.ui.foundation.shape.RectangleShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.Image
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.text.TextStyle

/**
 * A TopAppBar displays information and actions relating to the current screen and is placed at the
 * top of the screen.
 *
 * This TopAppBar displays only a title and navigation icon, use the other TopAppBar overload if
 * you want to display actions as well.
 *
 * @sample androidx.ui.material.samples.SimpleTopAppBarNavIcon
 *
 * @param title The title to be displayed in the center of the TopAppBar
 * @param color An optional color for the TopAppBar. By default [MaterialColors.primary] will be
 * used.
 * @param navigationIcon The navigation icon displayed at the start of the TopAppBar
 */
// TODO: b/137311217 - type inference for nullable lambdas currently doesn't work
@Suppress("USELESS_CAST")
@Composable
fun TopAppBarFixed(
    title: @Composable() () -> Unit,
    color: Color = +themeColor { primary },
    navigationIcon: @Composable() (() -> Unit)? = null as @Composable() (() -> Unit)?
) {
    BaseTopAppBar(
        color = color,
        startContent = navigationIcon,
        title = {
            // Text color comes from the underlying Surface
            CurrentTextStyleProvider(value = +themeTextStyle { h6 }, children = title)
        },
        endContent = null as @Composable() (() -> Unit)?
    )
}

/**
 * A TopAppBar displays information and actions relating to the current screen and is placed at the
 * top of the screen.
 *
 * This TopAppBar has space for a title, navigation icon, and actions. Use the other TopAppBar
 * overload if you only want to display a title and navigation icon.
 *
 * @sample androidx.ui.material.samples.SimpleTopAppBarNavIconWithActions
 *
 * @param title The title to be displayed in the center of the TopAppBar
 * @param color An optional color for the TopAppBar. By default [MaterialColors.primary] will be
 * used.
 * @param navigationIcon The navigation icon displayed at the start of the TopAppBar
 * @param actionData A list of data representing the actions to be displayed at the end of
 * the TopAppBar. Any remaining actions that do not fit on the TopAppBar should typically be
 * displayed in an overflow menu at the end. This list will be transformed into icons / overflow
 * menu items by [action]. For example, you may choose to represent an action with a sealed class
 * containing an icon and text, so you can easily handle events when the action is pressed.
 * @param action A specific action that will be displayed at the end of the TopAppBar - this
 * will be called for items in [actionData] up to the maximum number of icons that can be displayed.
 * This parameter essentially transforms data in [actionData] to an icon / menu item that
 * will actually be displayed to the user.
 * @param T the type of data in [actionData]
 */
// TODO: b/137311217 - type inference for nullable lambdas currently doesn't work
@Suppress("USELESS_CAST")
@Composable
fun <T> TopAppBarFixed(
    title: @Composable() () -> Unit,
    actionData: List<T>,
    color: Color = +themeColor { primary },
    navigationIcon: @Composable() (() -> Unit)? = null as @Composable() (() -> Unit)?,
    action: @Composable() (T) -> Unit
    // TODO: support overflow menu here with the remainder of the list
) {
    BaseTopAppBar(
        color = color,
        startContent = navigationIcon,
        title = {
            // Text color comes from the underlying Surface
            CurrentTextStyleProvider(value = +themeTextStyle { h6 }, children = title)
        },
        endContent = getActions(actionData, MaxIconsInTopAppBar, action)
    )
}

@Composable
private fun BaseTopAppBar(
    color: Color = +themeColor { primary },
    startContent: @Composable() (() -> Unit)?,
    title: @Composable() () -> Unit,
    endContent: @Composable() (() -> Unit)?
) {
    BaseAppBar(color, TopAppBarElevation, RectangleShape) {
        FlexRow(
            mainAxisAlignment = MainAxisAlignment.SpaceBetween,
            crossAxisSize = LayoutSize.Expand
        ) {
            // We only want to reserve space here if we have some start content
            if (startContent != null) {
                inflexible {
                    Container(
                        width = AppBarTitleStartPadding,
                        expanded = true,
                        alignment = Alignment.CenterLeft,
                        children = startContent
                    )
                }
            }
            expanded(3f) {
                Align(Alignment.BottomLeft) {
                    AlignmentLineOffset(
                        alignmentLine = LastBaseline,
                        after = withDensity(+ambientDensity()) { AppBarTitleBaselineOffset.toDp() }
                    ) {
                        // TODO: AlignmentLineOffset requires a child, so in case title() is
                        // empty we just add an empty wrap here - should be fixed when we move to
                        // modifiers.
                        Wrap(children = title)
                    }
                }
            }
            inflexible {
                if (endContent != null) {
                    Center(children = endContent)
                }
            }
        }
    }
}

/**
 * An empty App Bar that expands to the parent's width.
 *
 * For an App Bar that follows Material spec guidelines to be placed on the top of the screen, see
 * [TopAppBar].
 */
@Composable
private fun BaseAppBar(
    color: Color,
    elevation: Dp,
    shape: Shape,
    children: @Composable() () -> Unit
) {
    Surface(color = color, elevation = elevation, shape = shape) {
        Container(
            height = AppBarHeight,
            expanded = true,
            padding = EdgeInsets(left = AppBarPadding, right = AppBarPadding),
            children = children
        )
    }
}

/**
 * @return [AppBarActions] if [actionData] is not empty, else `null`
 */
@Suppress("USELESS_CAST")
private fun <T> getActions(
    actionData: List<T>,
    numberOfActions: Int,
    action: @Composable() (T) -> Unit
): @Composable() (() -> Unit)? {
    return if (actionData.isEmpty()) {
        null as @Composable() (() -> Unit)?
    } else {
        @Composable {
            AppBarActions(numberOfActions, actionData, action)
        }
    }
}

@Composable
private fun <T> AppBarActions(
    actionsToDisplay: Int,
    actionData: List<T>,
    action: @Composable() (T) -> Unit
) {
    // Split the list depending on how many actions we are displaying - if actionsToDisplay is
    // greater than or equal to the number of actions provided, overflowActions will be empty.
    val (shownActions, overflowActions) = actionData.withIndex().partition {
        it.index < actionsToDisplay
    }

    Row {
        shownActions.forEach { (index, shownAction) ->
            action(shownAction)
            if (index != shownActions.lastIndex) {
                WidthSpacer(width = 24.dp)
            }
        }
        if (overflowActions.isNotEmpty()) {
            WidthSpacer(width = 24.dp)
            // TODO: use overflowActions to build menu here
            Container(width = 12.dp) {
                Text(text = "${overflowActions.size}", style = TextStyle(fontSize = 15.sp))
            }
        }
    }
}

/**
 * A correctly sized clickable icon that can be used inside [TopAppBar] and [BottomAppBar] for
 * either the navigation icon or the actions.
 *
 * @param icon The icon to be displayed
 * @param onClick the lambda to be invoked when this icon is pressed
 */
@Composable
fun AppBarIcon(icon: Image, onClick: () -> Unit) {
    Container(width = ActionIconDiameter, height = ActionIconDiameter) {
        Ripple(bounded = false) {
            Clickable(onClick = onClick) {
                SimpleImage(icon)
            }
        }
    }
}

private val ActionIconDiameter = 24.dp

private val AppBarHeight = 56.dp
private val AppBarPadding = 16.dp
private val AppBarTitleStartPadding = 72.dp - AppBarPadding
private val AppBarTitleBaselineOffset = 20.sp

private val TopAppBarElevation = 4.dp

private const val MaxIconsInTopAppBar = 2

