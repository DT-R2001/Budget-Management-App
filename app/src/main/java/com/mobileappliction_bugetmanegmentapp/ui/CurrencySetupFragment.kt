package com.mobileappliction_bugetmanegmentapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.mobileappliction_bugetmanegmentapp.R
import com.mobileappliction_bugetmanegmentapp.databinding.FragmentCurrencySetupBinding
import com.mobileappliction_bugetmanegmentapp.viewmodel.SetupWizardViewModel

class CurrencySetupFragment : Fragment() {

    private var _binding: FragmentCurrencySetupBinding? = null
    private val binding get() = _binding!!
    private val setupViewModel: SetupWizardViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCurrencySetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currencies = listOf("$ (USD)", "€ (EUR)", "£ (GBP)", "Rs (LKR)", "₹ (INR)", "¥ (JPY)")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter

        binding.btnNext.setOnClickListener {
            val selected = binding.spinnerCurrency.selectedItem.toString().split(" ")[0] // Extract symbol
            setupViewModel.setCurrency(selected)
            
            // Navigate to next
            val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
            viewPager.currentItem = viewPager.currentItem + 1
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
