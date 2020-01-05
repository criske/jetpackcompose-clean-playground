package com.crskdev.jccp.ui.compose.adapt

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.*
import androidx.ui.core.ContextAmbient
import androidx.ui.foundation.DrawImage
import androidx.ui.graphics.Image
import androidx.ui.graphics.ImageConfig
import androidx.ui.graphics.NativeImage
import androidx.ui.graphics.colorspace.ColorSpace
import androidx.ui.graphics.colorspace.ColorSpaces
import androidx.ui.graphics.imageFromResource
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

/**
 * Created by Cristian Pela on 05.11.2019.
 */
@Composable
fun GlideImage(path: String,
               @DrawableRes placeholder: Int? = null,
               builder: RequestBuilder<Bitmap>.() -> RequestBuilder<Bitmap> = { this }) {
    val context = +ambient(ContextAmbient)
    val (bitmap, setBitmap) = +state {
        placeholder?.let { imageFromResource(context.resources, placeholder).nativeImage }
    }
    +onCommit(path) {
        val requestManager = Glide.with(context)
        val target: Target<Bitmap> = requestManager
            .asBitmap()
            .load(path)
            .let(builder)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) = Unit
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) =
                    setBitmap(resource)
            })
        onDispose {
            requestManager.clear(target)
        }
    }
    bitmap?.also { DrawImage(AndroidImage(it)) }
}

