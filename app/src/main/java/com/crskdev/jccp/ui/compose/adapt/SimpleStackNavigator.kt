package com.crskdev.jccp.ui.compose.adapt

import com.crskdev.jccp.ui.compose.extra.Navigator
import com.crskdev.jccp.ui.compose.extra.Route
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.History

/**
 * Created by Cristian Pela on 11.11.2019.
 */
class SimpleStackNavigator(private val backstack: Backstack) : Navigator {

    override fun goTo(route: Route<*>) = backstack.goTo(route)

    override fun goBack(): Boolean = backstack.goBack()

    override fun setRoot(route: Route<*>) = backstack.setup(History.of(route))

    override fun moveToTop(route: Route<*>) = backstack.moveToTop(route)

    override fun hasRoot(): Boolean = backstack.isInitialized

    override fun setRouteChanger(onRouteChange: (Route<*>) -> Unit) =
        backstack.setStateChanger { stateChange, completionCallback ->
            if (!stateChange.isTopNewKeyEqualToPrevious) {
                onRouteChange(stateChange.topNewKey())
            }
            completionCallback.stateChangeComplete()
        }

}