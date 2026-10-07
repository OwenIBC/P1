package com.example.p1.ui.history

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.p1.R
import com.example.p1.databinding.FragmentHistoryBinding
import com.example.p1.ui.SessionViewModel
import kotlinx.coroutines.launch

/** Lista de cálculos válidos realizados durante la sesión. */
class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val sessionViewModel: SessionViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHistoryBinding.bind(view)
        val adapter = HistoryAdapter()
        binding.historyList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionViewModel.history.collect { entries ->
                    adapter.submitList(entries) { binding.historyList.scrollToPosition(0) }
                    binding.emptyState.isVisible = entries.isEmpty()
                    binding.historyList.isVisible = entries.isNotEmpty()
                }
            }
        }
    }
}

