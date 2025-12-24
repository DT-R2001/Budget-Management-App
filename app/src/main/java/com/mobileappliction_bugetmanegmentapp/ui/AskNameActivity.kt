package com.mobileappliction_bugetmanegmentapp.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.mobileappliction_bugetmanegmentapp.databinding.ActivityAskNameBinding

class AskNameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAskNameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAskNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNext.isEnabled = false
        binding.btnNext.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY)

        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val name = s.toString()
                val isValid = name.isNotBlank()
                binding.btnNext.isEnabled = isValid
                val color = if (isValid) android.graphics.Color.parseColor("#9C27B0") else android.graphics.Color.GRAY
                binding.btnNext.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnNext.setOnClickListener {
            val name = binding.etName.text.toString()
            val intent = Intent(this, AskCurrencyActivity::class.java)
            intent.putExtra("USER_NAME", name)
            startActivity(intent)
        }
    }
}
