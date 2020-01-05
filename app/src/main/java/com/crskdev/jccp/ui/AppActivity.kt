@file:Suppress("LocalVariableName")

package com.crskdev.jccp.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Ambient
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.ui.core.setContent
import androidx.ui.material.MaterialTheme
import com.crskdev.jccp.ui.app.App
import com.crskdev.jccp.ui.app.AppAmbient
import com.crskdev.jccp.ui.app.AppAmbientProvider
import com.crskdev.jccp.ui.app.AppViewModel
import com.crskdev.jccp.ui.compose.extra.AmbientProvider
import com.crskdev.jccp.ui.compose.extra.MultiAmbient
import com.crskdev.jccp.ui.compose.extra.OnDestroyContainerDispatcher
import com.crskdev.jccp.ui.compose.extra.SimpleRoute
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.History
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class AppActivity : AppCompatActivity() {

    private val viewModel by viewModels<AppViewModel>()

    private lateinit var backstack: Backstack

    private val onDestroyContainerDispatcher = OnDestroyContainerDispatcher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dependencies = Dependencies()
        backstack = viewModel.backstack.apply {
            //TODO remove on prod
            //setup(History.of(NoRoute()))
        }
//        GlobalScope.launch {
//            flowOf(1).collect {  }
//        }
        val ambients = listOf<AmbientProvider<*>>(
            {
                AppAmbient.Provider(
                    AppAmbientProvider(
                        viewModel.backstack,
                        viewModel.storeProvider,
                        onDestroyContainerDispatcher,
                        viewModel.systemDependencies
                    ), it
                )
            },
            {
                DependenciesContext.Provider(dependencies, it)
            }
        )
        setContent {
            MultiAmbient.Provider(ambients) {
                App()
            }
        }
    }

    override fun onPostResume() {
        super.onPostResume();
        backstack.reattachStateChanger();
    }

    override fun onPause() {
        backstack.detachStateChanger();
        super.onPause();
    }

    override fun onBackPressed() {
        if (!backstack.goBack()) {
            super.onBackPressed();
        }
    }

    override fun onDestroy() {
        onDestroyContainerDispatcher.dispatch()
        super.onDestroy()
        backstack.executePendingStateChange();
        if (isFinishing) {
            backstack.finalizeScopes();
        }
    }
}

class CountViewModel : ViewModel() {
    val countLiveData: LiveData<Int> = MutableLiveData<Int>(0)

    init {
        require(countLiveData is MutableLiveData)
        viewModelScope.launch {
            while (true) {
                countLiveData.value = countLiveData.value!! + 1
                delay(1000)
            }
        }
    }
}

@Parcelize
class NoRoute: SimpleRoute()

class Dependencies()

val DependenciesContext = Ambient.of<Dependencies>()

//@Composable
//fun App() {
//    val appAmbient = +ambient(AppAmbient)
//    Router(defaultRoute = HomeRoute(), navigator = SimpleStackNavigator(appAmbient.backstack)) {
//        when (it) {
//            is HomeRoute -> Home()
//            is CountRoute -> CountDisplay()
//            is OtherRoute -> Other()
//        }
//    }
////    Column(crossAxisSize = LayoutSize.Expand, modifier = Spacing(16.dp)) {
////        Container(expanded = true, height = 180.dp) {
////            Clip(shape = RoundedCornerShape(8.dp)) {
////                GlideImage(
////                    path = "https://picsum.photos/id/3/200/300",
////                    placeholder = android.R.drawable.btn_star_big_on
////                ) {
////                    centerCrop()
////                }
////            }
////        }
////        HeightSpacer(16.dp)
////        Text("Glide With Jetpack Compose")
////    }
//
//}
//
//
//@Composable
//fun CountDisplay() {
//    val navigator = +ambient(NavigatorContext)
//    Column {
//        val lifecycleOwner = +lifeCycleOwner()
//        ViewModelFactory<CountViewModel>(key = "COUNT_VM") { vm ->
//            LiveDataSub(lifecycleOwner = lifecycleOwner, liveData = vm.countLiveData) {
//                Text(text = "Count: $it")
//            }
//        }
//        Button("Home", onClick = {
//            navigator.goTo(HomeRoute(mapOf("Hello" to "Hello World")))
//        })
//        Button(text = "Other", onClick = {
//            navigator.goTo(OtherRoute())
//        })
//    }
//}
//
//@Composable
//fun Home() {
//    val navigator = +ambient(NavigatorContext)
//    val thisRoute = +thisRoute<HomeRoute>()
//    Container {
//        Column {
//            Button("Count", onClick = {
//                navigator.goTo(CountRoute())
//            })
//            Button(text = "Other", onClick = {
//                navigator.moveToTop(OtherRoute())
//            })
//            Text(text = thisRoute.args["Hello"] as String? ?: "")
//        }
//    }
//}
//
//
//@Composable
//fun Other() {
//    val navigator = +ambient(NavigatorContext)
//    val uiSavedState = +routeUISavedState<OtherUIState>()
//    var input by +state { uiSavedState.input }
//    Column {
//        Button(text = "Home", onClick = {
//            navigator.goTo(HomeRoute())
//        })
//        Button(text = "Count", onClick = {
//            navigator.goTo(CountRoute())
//        })
//        HeightSpacer(height = 16.dp)
//        Container {
//            TextField(
//                value = input,
//                onValueChange =  { input = it },
//                editorStyle = EditorStyle(textStyle = TextStyle(fontSize = (14.sp)))
//            )
//        }
//
//    }
//    +onDispose2 {
//        uiSavedState.input = input
//    }
//}
//
//class OtherUIState(var input: String = "Init")
//data class OtherRoute(override val args: Map<String, Any> = emptyMap()) :
//    Route<OtherUIState>() {
//    override var uiSavedState: OtherUIState =
//        OtherUIState()
//}
//
//
//data class HomeRoute(override val args: Map<String, Any> = emptyMap()) : Route<Unit>() {
//    override var uiSavedState: Unit = Unit
//}
//
//data class CountRoute(override val args: Map<String, Any> = emptyMap()) : Route<Unit>() {
//    override var uiSavedState: Unit = Unit
//}

