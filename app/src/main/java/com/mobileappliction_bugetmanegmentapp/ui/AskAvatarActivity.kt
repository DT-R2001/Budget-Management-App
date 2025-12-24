package com.mobileappliction_bugetmanegmentapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobileappliction_bugetmanegmentapp.MainActivity
import com.mobileappliction_bugetmanegmentapp.R
import com.mobileappliction_bugetmanegmentapp.data.User
import com.mobileappliction_bugetmanegmentapp.databinding.ActivityAskAvatarBinding
import com.mobileappliction_bugetmanegmentapp.viewmodel.BudgetViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class AskAvatarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAskAvatarBinding
    private lateinit var viewModel: BudgetViewModel
    private lateinit var userName: String
    private lateinit var userCurrency: String
    
    // State
    private var selectedAvatarPath: String = ""
    private var isCustomAvatar: Boolean = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val path = copyImageToInternalStorage(it)
            if (path != null) {
                selectedAvatarPath = path
                isCustomAvatar = true
                binding.ivAvatar.setImageURI(Uri.fromFile(File(path)))
                updateFinishButtonState(true)
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAskAvatarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[BudgetViewModel::class.java]
        
        userName = intent.getStringExtra("USER_NAME") ?: "User"
        userCurrency = intent.getStringExtra("USER_CURRENCY") ?: "$"
        
        binding.tvGreeting.text = "Hello, $userName!"
        
        updateFinishButtonState(false)

        setupAvatarList()

        binding.btnUpload.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnFinish.setOnClickListener {
            saveAndFinish()
        }
    }

    private fun updateFinishButtonState(isValid: Boolean) {
        binding.btnFinish.isEnabled = isValid
        val color = if (isValid) android.graphics.Color.parseColor("#4CAF50") else android.graphics.Color.GRAY
        binding.btnFinish.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
    }

    private fun setupAvatarList() {
        val avatars = loadAvatarsFromDrawable()
        val adapter = AvatarAdapter(avatars) { selected ->
            selectedAvatarPath = selected.resourceId.toString()
            isCustomAvatar = false
            binding.ivAvatar.setImageResource(selected.resourceId)
            updateFinishButtonState(true)
        }
        binding.rvAvatarList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvAvatarList.adapter = adapter
    }

    private fun saveAndFinish() {
        if (selectedAvatarPath.isEmpty()) {
            Toast.makeText(this, "Please select an avatar", Toast.LENGTH_SHORT).show()
            return
        }

        val user = User(
            name = userName,
            avatarPath = selectedAvatarPath,
            isCustomAvatar = isCustomAvatar,
            currency = userCurrency
        )
        viewModel.saveUser(user)

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
    
    // Helper to find raw drawables dynamically or static list
    private fun loadAvatarsFromDrawable(): List<AvatarItem> {
        val avatarList = mutableListOf<AvatarItem>()
        // Assuming R.drawable.avatar_* exists, otherwise fallback to nice defaults or R.mipmap.ic_launcher_round logic
        // We will reuse the logic from previous fragment or just use launcher for now if reflection fails
        // To be safe, let's use a safe list if we don't know the exact drawables present
        // But since we are allowed to use reflection as seen in previous code:
        val fields = R.drawable::class.java.fields
        for (field in fields) {
            if (field.name.startsWith("avatar_")) {
                try {
                    val id = field.getInt(null)
                    val nameRaw = field.name.removePrefix("avatar_")
                    avatarList.add(AvatarItem(id, nameRaw))
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
        // If empty, add default
        if (avatarList.isEmpty()) {
            avatarList.add(AvatarItem(R.mipmap.ic_launcher_round, "Default"))
        }
        return avatarList
    }

    private fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val fileName = "avatar_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
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
}
