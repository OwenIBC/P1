package com.example.p1.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.p1.databinding.ItemHistoryBinding
import com.example.p1.model.HistoryEntry
import com.example.p1.util.formatQuantity
import java.text.DateFormat
import java.util.Date

/** Adaptador inmutable (ListAdapter + DiffUtil) para el historial de la sesión. */
class HistoryAdapter : ListAdapter<HistoryEntry, HistoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class ViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {

        private val timeFormat: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)

        fun bind(entry: HistoryEntry) {
            val context = binding.root.context
            val formula = entry.formula

            binding.formulaTitle.setText(formula.titleRes)
            binding.formulaExpression.setText(formula.expressionRes)
            binding.timestamp.text = timeFormat.format(Date(entry.timestampMillis))

            // Una línea por dato ingresado, en el mismo orden que el formulario.
            binding.inputsSummary.text = formula.fields
                .mapNotNull { field ->
                    entry.inputs[field]?.let { value -> context.formatQuantity(field.symbolRes, value, field.unitRes) }
                }
                .joinToString(separator = "\n")

            binding.resultValue.text = context.formatQuantity(formula.resultSymbolRes, entry.result, formula.resultUnitRes)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<HistoryEntry>() {
        override fun areItemsTheSame(oldItem: HistoryEntry, newItem: HistoryEntry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: HistoryEntry, newItem: HistoryEntry) = oldItem == newItem
    }
}

