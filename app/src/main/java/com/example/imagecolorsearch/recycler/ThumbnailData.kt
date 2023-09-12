package com.example.imagecolorsearch.recycler

import android.graphics.Bitmap
import androidx.palette.graphics.Palette

data class ThumbnailData(
    val id: Long,
    val volume: String,
    val dateCreated: Long,
    val mimeType: String,
    val path: String,
    val palette: Palette,
    val bitmap: Bitmap
)