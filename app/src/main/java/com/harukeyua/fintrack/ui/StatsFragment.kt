package com.harukeyua.fintrack.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.MaterialDatePicker
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.databinding.StatsFragmentBinding
import com.harukeyua.fintrack.utils.getConvertedBalance
import com.harukeyua.fintrack.utils.getThemedColor
import com.harukeyua.fintrack.viewmodels.StatsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@AndroidEntryPoint
class StatsFragment : Fragment() {

    private var _binding: StatsFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = StatsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.dateRangePickButton.setOnClickListener {
            showDatePicker()
        }
        binding.balanceHistoryButton.setOnClickListener {
            viewModel.selectedDateRange.value?.let {
                val action = StatsFragmentDirections.actionStatsFragmentToAccountsChartsFragment(
                    it.first.toEpochSecond(),
                    it.second.toEpochSecond()
                )
                findNavController().navigate(action)
            }

        }
        setupChart()
        observe()
    }

    private fun setupChart() {
        with(binding.pieChartExpenses) {
            setUsePercentValues(true)
            description.isEnabled = false
            animateY(1400, Easing.EaseInOutQuad)
            setDrawEntryLabels(false)
            extraRightOffset = 40f
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            legend.setDrawInside(false)
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.textSize = 12f
            invalidate()
        }

        with(binding.pieChartIncome) {
            setUsePercentValues(true)
            description.isEnabled = false
            animateY(1400, Easing.EaseInOutQuad)
            setDrawEntryLabels(false)
            extraRightOffset = 40f
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            legend.setDrawInside(false)
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.textSize = 12f
            invalidate()
        }

        with(binding.pieChartPlaces) {
            setUsePercentValues(true)
            description.isEnabled = false
            animateY(1400, Easing.EaseInOutQuad)
            setDrawEntryLabels(false)
            extraRightOffset = 40f
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            legend.setDrawInside(false)
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.textSize = 12f
            invalidate()
        }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setSelection(
                androidx.core.util.Pair(
                    viewModel.selectedDateRange.value!!.first.toInstant().toEpochMilli(),
                    viewModel.selectedDateRange.value!!.second.toInstant().toEpochMilli()
                )
            )
            .build()

        picker.addOnPositiveButtonClickListener {
            val selection = picker.selection
            selection?.let {
                if (selection.first != null && selection.second != null) {
                    val selectionStart = Instant.ofEpochMilli(selection.first as Long)
                    val selectionEnd = Instant.ofEpochMilli(selection.second as Long)
                    val zone = ZoneOffset.systemDefault()
                    viewModel.setDate(
                        OffsetDateTime.ofInstant(selectionStart, zone),
                        OffsetDateTime.ofInstant(selectionEnd, zone)
                    )
                }
            }
        }

        picker.show(parentFragmentManager, "DatePickerDialog")
    }

    private fun observe() {
        viewModel.selectedDateRange.observe(viewLifecycleOwner) { range ->
            binding.dateRangePickButton.text =
                getString(R.string.date_range_standard, range.first, range.second)
        }

        viewModel.totalGainsTransactions.observe(viewLifecycleOwner) { amount ->
            amount?.let {
                binding.gainsText.text = "+${getConvertedBalance(amount)}"
            }
        }

        viewModel.totalExpensesTransactions.observe(viewLifecycleOwner) { amount ->
            amount?.let {
                binding.expensesText.text = getConvertedBalance(amount)
            }
        }

        viewModel.totalTransactions.observe(viewLifecycleOwner) { amount ->
            amount?.let {
                val color =
                    if (amount > 0) requireContext().getThemedColor(R.attr.colorTransactionGain) else requireContext().getThemedColor(
                        R.attr.colorTransactionSpend
                    )
                with(binding) {
                    totalText.text =
                        if (amount > 0) "+${getConvertedBalance(amount)}" else getConvertedBalance(
                            amount
                        )
                    totalText.setTextColor(color)
                    totalLabel.setTextColor(color)
                    summarySeparatorView.setBackgroundColor(color)
                }
            }
        }

        viewModel.transactionsExpensesTypeChartData.observe(viewLifecycleOwner) { list ->
            val dataSet = PieDataSet(list, null)

            dataSet.setDrawIcons(false)
            dataSet.sliceSpace = 3f
            dataSet.selectionShift = 0f
            val colors = ColorTemplate.JOYFUL_COLORS.toMutableList()
            colors.addAll(ColorTemplate.MATERIAL_COLORS.toList())
            colors.addAll(ColorTemplate.COLORFUL_COLORS.toList())
            colors.addAll(ColorTemplate.PASTEL_COLORS.toList())
            colors.addAll(ColorTemplate.LIBERTY_COLORS.toList())
            colors.addAll(ColorTemplate.VORDIPLOM_COLORS.toList())
            dataSet.colors = colors
            val data = PieData(dataSet)
            data.setValueFormatter(CustomFormatter(binding.pieChartExpenses))
            data.setValueTextSize(14f)
            binding.pieChartExpenses.data = data
            binding.pieChartExpenses.invalidate()
        }

        viewModel.transactionsIncomeTypeChartData.observe(viewLifecycleOwner) { list ->
            val dataSet = PieDataSet(list, null)

            dataSet.setDrawIcons(false)
            dataSet.sliceSpace = 3f
            dataSet.selectionShift = 0f
            val colors = ColorTemplate.JOYFUL_COLORS.toMutableList()
            colors.addAll(ColorTemplate.MATERIAL_COLORS.toList())
            colors.addAll(ColorTemplate.COLORFUL_COLORS.toList())
            colors.addAll(ColorTemplate.PASTEL_COLORS.toList())
            colors.addAll(ColorTemplate.LIBERTY_COLORS.toList())
            colors.addAll(ColorTemplate.VORDIPLOM_COLORS.toList())
            dataSet.colors = colors
            val data = PieData(dataSet)
            data.setValueFormatter(CustomFormatter(binding.pieChartExpenses))
            data.setValueTextSize(14f)
            binding.pieChartIncome.data = data
            binding.pieChartIncome.invalidate()
        }

        viewModel.transactionsPlaceChartData.observe(viewLifecycleOwner) { list ->
            val dataSet = PieDataSet(list, null)

            dataSet.setDrawIcons(false)
            dataSet.sliceSpace = 3f
            dataSet.selectionShift = 0f
            val colors = ColorTemplate.JOYFUL_COLORS.toMutableList()
            colors.addAll(ColorTemplate.MATERIAL_COLORS.toList())
            colors.addAll(ColorTemplate.COLORFUL_COLORS.toList())
            colors.addAll(ColorTemplate.PASTEL_COLORS.toList())
            colors.addAll(ColorTemplate.LIBERTY_COLORS.toList())
            colors.addAll(ColorTemplate.VORDIPLOM_COLORS.toList())
            dataSet.colors = colors
            val data = PieData(dataSet)
            data.setValueFormatter(CustomFormatter(binding.pieChartExpenses))
            data.setValueTextSize(14f)
            binding.pieChartPlaces.data = data
            binding.pieChartPlaces.invalidate()
        }
    }

}

class CustomFormatter(chart: PieChart): PercentFormatter(chart) {
    override fun getFormattedValue(value: Float): String {
        val valueString = super.getFormattedValue(value)
        return if (value < 5f) "" else valueString
    }
}

