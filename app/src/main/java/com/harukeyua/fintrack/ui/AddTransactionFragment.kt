package com.harukeyua.fintrack.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.adapters.AccountListChooserAdapter
import com.harukeyua.fintrack.currencyInputFilter
import com.harukeyua.fintrack.databinding.AddTransactionFragmentBinding
import com.harukeyua.fintrack.utils.HorizontalMarginItemDecoration
import com.harukeyua.fintrack.viewmodels.AddTransactionViewModel
import com.harukeyua.fintrack.viewmodels.AmountErrorTypes
import com.harukeyua.fintrack.viewmodels.Operation
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@AndroidEntryPoint
class AddTransactionFragment : Fragment() {

    private val viewModel: AddTransactionViewModel by viewModels()

    private var _binding: AddTransactionFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var accountListChooserAdapter: AccountListChooserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddTransactionFragmentBinding.inflate(inflater, container, false)

        val appBarConfiguration = AppBarConfiguration(findNavController().graph)
        binding.topAppBar.setupWithNavController(findNavController(), appBarConfiguration)

        accountListChooserAdapter = AccountListChooserAdapter()
        binding.accountsList.adapter = accountListChooserAdapter
        binding.accountsList.addItemDecoration(
            HorizontalMarginItemDecoration(
                resources.getDimensionPixelSize(
                    R.dimen.margin_medium
                )
            )
        )

        binding.setDateButton.text = getString(R.string.date_standard, viewModel.selectedDate)
        binding.setTimeButton.text = getString(R.string.time_standard, viewModel.selectedTime)

        binding.transactionAmount.editText?.filters = listOf(currencyInputFilter).toTypedArray()

        observe()
        setupListeners()

        return binding.root
    }

    private fun observe() {
        viewModel.accountsList.observe(viewLifecycleOwner) { list ->
            accountListChooserAdapter.submitList(list)
        }

        viewModel.transactionTypes.observe(viewLifecycleOwner) { types ->
            binding.categoryChips.removeAllViews()
            types.forEachIndexed { index, type ->
                val chip =
                    layoutInflater.inflate(
                        R.layout.chip_choice,
                        binding.categoryChips,
                        false
                    ) as Chip
                chip.text = type.name
                chip.id = type.id
                binding.categoryChips.addView(chip)
                if (index == 0) {
                    binding.categoryChips.clearCheck()
                    binding.categoryChips.check(type.id)
                }
            }
        }

        viewModel.amountErrorEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                when (it) {
                    AmountErrorTypes.EMPTY -> binding.transactionAmount.error =
                        getString(R.string.required_field_label)
                    AmountErrorTypes.ZERO -> binding.transactionAmount.error =
                        getString(R.string.amount_zero_error)
                    AmountErrorTypes.FORMAT -> binding.transactionAmount.error =
                        getString(R.string.format_error)
                }
            }
        }

        viewModel.descriptionErrorEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                binding.transactionDescription.error = getString(R.string.required_field_label)
            }
        }

        viewModel.insufficientAmountError.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.insufficient_error_title))
                    .setMessage(getString(R.string.insufficient_error_desc))
                    .setPositiveButton(R.string.OK) { _, _ -> }
                    .show()
            }
        }

        viewModel.dbError.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.setDateButton.setOnClickListener {
            showDatePicker()
        }

        binding.addCategoryButton.setOnClickListener {
            showTypeAddDialog()
        }

        binding.setTimeButton.setOnClickListener {
            showTimePicker()
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.add_transaction) {
                val operation =
                    if (binding.toggleTransactionType.checkedButtonId == binding.buttonAdd.id) Operation.ADD else Operation.REMOVE
                viewModel.insertTransaction(
                    binding.transactionDescription.editText!!.text.toString(),
                    operation,
                    binding.transactionAmount.editText!!.text.toString(),
                    binding.categoryChips.checkedChipId,
                    accountListChooserAdapter.getSelectedAccount()
                )
                true
            } else
                false

        }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(viewModel.selectedDate.toInstant().toEpochMilli())
            .build()

        picker.addOnPositiveButtonClickListener {
            val selection = picker.selection
            selection?.let {
                val selection1 = Instant.ofEpochMilli(selection)
                val zone = ZoneOffset.systemDefault()
                viewModel.setDate(OffsetDateTime.ofInstant(selection1, zone))
                updateButtonText()
            }
        }

        picker.show(parentFragmentManager, "DatePickerDialog")
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(viewModel.selectedTime.hour).setMinute(viewModel.selectedTime.minute).build()

        picker.addOnPositiveButtonClickListener {
            viewModel.setTime(picker.hour, picker.minute)
            updateButtonText()
        }

        picker.show(parentFragmentManager, "TimePickerDialog")
    }

    private fun showTypeAddDialog() {
        val dialog = AddTypeDialog { name ->
            viewModel.insertType(name)
        }
        dialog.show(parentFragmentManager, "AddTypeDialog")
    }

    private fun updateButtonText() {
        binding.setDateButton.text = getString(R.string.date_standard, viewModel.selectedDate)
        binding.setTimeButton.text = getString(R.string.time_standard, viewModel.selectedTime)
    }
}