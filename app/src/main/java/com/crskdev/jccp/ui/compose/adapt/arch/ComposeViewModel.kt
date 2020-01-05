package com.crskdev.jccp.ui.compose.adapt.arch

import androidx.compose.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.crskdev.jccp.ui.app.AppAmbient
import com.crskdev.jccp.ui.app.AppAmbientProvider
import com.crskdev.jccp.ui.compose.fix.onDispose2

/**
 * Created by Cristian Pela on 07.11.2019.
 */

class StoreProviderImpl : StoreProvider {

    private val storeOwners = mutableMapOf<String, ViewModelStoreOwner>()

    override fun getStore(key: String): ViewModelStore = getStoreOwner(key).viewModelStore

    override fun getStoreOwner(key: String): ViewModelStoreOwner =
        storeOwners[key] ?: object : ViewModelStoreOwner {
            val store = ViewModelStore()
            override fun getViewModelStore(): ViewModelStore = store
        }.also {
            storeOwners[key] = it
        }

    override fun clear(key: String) {
        storeOwners[key]?.viewModelStore?.clear()
        storeOwners.remove(key)
    }
}

interface StoreProvider {
    fun getStore(key: String): ViewModelStore
    fun getStoreOwner(key: String): ViewModelStoreOwner
    fun clear(key: String)
}


@Composable
inline fun <reified VM : ViewModel> ViewModelFactory(@Pivotal key: String,
                                                     noinline factory: ((AppAmbientProvider) -> VM)? = null,
                                                     children: @Composable() (VM) -> Unit) {

    val appAmbient = +ambient(AppAmbient)
    val storeProvider = +memo {appAmbient.storeProvider}
    val (viewModel, setViewModel) = +state<VM?> { null }

    +onActive {
        val storeOwner = storeProvider.getStoreOwner(key)
        val viewModelProvider = factory?.let {
            val vmFactory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T = factory(appAmbient) as T
            }
            ViewModelProvider(storeOwner, vmFactory)
        } ?: ViewModelProvider(storeOwner)
        setViewModel(viewModelProvider.get(VM::class.java))
        onDispose {
            storeProvider.clear(key)
        }
    }

    viewModel?.run {
        children(viewModel)
    }
}

@Composable
fun <VM : ViewModel> ViewModelFactory(@Pivotal key: String,
                                      clazz: Class<VM>,
                                      factory: ((AppAmbientProvider) -> VM)? = null,
                                      children: @Composable() (VM) -> Unit) {

    val appAmbient = +ambient(AppAmbient)
    val storeProvider = appAmbient.storeProvider
    val (viewModel, setViewModel) = +state<VM?> { null }

    +onActive {
        val storeOwner = storeProvider.getStoreOwner(key)
        val viewModelProvider = factory?.let {
            val vmFactory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T = factory(appAmbient) as T
            }
            ViewModelProvider(storeOwner, vmFactory)
        } ?: ViewModelProvider(storeOwner)
        setViewModel(viewModelProvider.get(clazz))
        onDispose {
            storeProvider.clear(key)
        }
    }

    viewModel?.run {
        children(viewModel)
    }
}


