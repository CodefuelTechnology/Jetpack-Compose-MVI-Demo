package dev.rivu.mvijetpackcomposedemo.moviesearch.ui

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.state
import androidx.compose.ui.graphics.ImageAsset
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.platform.ContextAmbient
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

@Composable
fun loadPicture(url: String, @DrawableRes placeholderRes: Int, @DrawableRes errorImageRes: Int = placeholderRes): ImageState {

    val placeHolderImage = imageFromResource(ContextAmbient.current.resources, placeholderRes)
    val errorImage = if (errorImageRes == placeholderRes) placeHolderImage else imageFromResource(ContextAmbient.current.resources, errorImageRes)

    var bitmapState: ImageState by state<ImageState> { ImageState.Loading(placeHolderImage) }

    Glide.with(ContextAmbient.current)
        .asBitmap()
        .load(url)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                bitmapState = ImageState.Success(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }
        })

    return bitmapState
}

sealed class ImageState {
    abstract val image: ImageAsset

    data class Loading(override val image: ImageAsset) : ImageState()

    data class Success(val data: Bitmap) : ImageState() {
        override val image: ImageAsset = data.asImageAsset()
    }

    data class Error(override val image: ImageAsset, val exception: Exception) : ImageState()
}
