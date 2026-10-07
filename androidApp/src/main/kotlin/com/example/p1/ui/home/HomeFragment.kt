package com.example.p1.ui.home

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.RawRes
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.p1.R
import com.example.p1.databinding.FragmentHomeBinding
import com.example.p1.media.SoundEffectPlayer
import com.example.p1.model.CalculationOutcome
import com.example.p1.model.Formula
import com.example.p1.ui.SessionViewModel
import com.example.p1.util.formatQuantity
import com.example.p1.util.resolve
import kotlinx.coroutines.launch

/**
 * Pantalla de Inicio: selector de fórmula, formulario dinámico, cálculo y retroalimentación.
 *
 * Patrón de binding sin `!!` ni propiedades nulables: el [FragmentHomeBinding] se crea en
 * [onViewCreated] como `val` local y sólo lo capturan objetos ligados a `viewLifecycleOwner`,
 * así que no hay fugas de vista tras `onDestroyView`.
 */
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val homeViewModel: HomeViewModel by viewModels()
    private val sessionViewModel: SessionViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHomeBinding.bind(view)

        // MediaPlayer ligado al ciclo de vida de la VISTA: se libera en onStop/onDestroy.
        val soundPlayer = SoundEffectPlayer(requireContext())
        viewLifecycleOwner.lifecycle.addObserver(soundPlayer)

        val formController = DynamicFormController(binding.fieldsContainer, layoutInflater)

        setupFormulaMenu(binding)
        binding.calculateButton.setOnClickListener { onCalculateClicked(binding, formController, soundPlayer) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.uiState.collect { state ->
                    render(binding, formController, soundPlayer, state)
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // Configuración
    // ---------------------------------------------------------------------------------------

    private fun setupFormulaMenu(binding: FragmentHomeBinding) {
        val formulas = Formula.entries
        val titles = formulas.map { getString(it.titleRes) }
        binding.formulaDropdown.setAdapter(ArrayAdapter(requireContext(), R.layout.item_dropdown_formula, titles))
        binding.formulaDropdown.setOnItemClickListener { _, _, position, _ ->
            formulas.getOrNull(position)?.let(homeViewModel::selectFormula)
        }
    }

    // ---------------------------------------------------------------------------------------
    // Renderizado del estado
    // ---------------------------------------------------------------------------------------

    private fun render(
        binding: FragmentHomeBinding,
        formController: DynamicFormController,
        soundPlayer: SoundEffectPlayer,
        state: HomeUiState,
    ) {
        val formula = state.formula
        val formulaTitle = getString(formula.titleRes)

        // `filter = false` evita que el AutoCompleteTextView filtre la lista a un único elemento.
        if (binding.formulaDropdown.text?.toString() != formulaTitle) {
            binding.formulaDropdown.setText(formulaTitle, false)
        }

        // Sólo al cambiar de fórmula: campos, imagen y expresión.
        val rebuilt = formController.bind(formula, homeViewModel::inputText, homeViewModel::onInputChanged)
        if (rebuilt) {
            binding.formulaImage.setImageResource(formula.imageRes)
            binding.formulaImage.contentDescription = getString(R.string.cd_formula_image, formulaTitle)
            binding.formulaExpression.setText(formula.expressionRes)
            formController.setOnDoneAction { onCalculateClicked(binding, formController, soundPlayer) }
        }

        formController.showErrors(state.fieldErrors) { error -> requireContext().resolve(error) }
        renderFeedback(binding, formula, state.feedback)
    }

    private fun renderFeedback(binding: FragmentHomeBinding, formula: Formula, feedback: Feedback) {
        binding.resultCard.isVisible = feedback != Feedback.Idle
        when (feedback) {
            Feedback.Idle -> Unit

            Feedback.Failure -> {
                binding.memeImage.setImageResource(R.drawable.meme1)
                binding.memeImage.contentDescription = getString(R.string.cd_meme_error)
                binding.resultTitle.setText(R.string.result_title_error)
                binding.resultValue.setText(R.string.result_message_error)
            }

            is Feedback.Success -> {
                binding.memeImage.setImageResource(R.drawable.meme2)
                binding.memeImage.contentDescription = getString(R.string.cd_meme_success)
                binding.resultTitle.setText(R.string.result_title_success)
                binding.resultValue.text = requireContext().formatQuantity(
                    formula.resultSymbolRes,
                    feedback.result,
                    formula.resultUnitRes,
                )
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // Acciones
    // ---------------------------------------------------------------------------------------

    private fun onCalculateClicked(
        binding: FragmentHomeBinding,
        formController: DynamicFormController,
        soundPlayer: SoundEffectPlayer,
    ) {
        hideKeyboard(binding.root)

        when (val outcome = homeViewModel.calculate()) {
            is CalculationOutcome.Success -> {
                sessionViewModel.record(outcome)
                playIfEnabled(soundPlayer, R.raw.matematicashijo)
            }
            is CalculationOutcome.Invalid -> {
                formController.focusFirstError(outcome.errors)
                playIfEnabled(soundPlayer, R.raw.risa)
            }
        }
        // Tras el layout, desplaza hasta la tarjeta con el meme/resultado.
        binding.scrollView.post { binding.scrollView.smoothScrollTo(0, binding.resultCard.top) }
    }

    private fun playIfEnabled(soundPlayer: SoundEffectPlayer, @RawRes soundRes: Int) {
        if (sessionViewModel.soundEnabled.value) soundPlayer.play(soundRes)
    }

    private fun hideKeyboard(view: View) {
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).hide(WindowInsetsCompat.Type.ime())
        }
    }
}

