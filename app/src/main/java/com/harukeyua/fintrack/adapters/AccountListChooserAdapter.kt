package com.harukeyua.fintrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.data.model.Account
import com.harukeyua.fintrack.data.model.AccountType
import com.harukeyua.fintrack.databinding.AccountSmallCardBinding
import com.harukeyua.fintrack.getThemedColor

class AccountListChooserAdapter :
    ListAdapter<Account, AccountListChooserAdapter.AccountViewHolder>(AccountDiffCallback()) {

    private var selectedCard = 0
        private set(value) {
            val oldValue = selectedCard
            field = value
            notifyItemChanged(oldValue)
            notifyItemChanged(value)
        }

    fun getSelectedAccount(): Account? = if (itemCount != 0) getItem(selectedCard) else null

    inner class AccountViewHolder(private val binding: AccountSmallCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                selectedCard = layoutPosition
            }
        }

        fun bind(item: Account, position: Int) {
            with(binding) {
                val cardColor = when (item.type) {
                    AccountType.CARD -> root.context.getThemedColor(R.attr.colorMoneyStoreCard)
                    AccountType.CASH -> root.context.getThemedColor(R.attr.colorMoneyStoreCash)
                    AccountType.MONO -> root.context.getThemedColor(R.attr.colorMoneyStoreMonoBlack)
                }
                accountCard.setCardBackgroundColor(cardColor)

                val iconDrawable = when (item.type) {
                    AccountType.CARD -> R.drawable.ic_credit_card
                    AccountType.CASH -> R.drawable.ic_money
                    AccountType.MONO -> R.drawable.ic_credit_card
                }
                accountIcon.setImageResource(iconDrawable)
                accountName.text = item.name.trim()

                root.isChecked = selectedCard == position

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    root.tooltipText = item.name
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        return AccountViewHolder(
            AccountSmallCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    private class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem == newItem
        }

    }
}