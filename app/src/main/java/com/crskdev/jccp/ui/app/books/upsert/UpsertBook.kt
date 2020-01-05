package com.crskdev.jccp.ui.app.books.upsert

import androidx.compose.Composable
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.layout.FlexColumn
import androidx.ui.layout.Padding
import androidx.ui.layout.Stack
import androidx.ui.material.FloatingActionButton
import androidx.ui.material.TopAppBar
import androidx.ui.material.themeTextStyle
import com.crskdev.jccp.R
import com.crskdev.jccp.domain.validators.book.BookField
import com.crskdev.jccp.ui.compose.extra.VectorImageButton
import com.crskdev.jccp.ui.compose.fix.onDispose2

/**
 * Created by Cristian Pela on 19.11.2019.
 */
sealed class Action {
    class Save(val bookFormState: BookFormState) : Action()
    class SaveState(val bookFormState: BookFormState) : Action()
    object Back : Action()
}

@Composable
fun UpsertBook(initialState: BookFormState,
               formErrors: Map<BookField, List<String>>,
               isEdit: Boolean = false,
               onAction: (Action) -> Unit = {}) {

    var formState by +state { initialState }

    FlexColumn {
        inflexible {
            TopAppBar(
                title = { Text(text = if (isEdit) "Edit Book" else "New Book") },
                navigationIcon = {
                    VectorImageButton(id = R.drawable.ic_baseline_arrow_back_24, onClick = {
                        onAction(Action.Back)
                    })
                }
            )
        }
        flexible(flex = 1f) {
            Stack {
                expanded {
                    Padding(padding = 16.dp) {
                        BookForm(formState, formErrors = formErrors) {
                            formState = it.updateForm(formState)
                        }
                    }
                }
                positioned(bottomInset = 16.dp, rightInset = 16.dp) {
                    FloatingActionButton(text = "+",
                        textStyle = +themeTextStyle { h6 },
                        onClick = { onAction(Action.Save(formState)) })
                }
            }
        }
    }
    +onDispose2 {
        onAction(Action.SaveState(formState))
    }
}