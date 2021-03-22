package com.harukeyua.fintrack.adapters

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.toSpannable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.data.model.Account
import com.harukeyua.fintrack.data.model.AccountType
import com.harukeyua.fintrack.databinding.AddNewMoneyStoreItemBinding
import com.harukeyua.fintrack.databinding.MoneyStoreCardItemBinding
import com.harukeyua.fintrack.databinding.TotalBalanceCardBinding
import com.harukeyua.fintrack.utils.getThemedColor

class AccountListAdapter(val onAddClick: () -> Unit) :
    ListAdapter<Account, RecyclerView.ViewHolder>(AccountDiffCallback()) {

    private enum class ViewTypes { TOTAL, ADD, ACCOUNT }

    var totalBalance: String? = null
        set(value) {
            field = value
            this.notifyItemChanged(0)
        }

    fun getCurrentAccount(position: Int): Account? {
        val account = getItem(position)
        return if (account.id != -1 && account.id != -2) account else null
    }

    override fun submitList(list: List<Account>?) {
        // Additional element serves as last "add" card and first "total" card
        val listWithFooter = list?.toMutableList().also {
            // "Total" card
            if (list?.isNotEmpty() == true) {
                it?.add(
                    0,
                    Account(
                        id = -1,
                        name = "",
                        type = AccountType.CASH,
                        balance = 0
                    )
                )
            }
            // "Add" card
            it?.add(
                Account(
                    id = -2,
                    name = "",
                    type = AccountType.CASH,
                    balance = 0
                )
            )
        }
        super.submitList(listWithFooter)
    }

    inner class TotalBalanceViewHolder(private val binding: TotalBalanceCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(balance: String?) {
            balance?.let {
                val spanned = SpannableStringBuilder(balance)
                spanned.append(" ")
                spanned.append(
                    binding.root.context.getString(R.string.uah_label),
                    RelativeSizeSpan(0.4f),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.balanceAmount.text = spanned.toSpannable()
            }
        }
    }


    inner class AddAccountViewHolder(private val binding: AddNewMoneyStoreItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.root.setOnClickListener {
                onAddClick()
            }
        }
    }

    class AccountViewHolder(private val binding: MoneyStoreCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Account) {
            with(binding) {
                val cardColor = when (item.type) {
                    AccountType.CARD -> root.context.getThemedColor(R.attr.colorMoneyStoreCard)
                    AccountType.CASH -> root.context.getThemedColor(R.attr.colorMoneyStoreCash)
                    AccountType.MONO -> root.context.getThemedColor(R.attr.colorMoneyStoreMonoBlack)
                }
                moneyStoreCard.setCardBackgroundColor(cardColor)

                val iconDrawable = when (item.type) {
                    AccountType.CARD -> R.drawable.ic_credit_card
                    AccountType.CASH -> R.drawable.ic_money
                    AccountType.MONO -> R.drawable.ic_credit_card
                }
                moneyStoreIcon.setImageResource(iconDrawable)
                balanceDescription.setText(R.string.store_balance)
                val spanned = SpannableStringBuilder(item.getConvertedBalance())
                spanned.append(" ")
                spanned.append(
                    binding.root.context.getString(R.string.uah_label),
                    RelativeSizeSpan(0.4f),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                balanceAmount.text = spanned.toSpannable()
                storeName.text = item.name.trim()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).id) {
            -1 -> ViewTypes.TOTAL.ordinal
            -2 -> ViewTypes.ADD.ordinal
            else -> ViewTypes.ACCOUNT.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewTypes.TOTAL.ordinal -> TotalBalanceViewHolder(
                TotalBalanceCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            ViewTypes.ADD.ordinal -> AddAccountViewHolder(
                AddNewMoneyStoreItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> AccountViewHolder(
                MoneyStoreCardItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AccountViewHolder)
            holder.bind(getItem(position))
        else if (holder is AddAccountViewHolder)
            holder.bind()
        else if (holder is TotalBalanceViewHolder)
            holder.bind(totalBalance)
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