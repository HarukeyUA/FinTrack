package com.harukeyua.fintrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.data.model.MoneyStore
import com.harukeyua.fintrack.data.model.StoreType
import com.harukeyua.fintrack.databinding.AddNewMoneyStoreItemBinding
import com.harukeyua.fintrack.databinding.MoneyStoreCardItemBinding
import com.harukeyua.fintrack.getThemedColor

class MoneyStoreListAdapter(val onAddClick: () -> Unit) :
    ListAdapter<MoneyStore, RecyclerView.ViewHolder>(MoneyStoreDiffCallback()) {

    override fun submitList(list: List<MoneyStore>?) {
        // Additional element serves as last "add" card
        val listWithFooter = list?.toMutableList().also {
            it?.add(
                MoneyStore(
                    id = -1,
                    name = "",
                    type = StoreType.CASH,
                    balance = 0
                )
            )
        }
        super.submitList(listWithFooter)
    }


    inner class AddStoreViewHolder(private val binding: AddNewMoneyStoreItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.root.setOnClickListener {
                onAddClick()
            }
        }
    }

    class MoneyStoreViewHolder(private val binding: MoneyStoreCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MoneyStore) {
            with(binding) {
                val cardColor = when (item.type) {
                    StoreType.CARD -> root.context.getThemedColor(R.attr.colorMoneyStoreCard)
                    StoreType.CASH -> root.context.getThemedColor(R.attr.colorMoneyStoreCash)
                    StoreType.MONO -> root.context.getThemedColor(R.attr.colorMoneyStoreMonoBlack)
                }
                moneyStoreCard.setCardBackgroundColor(cardColor)

                val iconDrawable = when (item.type) {
                    StoreType.CARD -> R.drawable.ic_credit_card
                    StoreType.CASH -> R.drawable.ic_money
                    StoreType.MONO -> R.drawable.ic_credit_card
                }
                moneyStoreIcon.setImageResource(iconDrawable)
                balanceDescription.setText(R.string.store_balance)
                balanceAmount.text = item.getConvertedBalance()
                storeName.text = item.name.trim()
            }
        }
    }

    override fun getItemViewType(position: Int): Int = if (position != itemCount - 1) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> MoneyStoreViewHolder(
                MoneyStoreCardItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> AddStoreViewHolder(
                AddNewMoneyStoreItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MoneyStoreViewHolder)
            holder.bind(getItem(position))
        else if (holder is AddStoreViewHolder)
            holder.bind()
    }

    private class MoneyStoreDiffCallback : DiffUtil.ItemCallback<MoneyStore>() {
        override fun areItemsTheSame(oldItem: MoneyStore, newItem: MoneyStore): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MoneyStore, newItem: MoneyStore): Boolean {
            return oldItem == newItem
        }

    }
}