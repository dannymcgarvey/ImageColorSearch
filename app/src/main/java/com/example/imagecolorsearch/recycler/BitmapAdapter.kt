package com.example.imagecolorsearch.recycler

import android.graphics.Bitmap
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.paging.PagingDataAdapter
import androidx.palette.graphics.*
import androidx.recyclerview.widget.DiffUtil
import com.example.imagecolorsearch.databinding.HolderThumbnailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.palette.graphics.Target

class BitmapAdapter : PagingDataAdapter<Bitmap, ThumbnailHolder>(callback) {

    override fun onBindViewHolder(holder: ThumbnailHolder, position: Int) {
        val item = getItem(position)
        holder.binding.thumbnail.setImageBitmap(item)
        var loading = false
        holder.binding.thumbnail.setOnClickListener {
            if (loading) return@setOnClickListener
            loading = true
            val bitmap = item ?: return@setOnClickListener
            CoroutineScope(Dispatchers.IO).launch {
                val palette = Palette.Builder(bitmap).generate()
                val targets = arrayOf(
                    Target.LIGHT_VIBRANT to "Light Vibrant",
                    Target.VIBRANT to "Vibrant",
                    Target.DARK_VIBRANT to "Dark Vibrant",
                    Target.LIGHT_MUTED to "Light Muted",
                    Target.MUTED to "Muted",
                    Target.DARK_MUTED to "Dark Muted"
                )
                withContext(Dispatchers.Main) {
                    val builder = SpannableStringBuilder()
                    MaterialAlertDialogBuilder(holder.binding.root.context).setMessage(
                        targets.mapNotNull { (target: Target, label: String) ->
                            palette[target]?.let {
                                makeColorString(it.rgb , label)
                            }
                        }.joinTo(builder, separator = "\n")
                    ).setOnDismissListener {
                        loading = false
                    }.show()
                }
            }
        }
    }


    private fun makeColorString(@ColorInt color: Int, label: String) =
        SpannableString(
            "$label: ${String.format("#%06X", 0xFFFFFF and color)}"
        ).apply {
            setSpan(
                BackgroundColorSpan(color), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(Color.WHITE), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailHolder {
        return ThumbnailHolder(HolderThumbnailBinding.inflate(LayoutInflater.from(parent.context)))
    }

    companion object {
        private val callback = object : DiffUtil.ItemCallback<Bitmap>() {
            override fun areItemsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
                return true
            }

        }
    }
}