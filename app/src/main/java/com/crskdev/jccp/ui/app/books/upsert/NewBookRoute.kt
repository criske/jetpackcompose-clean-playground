package com.crskdev.jccp.ui.app.books.upsert

import android.os.Bundle
import com.crskdev.jccp.ui.compose.extra.Route
import kotlinx.android.parcel.Parcelize

@Parcelize
class NewBookRoute(override val args: Bundle = Bundle.EMPTY, override var uiSavedState: BookFormState = BookFormState())
    : Route<BookFormState>()