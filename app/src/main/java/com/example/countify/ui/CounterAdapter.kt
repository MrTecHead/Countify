package com.example.countify.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.countify.databinding.ItemCounterRectangleBinding
import com.example.countify.model.CounterItem

class CounterAdapter(
    private val onItemClick: (CounterItem) -> Unit,
    private val onItemLongClick: (CounterItem) -> Unit
) : ListAdapter<CounterItem, CounterAdapter.CounterViewHolder>(CounterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CounterViewHolder {
        val binding = ItemCounterRectangleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CounterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CounterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CounterViewHolder(
        private val binding: ItemCounterRectangleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CounterItem) {
            binding.tvTitle.text = item.title
            binding.tvDate.text = item.getFormattedDate()
            binding.tvDaysLeft.text = item.getDaysLeftText()
            binding.ivIcon.setImageResource(item.getIconResId())

            binding.cardCounter.setCardBackgroundColor(item.backgroundColor)
            binding.tvTitle.setTextColor(item.textColor)
            binding.tvDate.setTextColor(item.textColor)
            binding.tvDaysLeft.setTextColor(item.textColor)
            binding.ivIcon.setColorFilter(item.textColor)

            binding.cardCounter.setOnClickListener {
                onItemClick(item)
            }

            binding.cardCounter.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }

    class CounterDiffCallback : DiffUtil.ItemCallback<CounterItem>() {
        override fun areItemsTheSame(oldItem: CounterItem, newItem: CounterItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CounterItem, newItem: CounterItem): Boolean {
            return oldItem == newItem
        }
    }
}
