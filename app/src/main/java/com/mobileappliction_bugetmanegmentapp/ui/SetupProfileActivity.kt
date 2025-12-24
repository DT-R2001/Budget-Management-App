package com.mobileappliction_bugetmanegmentapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mobileappliction_bugetmanegmentapp.databinding.ActivitySetupProfileBinding

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = SetupPagerAdapter(this)
        binding.viewPager.adapter = adapter
        // User can swipe back/forth
        binding.viewPager.isUserInputEnabled = true
    }

    override fun onBackPressed() {
        if (binding.viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            binding.viewPager.currentItem = binding.viewPager.currentItem - 1
        }
    }

    private inner class SetupPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 4 // Welcome, Name, Currency, Avatar

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> WelcomeFragment()
                1 -> NameSetupFragment()
                2 -> CurrencySetupFragment()
                3 -> AvatarSetupFragment()
                else -> WelcomeFragment()
            }
        }
    }
}
