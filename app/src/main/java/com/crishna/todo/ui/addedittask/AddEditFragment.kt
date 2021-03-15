package com.crishna.todo.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.crishna.todo.R
import com.crishna.todo.databinding.FragmentAddEditTaskBinding
import com.crishna.todo.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class AddEditFragment : Fragment(R.layout.fragment_add_edit_task) {

    private val viewModel: AddEditTaskViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditTaskBinding.bind(view)

        binding.apply {
            editTextTaskName.setText(viewModel.taskName)
            checkImpTask.isChecked = viewModel.taskImportance

            checkImpTask.jumpDrawablesToCurrentState()
            textDateCreated.isVisible = viewModel.task != null
            textDateCreated.text = "Created: ${viewModel.task?.createdDateFormatted}"

            editTextTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()
            }

            checkImpTask.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }

            floatAddTask.setOnClickListener {
                viewModel.onSaveClick()
            }

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->

                when (event) {
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        binding.editTextTaskName.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()

                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {

                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
        }
    }
}