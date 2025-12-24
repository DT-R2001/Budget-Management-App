package com.mobileappliction_bugetmanegmentapp.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobileappliction_bugetmanegmentapp.data.Transaction
import com.mobileappliction_bugetmanegmentapp.databinding.ItemTransactionBinding

class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private var transactionList = listOf<Transaction>()
    private var currencySymbol: String = "$"

    fun submitList(list: List<Transaction>) {
        transactionList = list
        notifyDataSetChanged()
    }

    fun updateCurrency(symbol: String) {
        currencySymbol = symbol
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(transaction: Transaction) {
            binding.tvTitle.text = transaction.title
            binding.tvDate.text = transaction.date
            binding.tvCategory.text = transaction.category
            
            // Set category color
            try {
                binding.viewCategoryColor.setBackgroundColor(Color.parseColor(transaction.categoryColor))
            } catch (e: Exception) {
                binding.viewCategoryColor.setBackgroundColor(Color.parseColor("#757575"))
            }
            
            if (transaction.type == "Income") {
                binding.tvAmount.text = "+$currencySymbol${String.format("%.2f", transaction.amount)}"
                binding.tvAmount.setTextColor(Color.parseColor("#4CAF50")) // Green
            } else {
                binding.tvAmount.text = "-$currencySymbol${String.format("%.2f", transaction.amount)}"
                binding.tvAmount.setTextColor(Color.parseColor("#F44336")) // Red
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactionList[position])
    }

    override fun getItemCount() = transactionList.size
}
