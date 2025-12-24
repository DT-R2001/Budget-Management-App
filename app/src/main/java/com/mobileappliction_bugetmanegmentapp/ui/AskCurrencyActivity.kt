package com.mobileappliction_bugetmanegmentapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.mobileappliction_bugetmanegmentapp.databinding.ActivityAskCurrencyBinding

class AskCurrencyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAskCurrencyBinding
    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAskCurrencyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userName = intent.getStringExtra("USER_NAME") ?: "User"

        val currencies = listOf("$ (USD)", "€ (EUR)", "£ (GBP)", "Rs (LKR)", "₹ (INR)", "¥ (JPY)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter

        binding.btnNext.setOnClickListener {
            val selected = binding.spinnerCurrency.selectedItem.toString().split(" ")[0]
            val intent = Intent(this, AskAvatarActivity::class.java)
            intent.putExtra("USER_NAME", userName)
            intent.putExtra("USER_CURRENCY", selected)
            startActivity(intent)
        }
    }
}
