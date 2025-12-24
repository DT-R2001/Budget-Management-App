package com.mobileappliction_bugetmanegmentapp.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobileappliction_bugetmanegmentapp.data.Transaction
import com.mobileappliction_bugetmanegmentapp.databinding.ActivityFilteredHistoryBinding
import com.mobileappliction_bugetmanegmentapp.viewmodel.BudgetViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FilteredHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFilteredHistoryBinding
    private lateinit var viewModel: BudgetViewModel
    private lateinit var adapter: TransactionAdapter
    
    // "Income" or "Expense" passed via Intent
    private var filterMode: String = "Income" 
    
    private val months = listOf("All Months", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    private val types = mutableListOf("All Types") // Will be populated dynamically

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilteredHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        filterMode = intent.getStringExtra("MODE") ?: "Income"
        binding.tvPageTitle.text = "$filterMode History"

        viewModel = ViewModelProvider(this)[BudgetViewModel::class.java]

        setupRecyclerView()
        setupSpinners()
        
        // Load data
        viewModel.loadTransactions()
        
        viewModel.transactions.observe(this) { allTransactions ->
            applyFilters(allTransactions)
            populateTypeSpinner(allTransactions)
        }

        binding.ivBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter()
        binding.rvFilteredTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvFilteredTransactions.adapter = adapter
    }

    private fun setupSpinners() {
        // Month Spinner
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter

        // Type Spinner (Initial)
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = typeAdapter

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.transactions.value?.let { applyFilters(it) }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerMonth.onItemSelectedListener = listener
        binding.spinnerType.onItemSelectedListener = listener
    }

    private fun populateTypeSpinner(transactions: List<Transaction>) {
        val distinctTypes = transactions
            .filter { it.type == filterMode } // Only show types relevant to current mode
            .map { it.title } // Ideally we should have a 'category' field, but user input 'Title' is basically the category/type in this simple app
            .distinct()
        
        // Refresh type list only if changed significantly? 
        // For simple UX, let's just add them.
        // Actually, 'Title' is user input (e.g. 'Salary from Job'). 
        // The prompt said "type vise like salary,bonus". 
        // In AddTransaction, Title is the input. Type is just Income/Expense.
        // So we will filter by Title as "Type".
        
        val currentSelection = binding.spinnerType.selectedItem as? String
        
        types.clear()
        types.add("All Types")
        types.addAll(distinctTypes)
        
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = typeAdapter
        
        // Restore selection if possible
        if (currentSelection != null && types.contains(currentSelection)) {
            binding.spinnerType.setSelection(types.indexOf(currentSelection))
        }
    }

    private fun applyFilters(transactions: List<Transaction>) {
        val selectedMonthIndex = binding.spinnerMonth.selectedItemPosition // 0 = All, 1 = Jan...
        val selectedType = binding.spinnerType.selectedItem.toString()

        val filtered = transactions.filter { t ->
            // 1. Filter by Mode (Income/Expense)
            if (t.type != filterMode) return@filter false

            // 2. Filter by Month
            var monthMatch = true
            if (selectedMonthIndex > 0) {
                // Parse date string "Fri Dec 19 07:05:00 GMT+05:30 2025" or similar
                // We stored date as `Date().toString()` in AddTransactionActivity (default Java Date.toString())
                // Let's try to parse it or just check substring if lazy.
                // Better: Use Calendar to check.
                // Since format might vary, let's look for the Month string in the date (simple heuristic for MVP).
                // Date.toString() format: "Wed Jul 04 12:08:56 PDT 2001"
                // Contains "Jan", "Feb", etc.
                
                val shortMonth = months[selectedMonthIndex].substring(0, 3) // "Jan"
                monthMatch = t.date.contains(shortMonth)
            }

            // 3. Filter by Type (Title)
            var typeMatch = true
            if (selectedType != "All Types") {
                typeMatch = t.title.equals(selectedType, ignoreCase = true)
            }

            monthMatch && typeMatch
        }
        
        adapter.submitList(filtered)
    }
}
