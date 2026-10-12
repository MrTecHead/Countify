package com.example.countify

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.countify.data.CounterRepository
import com.example.countify.databinding.FragmentSecondBinding
import com.example.countify.model.CounterItem
import com.example.countify.notification.NotificationHelper
import com.example.countify.ui.ColorAdapter
import com.example.countify.ui.IconAdapter
import com.example.countify.util.VibrantColors
import com.example.countify.widget.CounterWidgetProvider
import java.util.Calendar

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private var existingCounterId: String? = null
    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDayOfMonth: Int = 0

    private var selectedIconName: String = "calendar"
    private var selectedTextColor: Int = VibrantColors.TEXT_COLORS[0]
    private var selectedBackgroundColor: Int = VibrantColors.getRandomVibrantColor()

    private lateinit var iconAdapter: IconAdapter
    private lateinit var textColorAdapter: ColorAdapter
    private lateinit var backgroundColorAdapter: ColorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        existingCounterId = arguments?.getString(ARG_COUNTER_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = CounterRepository.getInstance(requireContext())
        val existingCounter = existingCounterId?.let { repository.getCounterById(it) }

        if (existingCounter != null) {
            binding.tvHeaderTitle.text = "Edit Counter"
            binding.etTitle.setText(existingCounter.title)
            selectedYear = existingCounter.year
            selectedMonth = existingCounter.month
            selectedDayOfMonth = existingCounter.dayOfMonth
            selectedIconName = existingCounter.iconName
            selectedTextColor = existingCounter.textColor
            selectedBackgroundColor = existingCounter.backgroundColor
        } else {
            binding.tvHeaderTitle.text = "Add Counter"
            val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }
            selectedYear = calendar.get(Calendar.YEAR)
            selectedMonth = calendar.get(Calendar.MONTH)
            selectedDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        }

        setupPickers()
        updateDateButtonText()
        updatePreviewCard()

        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePreviewCard()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSelectDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedYear = year
                    selectedMonth = month
                    selectedDayOfMonth = dayOfMonth
                    updateDateButtonText()
                    updatePreviewCard()
                },
                selectedYear,
                selectedMonth,
                selectedDayOfMonth
            ).show()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnDone.setOnClickListener {
            val titleText = binding.etTitle.text?.toString()?.trim().orEmpty()
            if (titleText.isBlank()) {
                binding.etTitle.error = "Please enter a title"
                return@setOnClickListener
            }

            val newCounter = CounterItem(
                id = existingCounter?.id ?: java.util.UUID.randomUUID().toString(),
                title = titleText,
                year = selectedYear,
                month = selectedMonth,
                dayOfMonth = selectedDayOfMonth,
                textColor = selectedTextColor,
                backgroundColor = selectedBackgroundColor,
                iconName = selectedIconName,
                notificationType = existingCounter?.notificationType ?: CounterItem.NOTIF_NONE,
                customDaysBefore = existingCounter?.customDaysBefore ?: 1,
                notificationHour = existingCounter?.notificationHour ?: 9,
                notificationMinute = existingCounter?.notificationMinute ?: 0
            )

            repository.saveCounter(newCounter)
            if (newCounter.notificationType != CounterItem.NOTIF_NONE) {
                NotificationHelper.scheduleNotification(requireContext(), newCounter)
            }
            CounterWidgetProvider.updateAllWidgets(requireContext())

            Toast.makeText(
                requireContext(),
                if (existingCounter != null) "Counter updated!" else "Counter added!",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigateUp()
        }
    }

    private fun setupPickers() {
        iconAdapter = IconAdapter(initialSelectedIcon = selectedIconName) { icon ->
            selectedIconName = icon
            updatePreviewCard()
        }
        binding.rvIcons.adapter = iconAdapter

        textColorAdapter = ColorAdapter(VibrantColors.TEXT_COLORS, selectedTextColor) { color ->
            selectedTextColor = color
            updatePreviewCard()
        }
        binding.rvTextColors.adapter = textColorAdapter

        backgroundColorAdapter = ColorAdapter(VibrantColors.BACKGROUND_COLORS, selectedBackgroundColor) { color ->
            selectedBackgroundColor = color
            updatePreviewCard()
        }
        binding.rvBackgroundColors.adapter = backgroundColorAdapter
    }

    private fun updateDateButtonText() {
        val tempItem = CounterItem(
            title = "",
            year = selectedYear,
            month = selectedMonth,
            dayOfMonth = selectedDayOfMonth,
            textColor = 0,
            backgroundColor = 0
        )
        binding.btnSelectDate.text = tempItem.getFormattedDate()
    }

    private fun updatePreviewCard() {
        val title = binding.etTitle.text?.toString()?.trim().let {
            if (it.isNullOrBlank()) "Title Preview" else it
        }

        val tempItem = CounterItem(
            title = title,
            year = selectedYear,
            month = selectedMonth,
            dayOfMonth = selectedDayOfMonth,
            textColor = selectedTextColor,
            backgroundColor = selectedBackgroundColor,
            iconName = selectedIconName
        )

        binding.previewTvTitle.text = tempItem.title
        binding.previewTvDate.text = tempItem.getFormattedDate()
        binding.previewTvDaysLeft.text = tempItem.getDaysLeftText()
        binding.previewIvIcon.setImageResource(tempItem.getIconResId())

        binding.cardPreview.setCardBackgroundColor(tempItem.backgroundColor)
        binding.previewTvTitle.setTextColor(tempItem.textColor)
        binding.previewTvDate.setTextColor(tempItem.textColor)
        binding.previewTvDaysLeft.setTextColor(tempItem.textColor)
        binding.previewIvIcon.setColorFilter(tempItem.textColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_COUNTER_ID = "arg_counter_id"
    }
}
