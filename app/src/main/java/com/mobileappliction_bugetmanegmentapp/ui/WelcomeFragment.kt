package com.mobileappliction_bugetmanegmentapp.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.mobileappliction_bugetmanegmentapp.R
import com.mobileappliction_bugetmanegmentapp.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pulse Animation
        val scaleX = ObjectAnimator.ofFloat(binding.ivAppIcon, "scaleX", 0.9f, 1.1f)
        val scaleY = ObjectAnimator.ofFloat(binding.ivAppIcon, "scaleY", 0.9f, 1.1f)
        
        scaleX.duration = 1000
        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleX.repeatMode = ValueAnimator.REVERSE
        
        scaleY.duration = 1000
        scaleY.repeatCount = ValueAnimator.INFINITE
        scaleY.repeatMode = ValueAnimator.REVERSE

        scaleX.start()
        scaleY.start()

        binding.btnLetsBegin.setOnClickListener {
            // Move to next page
            activity?.findViewById<ViewPager2>(R.id.viewPager)?.currentItem = 1
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
