package com.crskdev.jccp.ui.app

import android.app.Application
import androidx.compose.Ambient
import androidx.lifecycle.AndroidViewModel
import com.crskdev.jccp.domain.repository.BookRepository
import com.crskdev.jccp.domain.repository.IBookRepository
import com.crskdev.jccp.domain.util.coroutines.AbstractDispatchers
import com.crskdev.jccp.system.SystemDispatchers
import com.crskdev.jccp.system.repository.DummyBookRepository
import com.crskdev.jccp.system.repository.INITIAL_LIST
import com.crskdev.jccp.system.repository.WorkingRepository
import com.crskdev.jccp.ui.compose.adapt.arch.StoreProvider
import com.crskdev.jccp.ui.compose.adapt.arch.StoreProviderImpl
import com.crskdev.jccp.ui.compose.extra.OnDestroyContainerDispatcher
import com.crskdev.jccp.ui.resources.AndroidStringResTranslator
import com.crskdev.jccp.ui.resources.StringResTranslator
import com.zhuinden.simplestack.Backstack

/**
 * Created by Cristian Pela on 07.11.2019.
 */
class AppViewModel(sysApplication: Application) : AndroidViewModel(sysApplication) {

    val backstack = Backstack()
    val storeProvider = StoreProviderImpl()
    val systemDependencies = SystemDependencies(
        SystemDispatchers,
        WorkingRepository(INITIAL_LIST),
        AndroidStringResTranslator(sysApplication.resources)
    )
}

class AppAmbientProvider(
    val backstack: Backstack,
    val storeProvider: StoreProvider,
    val onDestroyContainerDispatcher: OnDestroyContainerDispatcher,
    val dependencies: SystemDependencies
)

val AppAmbient = Ambient.of<AppAmbientProvider>()

class SystemDependencies(
    val dispatchers: AbstractDispatchers,
    val repository: WorkingRepository,
    val stringResTranslator: StringResTranslator
)