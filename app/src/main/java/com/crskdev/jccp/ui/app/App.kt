package com.crskdev.jccp.ui.app

import androidx.compose.*
import androidx.ui.core.CoroutineContextAmbient
import androidx.ui.core.Text
import androidx.ui.graphics.Color
import androidx.ui.material.MaterialColors
import androidx.ui.material.MaterialTheme
import androidx.ui.material.TopAppBar
import com.crskdev.jccp.ui.app.books.list.ListRoute
import com.crskdev.jccp.ui.app.books.list.ListScreen
import com.crskdev.jccp.ui.app.books.upsert.EditBook
import com.crskdev.jccp.ui.app.books.upsert.EditBookRoute
import com.crskdev.jccp.ui.app.books.upsert.NewBookRoute
import com.crskdev.jccp.ui.app.books.upsert.UpsertBook
import com.crskdev.jccp.ui.compose.adapt.SimpleStackNavigator
import com.crskdev.jccp.ui.compose.extra.Router
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Created by Cristian Pela on 11.11.2019.
 */

private val themeColors = MaterialColors(
    primary = Color(0xFFDD0D3C),
    primaryVariant = Color(0xFFC20029),
    onPrimary = Color.White,
    secondary = Color.White,
    onSecondary = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    error = Color(0xFFD00036),
    onError = Color.White
)


@Composable
fun App() {
    MaterialTheme(colors = themeColors) {
        val appAmbient = +ambient(AppAmbient)
        Router(defaultRoute = ListRoute, navigator = SimpleStackNavigator(appAmbient.backstack)) {
            when (it) {
                ListRoute -> ListScreen()
                is NewBookRoute -> UpsertBook()
                is EditBookRoute -> EditBook()
            }
        }

//        Counter()
//
//        Toolbar()
    }
}

@Composable
fun Counter() {
    var count by +state { 1 }
    Key(key = "zzz") {
        Text(text = count.toString())
    }
    val coroutineScope = +coroutineScope()
    +onActive {
        coroutineScope.launch {
            repeat(100) {
                delay(1000)
                count++
            }
        }
    }
}

@Composable
fun Toolbar() {
    var menu by +state { emptyList<Int>() }
    Key(key = "toolbar") {
        TopAppBar(title = { Text("Title") }, actionData = menu) {
            Text(text = it.toString())
        }
    }

    val coroutineScope = +coroutineScope()
    +onActive {
        coroutineScope.launch {
            repeat(100) {
                delay(1000)
                menu = listOf(it, it + 1).take(Random.nextInt(2))
            }
        }
    }
}

fun coroutineScope() = effectOf<CoroutineScope> {
    val job = +memo { Job() }
    +onDispose { job.cancel() }
    +memo { CoroutineScope(+ambient(CoroutineContextAmbient) + job) }
}