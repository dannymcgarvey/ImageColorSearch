package com.example.imagecolorsearch.ui.main

import android.app.Application
import android.graphics.Color
import android.util.Size
import androidx.annotation.ColorInt
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.palette.graphics.Palette
import com.example.imagecolorsearch.recycler.ThumbnailDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var thumbnailSize = Size(200, 200)
    var pageSize = 48

    private val thumbnails = Pager(
        PagingConfig(pageSize = pageSize, enablePlaceholders = true)
    ) {
        ThumbnailDataSource(application, thumbnailSize, pageSize)
    }.flow.cachedIn(viewModelScope)


    val searchParams = MutableStateFlow(
        SearchParams(
            filter = listOf(Color.BLUE, Color.RED),
            filterThreshold = 0.5,
            minimumDensity = 0.2,
            requireAll = false
        )
    )

    val filteredThumbnails = thumbnails.combine(searchParams) { pagingData, params ->
        if (params.filter.isEmpty()) {
            return@combine pagingData
        }
        return@combine pagingData.filter { thumbnailData ->
            thumbnailData.palette.swatches.any { thumbnailSwatch ->
                if (params.requireAll) {
                    params.filter.all { filterColor ->
                        areColorsWithinThreshold(thumbnailSwatch.rgb, filterColor, params.filterThreshold)
                                && isSwatchWithinPopulationDensity(thumbnailSwatch, params.minimumDensity)
                    }
                } else {
                    params.filter.any { filterColor ->
                        areColorsWithinThreshold(thumbnailSwatch.rgb, filterColor, params.filterThreshold)
                                && isSwatchWithinPopulationDensity(thumbnailSwatch, params.minimumDensity)
                    }
                }
            }
        }
    }

    private fun areColorsWithinThreshold(
        @ColorInt color1: Int,
        @ColorInt color2: Int,
        filterThreshold: Double
    ) : Boolean {
        val r1 = (color1 and 0xFF0000) shr 16
        val g1 = (color1 and 0x00FF00) shr 8
        val b1 = color1 and 0x0000FF
        val r2 = (color2 and 0xFF0000) shr 16
        val g2 = (color2 and 0x00FF00) shr 8
        val b2 = color2 and 0x0000FF
        val squareDistance = (r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2)
        return squareDistance.toDouble() / (255 * 255 * 3) < filterThreshold
    }

    private fun isSwatchWithinPopulationDensity(swatch: Palette.Swatch, minimumDensity: Double) =
        swatch.population.toDouble() / (thumbnailSize.height * thumbnailSize.width) > minimumDensity

}