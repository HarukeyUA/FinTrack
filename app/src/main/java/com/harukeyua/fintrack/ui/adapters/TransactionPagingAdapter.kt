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

package com.harukeyua.fintrack.ui.adapters

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.data.model.LocationInfo
import com.harukeyua.fintrack.data.model.TransactionListItem
import com.harukeyua.fintrack.data.model.TransactionType
import com.harukeyua.fintrack.databinding.TransactionListItemBinding
import com.harukeyua.fintrack.databinding.TransactionListSeparatorBinding
import com.harukeyua.fintrack.utils.getConvertedBalance
import com.harukeyua.fintrack.utils.getThemedColor


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
                    locationText.paintFlags = locationText.paintFlags.or(Paint.UNDERLINE_TEXT_FLAG)
                    locationText.setOnClickListener {
                        item.transaction.location?.let {
                            sendMapIntent(item.transaction.location)
                        }

                    }
                }
            }
        }

        private fun sendMapIntent(location: LocationInfo) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("geo:<${location.lat}>,<${location.lon}>?q=<${location.lat}>,<${location.lon}>(${location.name})")
            )
            intent.setPackage("com.google.android.apps.maps")
            intent.resolveActivity(binding.root.context.packageManager)?.let {
                startActivity(binding.root.context, intent, null)
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
        getItem(position)?.let {
            if (holder is TransactionViewHolder)
                holder.bind(getItem(position) as TransactionListItem.Item)
            else if (holder is SeparatorViewHolder)
                holder.bind(getItem(position) as TransactionListItem.Separator)
        }
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