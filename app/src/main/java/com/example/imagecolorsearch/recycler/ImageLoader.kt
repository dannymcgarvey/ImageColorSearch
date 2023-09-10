package com.example.imagecolorsearch.recycler

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Size
import androidx.palette.graphics.Palette
import java.io.IOException

class ImageLoader(context: Context, private val thumbnailSize: Size) {

    private val contentResolver: ContentResolver = context.contentResolver

    fun loadImages(limit: Int, offset: Int) : MutableList<ThumbnailData> {
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATA
            ),
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT $limit OFFSET $offset"
        ) ?: return mutableListOf()
        val bitmaps = mutableListOf<ThumbnailData>()
        while (cursor.moveToNext()) {
            val idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val dateIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
            val dataIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            val id = cursor.getLong(idIndex)
            val date = cursor.getLong(dateIndex)
            val data = cursor.getString(dataIndex)
            val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            try {
                val bitmap = contentResolver.loadThumbnail(uri, thumbnailSize, null)
                val palette = Palette.Builder(bitmap).generate()
                bitmaps.add(ThumbnailData(id, date, data, palette, bitmap))
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