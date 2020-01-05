package com.crskdev.jccp.ui.compose.extra

import android.os.Bundle
import android.os.Parcelable
import androidx.compose.*
import com.crskdev.jccp.ui.app.AppAmbient
import com.crskdev.jccp.ui.compose.fix.Crossfade
import com.zhuinden.simplestack.History
import kotlinx.android.parcel.Parcelize

/**
 * Created by Cristian Pela on 07.11.2019.
 */
val NavigatorContext = Ambient.of<Navigator>()
val RouteContext = Ambient.of<Route<*>>()

@Composable
fun Router(defaultRoute: Route<*>, navigator: Navigator, children: @Composable() (Route<*>) -> Unit) {
    val appAmbient = +ambient(AppAmbient)
    val backstack = appAmbient.backstack
    val (route, setRoute) = +state { defaultRoute }
    if(!navigator.hasRoot()){
        navigator.setRoot(defaultRoute)
    }
    +onActive {
        backstack.setStateChanger { stateChange, completionCallback ->
            if (!stateChange.isTopNewKeyEqualToPrevious) {
                setRoute(stateChange.topNewKey())
            }
            completionCallback.stateChangeComplete()
        }
    }
    if (backstack.isInitialized)
        NavigatorContext.Provider(value = navigator) {
            Crossfade(
                current = route,
                children = @Composable() {
                    RouteContext.Provider(value = it) {
                        children(it)
                    }
                })
        }
}


@Suppress("MemberVisibilityCanBePrivate")
abstract class Route<T: Parcelable>(): Parcelable {
    abstract val args: Bundle

    abstract var uiSavedState: T
    override fun equals(other: Any?): Boolean  =
            other is Route<*>
                    && this.javaClass.simpleName == other.javaClass.simpleName
                    && equalBundles(this.args, other.args)


    private fun equalBundles(one: Bundle, two: Bundle): Boolean {
        if (one.size() != two.size())
            return false

        if (!one.keySet().containsAll(two.keySet()))
            return false

        for (key in one.keySet()) {
            val valueOne = one.get(key)
            val valueTwo = two.get(key)
            if (valueOne is Bundle && valueTwo is Bundle) {
                if (!equalBundles(valueOne , valueTwo)) return false
            } else if (valueOne != valueTwo) return false
        }

        return true
    }

    override fun toString(): String {
        val strArgs = args.keySet().joinToString("#"){
            "${it}-${args.get(it)}"
        }
        return this.javaClass.simpleName + strArgs
    }
}

@Parcelize
open class SimpleRoute(override val args: Bundle = Bundle(), override var uiSavedState: Bundle = Bundle.EMPTY):
    Route<Bundle>()

interface Navigator {
    fun goTo(route: Route<*>)
    fun goBack(): Boolean
    fun setRoot(route: Route<*>)
    fun moveToTop(route: Route<*>)
    fun hasRoot(): Boolean
    fun setRouteChanger(onRouteChange: (Route<*>) -> Unit)
}

@Suppress("UNCHECKED_CAST")
fun <T> routeUISavedState() = effectOf<T> {
    val thisRoute = +ambient(RouteContext)
    +memo { thisRoute.uiSavedState as T }
}

@Suppress("UNCHECKED_CAST")
fun <T> thisRoute() = effectOf<T> {
    val thisRoute = +ambient(RouteContext)
    +memo { thisRoute as T }
}





