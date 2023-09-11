package com.example.imagecolorsearch.ui.main

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import com.example.imagecolorsearch.databinding.HolderColorBinding

class ColorAdapter(context: Context) : ArrayAdapter<Int>(context, 0, colorList) {

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

    companion object {
        private val colorList = listOf<Int>(
            0xDD2222,
            0x22DD22,
            0x2222DD,
            0xDDDD22,
            0x22DDDD,
            0xDD22DD,
            0xDDDDDD,
            0x888888,
            0x222222
        ).map { it or (0xFF shl 24) }
    }

}