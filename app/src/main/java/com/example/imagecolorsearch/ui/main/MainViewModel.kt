package com.example.imagecolorsearch.ui.main

import android.app.Application
import android.util.Size
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.imagecolorsearch.recycler.ThumbnailDataSource

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var thumbnailSize = Size(200, 200)
    var pageSize = 48

    val thumbnails = Pager(
        PagingConfig(pageSize = pageSize, enablePlaceholders = true)
    ) {
        ThumbnailDataSource(application, thumbnailSize, pageSize)
    }.flow.cachedIn(viewModelScope)
}