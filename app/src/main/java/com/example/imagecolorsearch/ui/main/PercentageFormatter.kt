package com.example.imagecolorsearch.ui.main

import com.google.android.material.slider.LabelFormatter

object PercentageFormatter : LabelFormatter {

    override fun getFormattedValue(value: Float): String {
        return (value * 100).toInt().toString() + "%"
    }
}