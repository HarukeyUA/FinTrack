/*
 * Copyright  2021 Nazar Rusnak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.harukeyua.fintrack.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.harukeyua.fintrack.ui.adapters.AccountListAdapter
import com.harukeyua.fintrack.ui.adapters.TransactionPagingAdapter
import com.harukeyua.fintrack.databinding.OverviewFragmentBinding
import com.harukeyua.fintrack.viewmodels.OverviewViewModel
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

    private lateinit var accountListAdapter: AccountListAdapter
    private lateinit var transactionsAdapter: TransactionPagingAdapter

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OverviewFragmentBinding.inflate(inflater, container, false)

        accountListAdapter = AccountListAdapter() {
            val action = OverviewFragmentDirections.actionOverviewFragmentToAddMoneyStoreFragment()
            findNavController().navigate(action)
        }
        binding.moneyStorePager.adapter = accountListAdapter


        transactionsAdapter = TransactionPagingAdapter()
        binding.transactionsList.adapter = transactionsAdapter
        lifecycleScope.launch {
            viewModel.transactions.collectLatest { transactionsAdapter.submitData(it) }
        }

        viewModel.showAllTransactions()

        observe()

        binding.addTransactionButton.setOnClickListener {
            val action = OverviewFragmentDirections.actionOverviewFragmentToAddTransactionFragment()
            findNavController().navigate(action)
        }

        binding.moneyStorePager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val account = accountListAdapter.getCurrentAccount(position)
                if (account == null)
                    viewModel.showAllTransactions()
                else
                    viewModel.showTransactionsForStore(account)
            }
        })

        return binding.root
    }

    private fun observe() {
        viewModel.accountsList.observe(viewLifecycleOwner) { list ->
            accountListAdapter.submitList(list)
            viewModel.updateTotalBalance(list)

            binding.addTransactionButton.visibility =
                if (list.isNotEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.transactionTypes.observe(viewLifecycleOwner) { list ->
            transactionsAdapter.transactionTypes = list
        }

        viewModel.totalBalance.observe(viewLifecycleOwner) { balance ->
            accountListAdapter.totalBalance = balance
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}