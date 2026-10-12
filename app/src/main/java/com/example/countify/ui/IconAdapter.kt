package com.example.countify.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.countify.databinding.ItemIconOptionBinding
import com.example.countify.model.CounterItem

class IconAdapter(
    private val iconNames: List<String> = ICON_OPTIONS,
    initialSelectedIcon: String,
    private val onIconSelected: (String) -> Unit
) : RecyclerView.Adapter<IconAdapter.IconViewHolder>() {

    private var selectedPosition = iconNames.indexOf(initialSelectedIcon).let { if (it >= 0) it else 0 }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val binding = ItemIconOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IconViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(iconNames[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = iconNames.size

    inner class IconViewHolder(
        private val binding: ItemIconOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(iconName: String, isSelected: Boolean) {
            val dummyItem = CounterItem(
                title = "", year = 0, month = 0, dayOfMonth = 0,
                textColor = 0, backgroundColor = 0, iconName = iconName
            )
            binding.ivOptionIcon.setImageResource(dummyItem.getIconResId())

            if (isSelected) {
                binding.cardIcon.setCardBackgroundColor(0xFF007AFF.toInt())
                binding.cardIcon.strokeColor = Color.WHITE
                binding.ivOptionIcon.setColorFilter(Color.WHITE)
            } else {
                binding.cardIcon.setCardBackgroundColor(0xFF2C2C2E.toInt())
                binding.cardIcon.strokeColor = Color.TRANSPARENT
                binding.ivOptionIcon.setColorFilter(Color.WHITE)
            }

            binding.root.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)
                onIconSelected(iconName)
            }
        }
    }

    companion object {
        val ICON_OPTIONS = listOf(
            "calendar", "cake", "star", "heart", "flight",
            "work", "party", "gift", "fitness", "music"
        )
    }
}
