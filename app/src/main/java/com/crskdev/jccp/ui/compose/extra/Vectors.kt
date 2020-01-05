package com.crskdev.jccp.ui.compose.extra

import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.*
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.ui.core.*
import androidx.ui.foundation.Clickable
import androidx.ui.graphics.Color
import androidx.ui.graphics.Image
import androidx.ui.graphics.toArgb
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.layout.Container
import androidx.ui.material.ripple.Ripple
import androidx.ui.res.vectorResource
import com.crskdev.jccp.ui.compose.adapt.AndroidImage

/**
 * Created by Cristian Pela on 19.11.2019.
 */
@Composable
fun VectorImageButton(@Pivotal @DrawableRes id: Int, tint: Color = Color.Transparent, onClick: () -> Unit) {
    Ripple(bounded = false) {
        Clickable(onClick = onClick) {
            VectorImage(id, tint)
        }
    }
}

@Composable
fun VectorImage(@DrawableRes id: Int, tint: Color = Color.Transparent) {
    val vector = +vectorResource(id)
    WithDensity {
        Container(width = vector.defaultWidth.toDp(), height = vector.defaultHeight.toDp()) {
            DrawVector(vector, tint)
        }
    }
}

fun imageVectorResource(@DrawableRes id: Int, tint: Color = Color.Black, size: Pair<Dp, Dp> = 24.dp to 24.dp) =
    effectOf<Image> {
        val key = "$id#${tint.value}#${size.first.value}-${size.second.value}"
        +memo(key) {
            val (wPx, hPx) = DensityScope(+ambientDensity()).run {
                size.first.toIntPx().value to size.second.toIntPx().value
            }
            val bitmap = AppCompatResources.getDrawable(+ambient(ContextAmbient), id)
                ?.apply { DrawableCompat.setTint(this, tint.toArgb()) }
                ?.toBitmap(wPx, hPx)
                ?: throw Resources.NotFoundException("Vector resource not found")
            AndroidImage(bitmap)
        }
    }
