package com.mobileappliction_bugetmanegmentapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobileappliction_bugetmanegmentapp.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnIncomeHistory.setOnClickListener {
            val intent = Intent(this, FilteredHistoryActivity::class.java)
            intent.putExtra("MODE", "Income")
            startActivity(intent)
        }

        binding.btnExpenseHistory.setOnClickListener {
            val intent = Intent(this, FilteredHistoryActivity::class.java)
            intent.putExtra("MODE", "Expense")
            startActivity(intent)
        }

        binding.btnCurrency.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("SETTINGS_MODE", "Currency")
            startActivity(intent)
        }

        binding.btnAppearance.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("SETTINGS_MODE", "Appearance")
            startActivity(intent)
        }

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }
}
