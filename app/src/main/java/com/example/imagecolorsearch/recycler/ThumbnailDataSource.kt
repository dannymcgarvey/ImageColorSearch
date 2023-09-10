package com.example.imagecolorsearch.recycler

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThumbnailDataSource(context: Context, thumbnailSize: Size, private val itemsPerPage: Int) : PagingSource<Int, Bitmap>() {

    private val loader = ImageLoader(context, thumbnailSize)

    override fun getRefreshKey(state: PagingState<Int, Bitmap>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Bitmap> {
        val nextPageNumber = params.key ?: 0
        val response = withContext(Dispatchers.IO) {
            loader.loadImages(itemsPerPage, nextPageNumber * itemsPerPage)
        }
        val numLeft = withContext(Dispatchers.IO) {
            loader.totalImageCount() - (nextPageNumber + 1) * itemsPerPage
        }.coerceAtLeast(0)
        return LoadResult.Page(
            data = response,
            prevKey = (nextPageNumber - 1).takeIf { it >= 0 },
            nextKey = if (numLeft == 0) null else nextPageNumber + 1,
            itemsBefore = itemsPerPage * nextPageNumber,
            itemsAfter = numLeft
        )
    }
}