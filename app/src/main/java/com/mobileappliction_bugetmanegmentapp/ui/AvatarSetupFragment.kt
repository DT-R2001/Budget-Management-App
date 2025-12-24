package com.mobileappliction_bugetmanegmentapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobileappliction_bugetmanegmentapp.MainActivity
import com.mobileappliction_bugetmanegmentapp.R
import com.mobileappliction_bugetmanegmentapp.data.User
import com.mobileappliction_bugetmanegmentapp.databinding.FragmentAvatarSetupBinding
import com.mobileappliction_bugetmanegmentapp.viewmodel.BudgetViewModel
import com.mobileappliction_bugetmanegmentapp.viewmodel.SetupWizardViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class AvatarSetupFragment : Fragment() {

    private var _binding: FragmentAvatarSetupBinding? = null
    private val binding get() = _binding!!
    private val setupViewModel: SetupWizardViewModel by activityViewModels()
    private lateinit var budgetViewModel: BudgetViewModel

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val path = copyImageToInternalStorage(it)
            if (path != null) {
                setupViewModel.setAvatar(path, true)
                binding.ivAvatar.setImageURI(Uri.fromFile(File(path)))
            } else {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAvatarSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        budgetViewModel = ViewModelProvider(this)[BudgetViewModel::class.java]

        setupViewModel.name.observe(viewLifecycleOwner) { name ->
            binding.tvGreeting.text = "Hello, $name!"
        }

        // Observe Avatar Selection for Button State
        setupViewModel.avatarPath.observe(viewLifecycleOwner) { path ->
            updateFinishButtonState(path)
        }

        setupAvatarList()

        binding.btnUpload.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnFinish.setOnClickListener {
            saveAndFinish()
        }
    }

    private fun updateFinishButtonState(path: String?) {
        val isValid = !path.isNullOrEmpty()
        binding.btnFinish.isEnabled = isValid
        val color = if (isValid) android.graphics.Color.parseColor("#4CAF50") else android.graphics.Color.GRAY
        binding.btnFinish.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
    }

    private fun setupAvatarList() {
        val avatars = loadAvatarsFromDrawable()
        val adapter = AvatarAdapter(avatars) { selected ->
            setupViewModel.setAvatar(selected.resourceId.toString(), false)
            binding.ivAvatar.setImageResource(selected.resourceId)
        }
        binding.rvAvatarList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvAvatarList.adapter = adapter
    }

    private fun saveAndFinish() {
        val name = setupViewModel.name.value ?: "User"
        val avatar = setupViewModel.avatarPath.value ?: ""
        val isCustom = setupViewModel.isCustomAvatar.value ?: false
        val currency = setupViewModel.currency.value ?: "$"

        val user = User(
            name = name,
            avatarPath = avatar,
            isCustomAvatar = isCustom,
            currency = currency
        )
        budgetViewModel.saveUser(user)

        startActivity(Intent(activity, MainActivity::class.java))
        activity?.finish()
    }

    private fun loadAvatarsFromDrawable(): List<AvatarItem> {
        val avatarList = mutableListOf<AvatarItem>()
        val fields = R.drawable::class.java.fields

        for (field in fields) {
            if (field.name.startsWith("avatar_")) {
                try {
                    val id = field.getInt(null)
                    val nameRaw = field.name.removePrefix("avatar_")
                    val profession = nameRaw.split("_")
                        .joinToString(" ") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() } }
                    
                    avatarList.add(AvatarItem(id, profession))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return avatarList
    }

    private fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val fileName = "avatar_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
