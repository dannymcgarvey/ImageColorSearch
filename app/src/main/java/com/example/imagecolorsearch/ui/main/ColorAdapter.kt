package com.example.imagecolorsearch.ui.main

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import com.example.imagecolorsearch.R
import com.example.imagecolorsearch.databinding.HolderColorBinding

class ColorAdapter(context: Context) : ArrayAdapter<Int>(
    context, 0, context.resources.getIntArray(R.array.search_colors).toTypedArray()
) {

    val selectedColors = mutableSetOf<Int>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: HolderColorBinding.inflate(LayoutInflater.from(context)).root
        val color = getItem(position) ?: Color.WHITE
        view.setBackgroundColor(color)
        val checkBox = (view as? CheckBox) ?: return view
        checkBox.setTextColor(Color.WHITE)
        checkBox.isChecked = (color in selectedColors)
        checkBox.setOnClickListener {
            if (checkBox.isChecked) {
                selectedColors.add(color)
            } else {
                selectedColors.remove(color)
            }
        }
        return view
    }

}