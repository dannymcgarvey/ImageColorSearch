package com.example.imagecolorsearch.recycler

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.MediaStore
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
import androidx.palette.graphics.Target
import java.text.DateFormat
import java.util.*

class BitmapAdapter : PagingDataAdapter<ThumbnailData, ThumbnailHolder>(callback) {

    override fun onBindViewHolder(holder: ThumbnailHolder, position: Int) {
        val item = getItem(position)
        holder.binding.thumbnail.setImageBitmap(item?.bitmap)
        holder.binding.thumbnail.setOnClickListener {
            val palette = (item ?: return@setOnClickListener).palette

            val messageBuilder = SpannableStringBuilder(
                 item.path + "\n\n"
            )
            val context = holder.binding.root.context
            MaterialAlertDialogBuilder(context)
                .setTitle(DateFormat.getInstance().format(Date(item.dateCreated * 1000)))
                .setMessage(
                defaultTargets.mapNotNull { (target: Target, label: String) ->
                    palette[target]?.let {
                        makeColorString(it.rgb, label)
                    }
                }.joinTo(messageBuilder, separator = "\n")
            )
                .setNegativeButton("Back") { _, _ -> }
                .setPositiveButton(palette.dominantSwatch?.rgb?.let {
                    makeDialogButton(it, "Share...")
                } ?: "Share"){ _, _ ->
                    startShare(context, item)
                }
                .show()
        }
    }

    private fun startShare(context: Context, data: ThumbnailData) {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            val uriToImage = ContentUris.withAppendedId(
                    MediaStore.Images.Media.getContentUri(data.volume),
                    data.id
                )
            putExtra(Intent.EXTRA_STREAM, uriToImage)
            type = data.mimeType
        }
        context.startActivity(Intent.createChooser(shareIntent, null))
    }

    private fun makeColorString(@ColorInt color: Int, label: String) = SpannableString(
        "$label: ${String.format("#%06X", 0xFFFFFF and color)}"
    ).apply {
        setSpan(
            BackgroundColorSpan(color), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        setSpan(
            ForegroundColorSpan(Color.WHITE), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    private fun makeDialogButton(@ColorInt color: Int, label: String) =
        SpannableString(label).apply {
            setSpan(
                ForegroundColorSpan(color), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailHolder {
        return ThumbnailHolder(HolderThumbnailBinding.inflate(LayoutInflater.from(parent.context)))
    }

    companion object {
        private val defaultTargets = arrayOf(
            Target.LIGHT_VIBRANT to "Light Vibrant",
            Target.VIBRANT to "Vibrant",
            Target.DARK_VIBRANT to "Dark Vibrant",
            Target.LIGHT_MUTED to "Light Muted",
            Target.MUTED to "Muted",
            Target.DARK_MUTED to "Dark Muted"
        )

        private val callback = object : DiffUtil.ItemCallback<ThumbnailData>() {
            override fun areItemsTheSame(oldItem: ThumbnailData, newItem: ThumbnailData): Boolean {
                return oldItem.id == newItem.id && oldItem.volume == newItem.volume
            }

            override fun areContentsTheSame(
                oldItem: ThumbnailData, newItem: ThumbnailData
            ): Boolean {
                return true
            }

        }
    }
}