package com.example.countify

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.countify.data.CounterRepository
import com.example.countify.databinding.FragmentFirstBinding
import com.example.countify.model.CounterItem
import com.example.countify.notification.NotificationHelper
import com.example.countify.ui.CounterAdapter
import com.example.countify.ui.CounterOptionsBottomSheet
import com.example.countify.ui.NotificationOptionsDialogFragment
import com.example.countify.widget.CounterWidgetProvider

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CounterAdapter
    private lateinit var repository: CounterRepository

    private var pendingNotificationCounter: CounterItem? = null

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pendingNotificationCounter?.let { openNotificationOptionsDialog(it) }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Notification permission required to set reminders",
                    Toast.LENGTH_SHORT
                ).show()
            }
            pendingNotificationCounter = null
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = CounterRepository.getInstance(requireContext())

        adapter = CounterAdapter(
            onItemClick = { counter ->
                openEditScreen(counter)
            },
            onItemLongClick = { counter ->
                showOptionsBottomSheet(counter)
            }
        )

        binding.rvCounters.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCounters.adapter = adapter

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        loadCounters()
    }

    override fun onResume() {
        super.onResume()
        loadCounters()
    }

    private fun loadCounters() {
        val counters = repository.getAllCounters()
        adapter.submitList(counters)

        if (counters.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvCounters.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvCounters.visibility = View.VISIBLE
        }
    }

    private fun openEditScreen(counter: CounterItem) {
        val bundle = Bundle().apply {
            putString(SecondFragment.ARG_COUNTER_ID, counter.id)
        }
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
    }

    private fun showOptionsBottomSheet(counter: CounterItem) {
        val bottomSheet = CounterOptionsBottomSheet(
            counter = counter,
            onEditSelected = {
                openEditScreen(it)
            },
            onNotificationOptionsSelected = {
                checkNotificationPermissionAndOpen(it)
            },
            onAddWidgetSelected = {
                addWidgetToHomeScreen(it)
            },
            onDeleteSelected = {
                confirmDeleteCounter(it)
            }
        )
        bottomSheet.show(childFragmentManager, CounterOptionsBottomSheet.TAG)
    }

    private fun checkNotificationPermissionAndOpen(counter: CounterItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                openNotificationOptionsDialog(counter)
            } else {
                pendingNotificationCounter = counter
                requestNotificationPermissionLauncher.launch(permission)
            }
        } else {
            openNotificationOptionsDialog(counter)
        }
    }

    private fun openNotificationOptionsDialog(counter: CounterItem) {
        val dialog = NotificationOptionsDialogFragment(counter) { updatedCounter ->
            loadCounters()
        }
        dialog.show(childFragmentManager, "NotificationOptionsDialogFragment")
    }

    private fun addWidgetToHomeScreen(counter: CounterItem) {
        val success = CounterWidgetProvider.requestPinWidget(requireContext(), counter)
        if (success) {
            Toast.makeText(
                requireContext(),
                "Requesting to pin '${counter.title}' widget to Home Screen...",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "Widget pinning not supported on this device version. You can add the widget from your home screen widget picker!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun confirmDeleteCounter(counter: CounterItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Counter")
            .setMessage("Are you sure you want to delete '${counter.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                repository.deleteCounter(counter.id)
                NotificationHelper.cancelNotification(requireContext(), counter.id)
                CounterWidgetProvider.updateAllWidgets(requireContext())
                loadCounters()
                Toast.makeText(requireContext(), "Counter deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
