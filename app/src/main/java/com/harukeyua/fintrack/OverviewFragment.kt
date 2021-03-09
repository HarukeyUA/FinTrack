package com.harukeyua.fintrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.harukeyua.fintrack.adapters.MoneyStoreListAdapter
import com.harukeyua.fintrack.adapters.TransactionPagingAdapter
import com.harukeyua.fintrack.databinding.OverviewFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OverviewFragment : Fragment() {

    private val viewModel: OverviewViewModel by viewModels()
    private var _binding: OverviewFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var moneyStoreListAdapter: MoneyStoreListAdapter
    private lateinit var transactionsAdapter: TransactionPagingAdapter

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OverviewFragmentBinding.inflate(inflater, container, false)

        moneyStoreListAdapter = MoneyStoreListAdapter() {
            val action = OverviewFragmentDirections.actionOverviewFragmentToAddMoneyStoreFragment()
            findNavController().navigate(action)
        }
        binding.moneyStorePager.adapter = moneyStoreListAdapter


        transactionsAdapter = TransactionPagingAdapter()
        binding.transactionsList.adapter = transactionsAdapter
        lifecycleScope.launch {
            viewModel.transactions.collectLatest { transactionsAdapter.submitData(it) }
        }

        viewModel.showAllTransactions()

        subscribe()

        return binding.root
    }

    private fun subscribe() {
        viewModel.moneyStoreList.observe(viewLifecycleOwner) { list ->
            moneyStoreListAdapter.submitList(list)
        }

        viewModel.transactionTypes.observe(viewLifecycleOwner) { list ->
            transactionsAdapter.transactionTypes = list
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}