package com.mobileappliction_bugetmanegmentapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobileappliction_bugetmanegmentapp.databinding.ActivityMainBinding
import com.mobileappliction_bugetmanegmentapp.ui.AddTransactionActivity
import com.mobileappliction_bugetmanegmentapp.ui.SetupProfileActivity
import com.mobileappliction_bugetmanegmentapp.ui.TransactionAdapter
import com.mobileappliction_bugetmanegmentapp.ui.WelcomeActivity
import com.mobileappliction_bugetmanegmentapp.viewmodel.BudgetViewModel
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: BudgetViewModel
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[BudgetViewModel::class.java]
        
        setupRecyclerView()
        setupObservers()
        
        binding.ivMenu.setOnClickListener {
            startActivity(Intent(this, com.mobileappliction_bugetmanegmentapp.ui.MenuActivity::class.java))
        }
        
        binding.btnAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        // Check user on load
        viewModel.loadUser()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTransactions()
        viewModel.loadUser()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter()
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.currentUser.observe(this) { user ->
            if (user == null) {
                // No user found, go to Setup Profile
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                adapter.updateCurrency(user.currency)
                // Removed Header Text update as we now have a static "Dashboard" title
                
                if (user.isCustomAvatar) {
                     // It's a file path
                     if (user.avatarPath.isNotEmpty()) {
                        binding.ivUserAvatar.setImageURI(Uri.fromFile(File(user.avatarPath)))
                     }
                } else {
                    // It's a resource ID (stored as string)
                    val resId = user.avatarPath.toIntOrNull()
                    if (resId != null) {
                        binding.ivUserAvatar.setImageResource(resId)
                    } else {
                         binding.ivUserAvatar.setImageResource(R.mipmap.ic_launcher_round)
                    }
                }
                
                binding.ivUserAvatar.setOnClickListener {
                    startActivity(Intent(this, com.mobileappliction_bugetmanegmentapp.ui.EditProfileActivity::class.java))
                }
            }
        }

        viewModel.transactions.observe(this) { list ->
            // Sort by Date Descending (assuming database returns random or sorted by ID)
            // Ideally VM should sort, but we can do it here or assume list is sorted.
            // Requirement: Latest 50.
            val sorted = list.sortedByDescending { it.id } // ID descending = Newest first
            adapter.submitList(sorted.take(50))
        }

        viewModel.totalBalance.observe(this) { balance ->
            val symbol = viewModel.currentUser.value?.currency ?: "$"
            binding.tvTotalBalance.text = "$symbol${String.format("%.2f", balance)}"
        }

        viewModel.income.observe(this) { income ->
            val symbol = viewModel.currentUser.value?.currency ?: "$"
            binding.tvIncome.text = "$symbol${String.format("%.2f", income)}"
        }

        viewModel.expense.observe(this) { expense ->
            val symbol = viewModel.currentUser.value?.currency ?: "$"
            binding.tvExpense.text = "$symbol${String.format("%.2f", expense)}"
            
            updateChartDisplay()
        }
    }

    private fun updateChartDisplay() {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val chartType = prefs.getString("CHART_TYPE", "Default") // Defaulting to Card (Default)
        val transactions = viewModel.transactions.value ?: emptyList()
        val income = viewModel.income.value ?: 0.0
        val expense = viewModel.expense.value ?: 0.0
        val balance = viewModel.totalBalance.value ?: 0.0

        binding.cvBalance.visibility = View.GONE
        binding.pieChart.visibility = View.GONE
        binding.barChart.visibility = View.GONE

        // Logic: The user requirement implies replacing the summary. 
        // We will show Chart IF set, otherwise (or if explicit "Card" option added later) show Card.
        // Actually, let's keep it simple: Settings has Pie/Bar. We show one of them.
        // But maybe user wants to see the BIG NUMBER?
        // Provide "Balance Card" as a fallback if setting is empty, or if we define "Pie" as "Replace Card".
        // Let's implement:
        // - Pie: Shows Income vs Expense breakdown.
        // - Bar: Shows Balance trend.
        
        when (chartType) {
            "Bar" -> {
                binding.barChart.visibility = View.VISIBLE
                setupBarChart(transactions)
            }
            "Pie" -> {
                binding.pieChart.visibility = View.VISIBLE
                setupPieChart(income, expense)
            }
            else -> {
                // Default or "Card"
                binding.cvBalance.visibility = View.VISIBLE
            }
        }
    }

    private fun setupPieChart(income: Double, expense: Double) {
        val entries = ArrayList<com.github.mikephil.charting.data.PieEntry>()
        val total = income + expense
        
        // Calculate percentages
        if (income > 0) {
            val incomePercentage = (income / total) * 100
            entries.add(com.github.mikephil.charting.data.PieEntry(incomePercentage.toFloat(), "Income"))
        }
        if (expense > 0) {
            val expensePercentage = (expense / total) * 100
            entries.add(com.github.mikephil.charting.data.PieEntry(expensePercentage.toFloat(), "Expense"))
        }

        val dataSet = com.github.mikephil.charting.data.PieDataSet(entries, "")
        dataSet.colors = listOf(
            android.graphics.Color.parseColor("#4CAF50"), // Green for Income
            android.graphics.Color.parseColor("#F44336")  // Red for Expense
        )
        
        // Configure text appearance
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = android.graphics.Color.WHITE
        dataSet.setDrawValues(true)
        
        // Format values to show percentages
        dataSet.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.1f%%", value)
            }
        }

        val data = com.github.mikephil.charting.data.PieData(dataSet)
        binding.pieChart.data = data
        binding.pieChart.description.isEnabled = false
        
        // Configure chart appearance
        binding.pieChart.centerText = "Balance\n${binding.tvTotalBalance.text}"
        binding.pieChart.setCenterTextSize(16f)
        binding.pieChart.setDrawEntryLabels(true) // Show category names
        binding.pieChart.setEntryLabelColor(android.graphics.Color.BLACK)
        binding.pieChart.setEntryLabelTextSize(12f)
        
        // Add legend to show categories with colors
        val legend = binding.pieChart.legend
        legend.isEnabled = true
        legend.textSize = 12f
        legend.formSize = 14f
        
        binding.pieChart.animateY(1000)
        binding.pieChart.invalidate() // refresh
    }


    private fun setupBarChart(transactions: List<com.mobileappliction_bugetmanegmentapp.data.Transaction>) {
        val entries = ArrayList<com.github.mikephil.charting.data.BarEntry>()
        // Sort by date/id ascending to show history
        val sorted = transactions.sortedBy { it.id } // ID is proxy for time
        
        // For Bar Chart, maybe showing Transaction Amounts? Or Balance Trend?
        // User said "Bar chart" instead of "Line chart". A line chart usually shows trend.
        // A bar chart can also show trend or individual transactions.
        // Let's stick to Balance Trend for consistency with previous Line Chart logic.
        
        var currentBalance = 0.0
        
        sorted.forEachIndexed { index, t ->
            if (t.type == "Income") currentBalance += t.amount else currentBalance -= t.amount
            entries.add(com.github.mikephil.charting.data.BarEntry(index.toFloat(), currentBalance.toFloat()))
        }

        val dataSet = com.github.mikephil.charting.data.BarDataSet(entries, "Balance Trend")
        dataSet.color = android.graphics.Color.parseColor("#2196F3")
        dataSet.valueTextSize = 10f
        
        val data = com.github.mikephil.charting.data.BarData(dataSet)
        binding.barChart.data = data
        binding.barChart.description.isEnabled = false
        binding.barChart.animateY(1000)
        binding.barChart.fitScreen()
        binding.barChart.invalidate()
    }
}
