package com.mobileappliction_bugetmanegmentapp.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.mobileappliction_bugetmanegmentapp.R
import com.mobileappliction_bugetmanegmentapp.databinding.FragmentNameSetupBinding
import com.mobileappliction_bugetmanegmentapp.viewmodel.SetupWizardViewModel

class NameSetupFragment : Fragment() {

    private var _binding: FragmentNameSetupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SetupWizardViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNameSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etName.setText(viewModel.name.value)
        updateNextButtonState(viewModel.name.value)

        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val name = s.toString()
                viewModel.setName(name)
                updateNextButtonState(name)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnNext.setOnClickListener {
            activity?.findViewById<ViewPager2>(R.id.viewPager)?.currentItem = 2
        }
    }

    private fun updateNextButtonState(name: String?) {
        val isValid = !name.isNullOrBlank()
        binding.btnNext.isEnabled = isValid
        val color = if (isValid) android.graphics.Color.parseColor("#2196F3") else android.graphics.Color.GRAY
        binding.btnNext.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
