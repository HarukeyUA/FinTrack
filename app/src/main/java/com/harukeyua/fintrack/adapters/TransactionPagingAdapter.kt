package com.harukeyua.fintrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.data.model.TransactionListItem
import com.harukeyua.fintrack.data.model.TransactionType
import com.harukeyua.fintrack.databinding.TransactionListItemBinding
import com.harukeyua.fintrack.databinding.TransactionListSeparatorBinding
import com.harukeyua.fintrack.getConvertedBalance
import com.harukeyua.fintrack.getThemedColor

class TransactionPagingAdapter :
    PagingDataAdapter<TransactionListItem, RecyclerView.ViewHolder>(TransactionDiffCallback()) {

    var transactionTypes: List<TransactionType>? = null

    private enum class ViewType { ITEM, SEPARATOR }

    private inner class TransactionViewHolder(private val binding: TransactionListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionListItem.Item?) {
            item?.let {
                with(binding) {
                    transactionDescription.text = item.transaction.description.trim()
                    locationText.text = item.transaction.location?.name?.trim()
                    var amount = getConvertedBalance(item.transaction.amount)
                    if (item.transaction.amount > 0) {
                        amount = "+$amount"
                        operationText.setTextColor(root.context.getThemedColor(R.attr.colorTransactionGain))
                    } else {
                        operationText.setTextColor(root.context.getThemedColor(R.attr.colorTransactionSpend))
                    }
                    operationText.text = amount
                    categoryText.text =
                        transactionTypes?.find { it.id == item.transaction.transactionTypeId }?.name
                            ?: ""
                }
            }
        }
    }

    private class SeparatorViewHolder(private val binding: TransactionListSeparatorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionListItem.Separator?) {
            item?.let {
                with(binding) {
                    dateText.text =
                        root.context.getString(R.string.date_separator_text, item.offsetDateTime)
                }
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TransactionListItem.Separator -> ViewType.SEPARATOR.ordinal
            else -> ViewType.ITEM.ordinal
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TransactionViewHolder)
            holder.bind(getItem(position) as TransactionListItem.Item)
        else if (holder is SeparatorViewHolder)
            holder.bind(getItem(position) as TransactionListItem.Separator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.SEPARATOR.ordinal -> SeparatorViewHolder(
                TransactionListSeparatorBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> TransactionViewHolder(
                TransactionListItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }


    private class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionListItem>() {
        override fun areItemsTheSame(
            oldItem: TransactionListItem,
            newItem: TransactionListItem
        ): Boolean {
            return if (oldItem is TransactionListItem.Item && newItem is TransactionListItem.Item) {
                oldItem.transaction == newItem.transaction
            } else if (oldItem is TransactionListItem.Separator && newItem is TransactionListItem.Separator) {
                oldItem.offsetDateTime == newItem.offsetDateTime
            } else {
                oldItem == newItem
            }
        }

        override fun areContentsTheSame(
            oldItem: TransactionListItem,
            newItem: TransactionListItem
        ): Boolean {
            return oldItem == newItem
        }

    }

}