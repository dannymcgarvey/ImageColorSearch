package com.example.imagecolorsearch.recycler

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Size
import java.io.IOException

class ImageLoader(context: Context, private val thumbnailSize: Size) {

    private val contentResolver: ContentResolver = context.contentResolver

    fun loadImages(limit: Int, offset: Int) : MutableList<Bitmap> {
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT $limit OFFSET $offset"
        ) ?: return mutableListOf()
        val bitmaps = mutableListOf<Bitmap>()
        while (cursor.moveToNext()) {
            val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            if (columnIndex < 0) {
                continue
            }
            val id = cursor.getLong(columnIndex)
            val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            try {
                val bitmap = contentResolver.loadThumbnail(uri, thumbnailSize, null)
                bitmaps.add(bitmap)
            } catch (e: IOException) {
                continue
            }
        }
        cursor.close()
        return bitmaps
    }

    fun totalImageCount() : Int {
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            null,
            null,
            null
        ) ?: return 0
        if (!cursor.moveToFirst()) {
            return 0
        }
        val count = cursor.count
        cursor.close()
        return count
    }

}