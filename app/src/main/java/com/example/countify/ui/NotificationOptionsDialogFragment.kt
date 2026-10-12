package com.example.countify.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.countify.data.CounterRepository
import com.example.countify.databinding.DialogNotificationOptionsBinding
import com.example.countify.model.CounterItem
import com.example.countify.notification.NotificationHelper
import java.util.Locale

class NotificationOptionsDialogFragment(
    private val counter: CounterItem,
    private val onSaved: (CounterItem) -> Unit
) : DialogFragment() {

    private var _binding: DialogNotificationOptionsBinding? = null
    private val binding get() = _binding!!

    private var selectedHour = counter.notificationHour
    private var selectedMinute = counter.notificationMinute

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNotificationOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateTimeButtonText()

        when (counter.notificationType) {
            CounterItem.NOTIF_SAME_DAY -> binding.rbSameDay.isChecked = true
            CounterItem.NOTIF_ONE_DAY_BEFORE -> binding.rbOneDayBefore.isChecked = true
            CounterItem.NOTIF_ONE_WEEK_BEFORE -> binding.rbOneWeekBefore.isChecked = true
            CounterItem.NOTIF_CUSTOM -> {
                binding.rbCustom.isChecked = true
                binding.layoutCustomDays.visibility = View.VISIBLE
                binding.etCustomDays.setText(counter.customDaysBefore.toString())
            }
            else -> binding.rbNone.isChecked = true
        }

        binding.radioGroupNotifications.setOnCheckedChangeListener { _, checkedId ->
            binding.layoutCustomDays.visibility =
                if (checkedId == binding.rbCustom.id) View.VISIBLE else View.GONE
        }

        binding.btnTimePicker.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    selectedHour = hourOfDay
                    selectedMinute = minute
                    updateTimeButtonText()
                },
                selectedHour,
                selectedMinute,
                false
            ).show()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            val notifType = when (binding.radioGroupNotifications.checkedRadioButtonId) {
                binding.rbSameDay.id -> CounterItem.NOTIF_SAME_DAY
                binding.rbOneDayBefore.id -> CounterItem.NOTIF_ONE_DAY_BEFORE
                binding.rbOneWeekBefore.id -> CounterItem.NOTIF_ONE_WEEK_BEFORE
                binding.rbCustom.id -> CounterItem.NOTIF_CUSTOM
                else -> CounterItem.NOTIF_NONE
            }

            val customDays = binding.etCustomDays.text.toString().toIntOrNull() ?: 1

            val updatedCounter = counter.copy(
                notificationType = notifType,
                customDaysBefore = customDays,
                notificationHour = selectedHour,
                notificationMinute = selectedMinute
            )

            CounterRepository.getInstance(requireContext()).saveCounter(updatedCounter)
            NotificationHelper.scheduleNotification(requireContext(), updatedCounter)

            Toast.makeText(requireContext(), "Notification settings updated", Toast.LENGTH_SHORT).show()
            onSaved(updatedCounter)
            dismiss()
        }
    }

    private fun updateTimeButtonText() {
        val amPm = if (selectedHour >= 12) "PM" else "AM"
        val hourFormatted = when {
            selectedHour == 0 -> 12
            selectedHour > 12 -> selectedHour - 12
            else -> selectedHour
        }
        val text = String.format(Locale.US, "%02d:%02d %s", hourFormatted, selectedMinute, amPm)
        binding.btnTimePicker.text = text
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
