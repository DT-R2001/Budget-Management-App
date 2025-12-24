package com.mobileappliction_bugetmanegmentapp.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.mobileappliction_bugetmanegmentapp.R
import com.mobileappliction_bugetmanegmentapp.data.User
import com.mobileappliction_bugetmanegmentapp.databinding.ActivitySettingsBinding
import com.mobileappliction_bugetmanegmentapp.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: BudgetViewModel
    private lateinit var prefs: SharedPreferences
    
    // User object to update
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[BudgetViewModel::class.java]
        prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        setupCurrencySpinner()
        setupThemeOptions()
        setupChartOptions()
        
        // Handle Mode (Currency vs Appearance)
        val mode = intent.getStringExtra("SETTINGS_MODE") ?: "All"
        
        when (mode) {
            "Currency" -> {
                binding.cvAppearance.visibility = android.view.View.GONE
                binding.tvTitle.text = "Currency Settings"
            }
            "Appearance" -> {
                binding.cvCurrency.visibility = android.view.View.GONE
                binding.tvTitle.text = "Appearance Settings"
            }
            else -> {
                // Show both
                binding.tvTitle.text = "Settings"
            }
        }
        
        viewModel.loadTransactions()
        viewModel.loadUser()
        viewModel.currentUser.observe(this) { user ->
            currentUser = user
            if (user != null) {
                setCurrencySpinnerSelection(user.currency)
            }
        }

        binding.btnSaveCurrency.setOnClickListener {
            saveCurrency()
        }
    }

    private fun setupCurrencySpinner() {
        val currencies = listOf("$ (USD)", "€ (EUR)", "£ (GBP)", "Rs (LKR)", "₹ (INR)", "¥ (JPY)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter
    }
    
    private fun setCurrencySpinnerSelection(symbol: String) {
        // Simple matching logic
        val adapter = binding.spinnerCurrency.adapter as ArrayAdapter<String>
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i)?.startsWith(symbol) == true) {
                binding.spinnerCurrency.setSelection(i)
                break
            }
        }
    }

    private fun saveCurrency() {
        val selectedCurrency = binding.spinnerCurrency.selectedItem.toString().split(" ")[0]
        val currentCurrency = currentUser?.currency ?: "$"
        
        if (selectedCurrency == currentCurrency) {
            return // No change
        }

        // Check if transactions exist
        val hasTransactions = (viewModel.transactions.value?.size ?: 0) > 0

        if (hasTransactions) {
            showConversionDialog(currentCurrency, selectedCurrency)
        } else {
            // No transactions, just update currency directly
             val updated = currentUser?.copy(currency = selectedCurrency) ?: User(0, "User", "", false, selectedCurrency)
             viewModel.saveUser(updated)
             Toast.makeText(this, "Currency Updated", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showConversionDialog(oldCurr: String, newCurr: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_conversion_rate, null)
        val etRate = dialogView.findViewById<android.widget.EditText>(R.id.etExchangeRate)
        val tvMsg = dialogView.findViewById<android.widget.TextView>(R.id.tvConversionMessage)
        val btnSwap = dialogView.findViewById<android.widget.ImageButton>(R.id.btnSwap)
        
        var isSwapped = false

        fun updateMessage() {
            if (isSwapped) {
                tvMsg.text = "1 $newCurr = ? $oldCurr"
            } else {
                tvMsg.text = "1 $oldCurr = ? $newCurr"
            }
        }
        updateMessage()

        btnSwap.setOnClickListener {
            isSwapped = !isSwapped
            updateMessage()
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Convert Currency")
            .setView(dialogView)
            .setPositiveButton("Convert") { _, _ ->
                val rateStr = etRate.text.toString()
                if (rateStr.isNotEmpty()) {
                    var rate = rateStr.toDoubleOrNull()
                    if (rate != null) {
                        // If swapped: 1 New = X Old -> New/Old = X -> Old/New = 1/X
                        // We need rate Old -> New
                        if (isSwapped) {
                            rate = 1.0 / rate
                        }
                        
                        // Use lifecycleScope to run suspend function
                        lifecycleScope.launch {
                            viewModel.updateCurrencyAndConvert(newCurr, rate)
                            Toast.makeText(this@SettingsActivity, "Currency & Transactions Updated", Toast.LENGTH_SHORT).show()
                            val intent = android.content.Intent(this@SettingsActivity, com.mobileappliction_bugetmanegmentapp.MainActivity::class.java)
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish() // Go to Dashboard
                        }
                    } else {
                        Toast.makeText(this, "Invalid Rate", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupThemeOptions() {
        val currentTheme = prefs.getInt("THEME_MODE", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> binding.rbSystem.isChecked = true
            AppCompatDelegate.MODE_NIGHT_NO -> binding.rbLight.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.rbDark.isChecked = true
        }

        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rbLight -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.rbDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(mode)
            prefs.edit().putInt("THEME_MODE", mode).apply()
        }
    }

    private fun setupChartOptions() {
        val chartType = prefs.getString("CHART_TYPE", "Default")
        
        when (chartType) {
            "Bar" -> binding.rbBar.isChecked = true
            "Pie" -> binding.rbPie.isChecked = true
            else -> binding.rbDefault.isChecked = true
        }

        binding.rgChart.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                R.id.rbBar -> "Bar"
                R.id.rbPie -> "Pie"
                else -> "Default"
            }
            prefs.edit().putString("CHART_TYPE", type).apply()
            // Navigate to Dashboard immediately
            finish()
        }
    }
}
