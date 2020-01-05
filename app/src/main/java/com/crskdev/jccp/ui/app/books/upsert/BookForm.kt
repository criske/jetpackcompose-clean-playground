package com.crskdev.jccp.ui.app.books.upsert

import androidx.compose.Composable
import androidx.compose.unaryPlus
import androidx.ui.core.Text
import androidx.ui.core.TextField
import androidx.ui.core.dp
import androidx.ui.core.sp
import androidx.ui.foundation.shape.border.Border
import androidx.ui.input.KeyboardType
import androidx.ui.layout.*
import androidx.ui.material.surface.Surface
import androidx.ui.material.themeColor
import androidx.ui.material.themeShape
import androidx.ui.material.themeTextStyle
import androidx.ui.material.withOpacity
import androidx.ui.text.TextStyle
import com.crskdev.jccp.domain.validators.book.BookField

/**
 * Created by Cristian Pela on 13.11.2019.
 */
@Composable
fun BookForm(form: BookFormState,
             formErrors: Map<BookField, List<String>> = emptyMap(),
             onFieldChange: (BookFormField) -> Unit = {}) {
    Column(modifier = Spacing()) {
        LabeledTextField(label = "Title",
            text = form.title,
            errors = formErrors[BookField.TITLE] ?: emptyList(),
            onValueChange = {
                onFieldChange(BookFormField.Title(it))
            })
        HeightSpacer(height = 16.dp)
        LabeledTextField(label = "Author",
            text = form.author,
            errors = formErrors[BookField.AUTHOR] ?: emptyList(),
            onValueChange = {
                onFieldChange(BookFormField.Author(it))
            })
        HeightSpacer(height = 16.dp)
        LabeledTextField(label = "Year",
            text = form.year.toString(),
            keyboardType = KeyboardType.Number,
            errors = formErrors[BookField.YEAR] ?: emptyList(),
            onValueChange = {
                onFieldChange(BookFormField.Year(it))
            }
        )
        HeightSpacer(height = 16.dp)
        LabeledTextField(label = "Genre",
            text = form.genre,
            errors = formErrors[BookField.GENRE] ?: emptyList(),
            onValueChange = {
                onFieldChange(BookFormField.Genre(it))
            })
        HeightSpacer(height = 4.dp)
    }
}

sealed class BookFormField(val field: BookField, val value: String) {
    class Author(value: String) : BookFormField(BookField.AUTHOR, value)
    class Title(value: String) : BookFormField(BookField.TITLE, value)
    class Year(value: String) : BookFormField(BookField.YEAR, value)
    class Genre(value: String) : BookFormField(BookField.GENRE, value)
}

fun BookFormField.updateForm(formState: BookFormState): BookFormState =
    when (this) {
        is BookFormField.Author -> formState.copy(author = value)
        is BookFormField.Title -> formState.copy(title = value)
        is BookFormField.Year -> formState.copy(year = value)
        is BookFormField.Genre -> formState.copy(genre = value)
    }

@Composable
private fun LabeledTextField(label: String,
                             text: String,
                             keyboardType: KeyboardType = KeyboardType.Text,
                             errors: List<String> = emptyList(),
                             onValueChange: (String) -> Unit = {}) {
    Column {
        Text(
            text = label,
            style = (+themeTextStyle { body2 }).withOpacity(0.8f)
        )
        HeightSpacer(height = 4.dp)
        Surface(
            border = Border((+themeColor { this.primary }).copy(alpha = 0.05f), 2.dp),
            shape = +themeShape { card }) {
            Padding(8.dp) {
                TextField(value = text, keyboardType = keyboardType, onValueChange = onValueChange)
            }
        }
        Column {
            errors.forEach {
                Padding(padding = EdgeInsets(left = 4.dp)) {
                    Text(
                        text = "*$it",
                        style = TextStyle(color = +themeColor { error }, fontSize = 10.sp)
                    )
                }
            }
        }
    }
}