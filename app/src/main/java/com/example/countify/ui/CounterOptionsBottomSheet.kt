package com.example.countify.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.countify.databinding.DialogCounterOptionsBinding
import com.example.countify.model.CounterItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CounterOptionsBottomSheet(
    private val counter: CounterItem,
    private val onEditSelected: (CounterItem) -> Unit,
    private val onNotificationOptionsSelected: (CounterItem) -> Unit,
    private val onAddWidgetSelected: (CounterItem) -> Unit,
    private val onDeleteSelected: (CounterItem) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogCounterOptionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCounterOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvCounterTitle.text = counter.title.ifBlank { "Counter Options" }

        binding.btnEdit.setOnClickListener {
            dismiss()
            onEditSelected(counter)
        }

        binding.btnNotificationOptions.setOnClickListener {
            dismiss()
            onNotificationOptionsSelected(counter)
        }

        binding.btnAddWidget.setOnClickListener {
            dismiss()
            onAddWidgetSelected(counter)
        }

        binding.btnDelete.setOnClickListener {
            dismiss()
            onDeleteSelected(counter)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CounterOptionsBottomSheet"
    }
}
