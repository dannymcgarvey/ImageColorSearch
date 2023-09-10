package com.example.imagecolorsearch.ui.main

data class SearchParams(
    var filter : List<Int>,
    var filterThreshold: Double,
    var minimumDensity: Double,
    var requireAll: Boolean
)
