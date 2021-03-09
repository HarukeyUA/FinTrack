package com.harukeyua.fintrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.harukeyua.fintrack.data.model.StoreType
import com.harukeyua.fintrack.databinding.AddMoneyStoreFragmentBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddMoneyStoreFragment : Fragment() {

    private val viewModel: AddMoneyStoreViewModel by viewModels()
    private var _binding: AddMoneyStoreFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddMoneyStoreFragmentBinding.inflate(inflater, container, false)

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
                    AddMoneyStoreFragmentDirections.actionAddMoneyStoreFragmentToOverviewFragment()
                findNavController().navigate(action)
            }
        }
    }

    private fun setupListeners() {
        binding.accountBalanceText.filters = listOf(currencyInputFilter).toTypedArray()

        binding.addButton.setOnClickListener {
            val accountName = binding.accountName.editText?.text.toString()
            val accountType =
                when ((binding.accountType.editText as AutoCompleteTextView).text.toString()) {
                    resources.getString(R.string.account_type_card) -> StoreType.CARD
                    resources.getString(R.string.account_type_wallet) -> StoreType.CASH
                    else -> StoreType.CARD
                }
            val accountBalance = binding.accountBalanceText.text.toString()
            viewModel.addAccount(accountName, accountType, accountBalance)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}