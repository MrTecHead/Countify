package com.example.countify.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.countify.databinding.ItemColorSwatchBinding

class ColorAdapter(
    private val colors: List<Int>,
    initialSelectedColor: Int,
    private val onColorSelected: (Int) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    private var selectedPosition = colors.indexOf(initialSelectedColor).let { if (it >= 0) it else 0 }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorSwatchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = colors.size

    fun getSelectedColor(): Int = colors.getOrElse(selectedPosition) { colors.first() }

    inner class ColorViewHolder(
        private val binding: ItemColorSwatchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(color: Int, isSelected: Boolean) {
            binding.cardColor.setCardBackgroundColor(color)

            // Adjust stroke color for visibility
            val isLight = isColorLight(color)
            binding.cardColor.strokeColor = if (isLight) Color.GRAY else Color.WHITE
            binding.ivCheck.setColorFilter(if (isLight) Color.BLACK else Color.WHITE)

            binding.ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)
                onColorSelected(color)
            }
        }

        private fun isColorLight(color: Int): Boolean {
            val red = (color shr 16) and 0xFF
            val green = (color shr 8) and 0xFF
            val blue = color and 0xFF
            val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
            return luminance > 180
        }
    }
}
