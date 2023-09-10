package com.example.imagecolorsearch.ui.main

import android.app.Application
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
import kotlinx.coroutines.flow.map

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var thumbnailSize = Size(200, 200)
    var pageSize = 48

    private val thumbnails = Pager(
        PagingConfig(pageSize = pageSize, enablePlaceholders = true)
    ) {
        ThumbnailDataSource(application, thumbnailSize, pageSize)
    }.flow.cachedIn(viewModelScope)

    var filter : List<Int> = listOf()

    var filterThreshold = 0.5

    var minimumDensity = 0.2

    var requireAll = false

    val filteredThumbnails = thumbnails.map { pagingData ->
        if (filter.isEmpty()) {
            return@map pagingData
        }
        return@map pagingData.filter { thumbnailData ->
            thumbnailData.palette.swatches.any { thumbnailSwatch ->
                if (requireAll) {
                    filter.all { filterColor ->
                        areColorsWithinThreshold(thumbnailSwatch.rgb, filterColor)
                                && isSwatchWithinPopulationDensity(thumbnailSwatch)
                    }
                } else {
                    filter.any { filterColor ->
                        areColorsWithinThreshold(thumbnailSwatch.rgb, filterColor)
                                && isSwatchWithinPopulationDensity(thumbnailSwatch)
                    }
                }
            }
        }
    }

    private fun areColorsWithinThreshold(@ColorInt color1: Int, @ColorInt color2: Int) : Boolean {
        val r1 = (color1 and 0xFF0000) shr 16
        val g1 = (color1 and 0x00FF00) shr 8
        val b1 = color1 and 0x0000FF
        val r2 = (color2 and 0xFF0000) shr 16
        val g2 = (color2 and 0x00FF00) shr 8
        val b2 = color2 and 0x0000FF
        val squareDistance = (r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2)
        return squareDistance.toDouble() / (255 * 255 * 3) < filterThreshold
    }

    private fun isSwatchWithinPopulationDensity(swatch: Palette.Swatch) =
        swatch.population.toDouble() / (thumbnailSize.height * thumbnailSize.width) > minimumDensity

}