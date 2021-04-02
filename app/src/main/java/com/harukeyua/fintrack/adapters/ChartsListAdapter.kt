package com.harukeyua.fintrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.data.model.Account
import com.harukeyua.fintrack.databinding.ChartListItemBinding
import com.harukeyua.fintrack.utils.getThemedColor
import java.time.OffsetDateTime
import java.util.*

class ChartsListAdapter :
    ListAdapter<Pair<Account, List<Entry>>, ChartsListAdapter.ChartViewHolder>(DiffCallback()) {

    class ChartViewHolder(private val binding: ChartListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            with(binding.chart) {
                description.isEnabled = false
                setTouchEnabled(false)
                isDragEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)
                axisRight.isEnabled = false
                axisLeft.setLabelCount(4, false)
                axisLeft.setDrawAxisLine(false)
                legend.isEnabled = false
                animateY(1000, Easing.EaseInOutQuad)
                xAxis.setDrawAxisLine(false)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setLabelCount(5, true)
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                        return binding.root.context.getString(
                            R.string.chart_date_axis_label,
                            OffsetDateTime.now().withDayOfYear(value.toInt())
                        )
                    }
                }
                invalidate()
            }
        }

        fun bind(data: Pair<Account, List<Entry>>) {
            with(binding) {
                chartTitleText.text = data.first.name.capitalize(Locale.getDefault())

                if (chart.data != null && chart.data.dataSetCount > 0) {
                    val setl = chart.data.getDataSetByIndex(0) as LineDataSet
                    setl.values = data.second
                    chart.data.notifyDataChanged()
                    chart.notifyDataSetChanged()
                    chart.invalidate()
                } else {
                    val setl = LineDataSet(data.second, "1")

                    with(setl) {
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        cubicIntensity = 0.15f
                        setDrawCircles(false)
                        lineWidth = 4f
                        setDrawHighlightIndicators(false)
                        color = root.context.getThemedColor(R.attr.colorPrimary)
                    }

                    val chartData = LineData(setl)
                    chartData.setDrawValues(false)
                    binding.chart.data = chartData
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartViewHolder {
        return ChartViewHolder(
            ChartListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ChartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<Pair<Account, List<Entry>>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Account, List<Entry>>,
            newItem: Pair<Account, List<Entry>>
        ): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(
            oldItem: Pair<Account, List<Entry>>,
            newItem: Pair<Account, List<Entry>>
        ): Boolean {
            return oldItem.first == newItem.first
        }

    }
}