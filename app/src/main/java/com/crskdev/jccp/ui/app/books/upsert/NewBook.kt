package com.crskdev.jccp.ui.app.books.upsert

import androidx.compose.Composable
import androidx.compose.ambient
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.lifecycle.Observer
import com.crskdev.jccp.domain.validators.book.BookField
import com.crskdev.jccp.ui.compose.adapt.arch.ViewModelFactory
import com.crskdev.jccp.ui.compose.adapt.arch.lifeCycleOwner
import com.crskdev.jccp.ui.compose.adapt.arch.observe
import com.crskdev.jccp.ui.compose.extra.NavigatorContext
import com.crskdev.jccp.ui.compose.extra.thisRoute
import com.crskdev.jccp.ui.compose.fix.onDispose2

/**
 * Created by Cristian Pela on 13.11.2019.
 */
@Composable
internal fun UpsertBook() {
    val thisRoute = +thisRoute<NewBookRoute>()
    val navigator = +ambient(NavigatorContext)
    ViewModelFactory(
        key = "NEW_BOOK",
        factory = {
            UpsertViewModel(it.dependencies.repository, it.dependencies.stringResTranslator)
        }) { vm ->

        val formErrors = +observe(vm.fieldErrorsLiveData)

        UpsertBook(initialState = thisRoute.uiSavedState, formErrors = formErrors ?: emptyMap()) {
            when (it) {
                is Action.Save -> {
                    vm.add(it.bookFormState.toBook()){
                        navigator.goBack()
                    }
                }
                Action.Back -> {
                    navigator.goBack()
                }
                is Action.SaveState ->{
                    thisRoute.uiSavedState = it.bookFormState
                }
            }
        }
    }
}