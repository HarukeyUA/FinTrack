package com.harukeyua.fintrack.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.harukeyua.fintrack.ui.adapters.ChartsListAdapter
import com.harukeyua.fintrack.databinding.AccountsChartsFragmentBinding
import com.harukeyua.fintrack.viewmodels.AccountsChartsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountsChartsFragment : Fragment() {

    private val viewModel: AccountsChartsViewModel by viewModels()

    private lateinit var binding: AccountsChartsFragmentBinding

    private val args: AccountsChartsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AccountsChartsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ChartsListAdapter()
        binding.accountsChartsList.adapter = adapter

        val appBarConfiguration = AppBarConfiguration(findNavController().graph)
        binding.topAppBar.setupWithNavController(findNavController(), appBarConfiguration)

        viewModel.chartsData.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        viewModel.getList(args.fromDateTimeStamp, args.toDateTimeStamp)

    }
}