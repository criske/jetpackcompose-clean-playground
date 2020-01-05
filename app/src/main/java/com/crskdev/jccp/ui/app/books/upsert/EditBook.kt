package com.crskdev.jccp.ui.app.books.upsert

import androidx.compose.Composable
import androidx.compose.ambient
import androidx.compose.unaryPlus
import com.crskdev.jccp.ui.compose.adapt.arch.ViewModelFactory
import com.crskdev.jccp.ui.compose.adapt.arch.observe
import com.crskdev.jccp.ui.compose.extra.NavigatorContext
import com.crskdev.jccp.ui.compose.extra.thisRoute

/**
 * Created by Cristian Pela on 13.11.2019.
 */
@Composable
fun EditBook() {
    val thisRoute = +thisRoute<EditBookRoute>()
    val navigator = +ambient(NavigatorContext)
    ViewModelFactory(
        key = "EDIT_BOOK",
        factory = { UpsertViewModel(it.dependencies.repository, it.dependencies.stringResTranslator) }) { vm ->
        val formErrors = +observe(vm.fieldErrorsLiveData, emptyMap())
        UpsertBook(
            initialState = thisRoute.uiSavedState,
            isEdit = true,
            formErrors = formErrors
        ) {
            when (it) {
                is Action.Save -> {
                    vm.update(it.bookFormState.toBook()){
                        navigator.goBack()
                    }
                }
                Action.Back -> {
                    navigator.goBack()
                }
                is Action.SaveState -> {
                    thisRoute.uiSavedState = it.bookFormState
                }
            }
        }
    }
}

