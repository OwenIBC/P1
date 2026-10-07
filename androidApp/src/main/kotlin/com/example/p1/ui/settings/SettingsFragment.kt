package com.example.p1.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.p1.R
import com.example.p1.databinding.FragmentSettingsBinding
import com.example.p1.ui.SessionViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/** Preferencias: efectos de sonido y gestión del historial de la sesión. */
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val sessionViewModel: SessionViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSettingsBinding.bind(view)

        // Estado inicial antes de registrar el listener para no disparar escrituras.
        binding.soundSwitch.isChecked = sessionViewModel.soundEnabled.value
        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            sessionViewModel.setSoundEnabled(isChecked)
        }

        binding.clearHistoryButton.setOnClickListener { confirmClearHistory() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionViewModel.history.collect { entries ->
                    binding.historyCount.text =
                        resources.getQuantityString(R.plurals.history_count, entries.size, entries.size)
                    binding.clearHistoryButton.isEnabled = entries.isNotEmpty()
                }
            }
        }
    }

    private fun confirmClearHistory() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_clear_history_title)
            .setMessage(R.string.dialog_clear_history_message)
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_confirm_clear) { _, _ -> sessionViewModel.clearHistory() }
            .show()
    }
}

