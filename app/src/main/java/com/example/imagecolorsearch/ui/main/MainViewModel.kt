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
import com.example.imagecolorsearch.recycler.ThumbnailData
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
            filter = emptyList(),
            filterThreshold = 0.5,
            minimumDensity = 0.5,
            requireAll = false
        )
    )

    val filteredThumbnails = thumbnails.combine(searchParams) { pagingData, params ->
        if (params.filter.isEmpty()) {
            return@combine pagingData
        }
        return@combine pagingData.filter { thumbnailData ->
            if (params.requireAll) {
                params.filter.all { filterColor ->
                    isPopulationAboveMinimum(
                        getThumbnailColorPopulation(thumbnailData, filterColor, params),
                        params
                    )
                }
            } else {
                isPopulationAboveMinimum(
                    params.filter.fold(0) { acc, filterColor ->
                        acc + getThumbnailColorPopulation(thumbnailData, filterColor, params)
                    },
                    params
                )
            }
        }
    }

    private fun isPopulationAboveMinimum(population: Int, params: SearchParams) : Boolean {
        val thumbnailPixelCount = thumbnailSize.height * thumbnailSize.width
        return population.toDouble() > thumbnailPixelCount * params.minimumDensity
    }

    private fun getThumbnailColorPopulation(
        thumbnailData: ThumbnailData,
        @ColorInt color: Int,
        searchParams: SearchParams
    ) = thumbnailData.palette.swatches.filter {
            areColorsWithinThreshold(color, it.rgb, searchParams.filterThreshold)
        }.fold(0) { acc, swatch ->
            acc + swatch.population
        }

    private fun areColorsWithinThreshold(
        @ColorInt color1: Int,
        @ColorInt color2: Int,
        filterThreshold: Double
    ) : Boolean {
        // https://en.wikipedia.org/wiki/Color_difference
        val r1 = (color1 and 0xFF0000) shr 16
        val g1 = (color1 and 0x00FF00) shr 8
        val b1 = color1 and 0x0000FF
        val r2 = (color2 and 0xFF0000) shr 16
        val g2 = (color2 and 0x00FF00) shr 8
        val b2 = color2 and 0x0000FF
        val dr = r1 - r2
        val dg = g1 - g2
        val db = b1 - b2
        val squareDistance = (2.0 + (r1 + r2) / 512.0) * dr * dr + 4 * dg * dg + (2.0 + (510.0 - r1 - r2) / 512.0) * db * db
        return squareDistance < filterThreshold * filterThreshold * 255 * 255 * 9
    }

}