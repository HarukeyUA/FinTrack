package com.harukeyua.fintrack.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.utils.currencyInputFilter
import com.harukeyua.fintrack.data.model.AccountType
import com.harukeyua.fintrack.databinding.AddAccountFragmentBinding
import com.harukeyua.fintrack.viewmodels.AddMoneyStoreViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddAccountFragment : Fragment() {

    private val viewModel: AddMoneyStoreViewModel by viewModels()
    private var _binding: AddAccountFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddAccountFragmentBinding.inflate(inflater, container, false)

        val appBarConfiguration = AppBarConfiguration(findNavController().graph)
        binding.topAppBar.setupWithNavController(findNavController(), appBarConfiguration)

        setupAutocompleteText()
        subscribe()
        setupListeners()

        return binding.root
    }

    private fun setupAutocompleteText() {
        val items =
            listOf(
                resources.getString(R.string.account_type_card),
                resources.getString(R.string.account_type_wallet)
            )
        val adapter = ArrayAdapter(requireContext(), R.layout.list_select_item, items)
        (binding.accountType.editText as AutoCompleteTextView).setAdapter(adapter)
        (binding.accountType.editText as AutoCompleteTextView).setText(items[0], false)
    }

    private fun subscribe() {
        viewModel.accountNameError.observe(viewLifecycleOwner) { status ->
            binding.accountName.error = if (status) getString(R.string.account_name_error) else null
        }

        viewModel.navigateToOverview.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                val action =
                    AddAccountFragmentDirections.actionAddMoneyStoreFragmentToOverviewFragment()
                findNavController().navigate(action)
            }
        }
    }

    private fun setupListeners() {
        binding.accountBalanceText.filters = listOf(currencyInputFilter).toTypedArray()

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.add_transaction) {
                val accountName = binding.accountName.editText?.text.toString()
                val accountType =
                    when ((binding.accountType.editText as AutoCompleteTextView).text.toString()) {
                        resources.getString(R.string.account_type_card) -> AccountType.CARD
                        resources.getString(R.string.account_type_wallet) -> AccountType.CASH
                        else -> AccountType.CARD
                    }
                val accountBalance = binding.accountBalanceText.text.toString()
                viewModel.addAccount(accountName, accountType, accountBalance)
                true
            } else
                false
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}