package com.mobileappliction_bugetmanegmentapp.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobileappliction_bugetmanegmentapp.R
import com.mobileappliction_bugetmanegmentapp.data.User
import com.mobileappliction_bugetmanegmentapp.databinding.ActivityEditProfileBinding
import com.mobileappliction_bugetmanegmentapp.viewmodel.BudgetViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var viewModel: BudgetViewModel
    private var currentAvatarPath: String = ""
    private var isCustomAvatar: Boolean = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val path = copyImageToInternalStorage(it)
            if (path != null) {
                currentAvatarPath = path
                isCustomAvatar = true
                binding.ivAvatar.setImageURI(Uri.fromFile(File(path)))
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[BudgetViewModel::class.java]

        setupObservers()
        setupAvatarList() // Ensure this is called
        viewModel.loadUser() // Load data

        binding.btnUpload.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnUpdate.setOnClickListener {
            saveChanges()
        }
    }

    private fun setupObservers() {
        viewModel.currentUser.observe(this) { user ->
            if (user != null) {
                // Only populate if fields are empty to avoid overwriting user edits while typing
                if (binding.etName.text.isNullOrEmpty()) {
                    binding.etName.setText(user.name)
                    currentAvatarPath = user.avatarPath
                    isCustomAvatar = user.isCustomAvatar
                    loadAvatarImage(user.avatarPath, user.isCustomAvatar)
                }
            }
        }
    }

    private fun loadAvatarImage(path: String, isCustom: Boolean) {
        if (isCustom) {
            if (path.isNotEmpty()) {
                binding.ivAvatar.setImageURI(Uri.fromFile(File(path)))
            }
        } else {
            val resId = path.toIntOrNull()
            if (resId != null) {
                binding.ivAvatar.setImageResource(resId)
            } else {
                binding.ivAvatar.setImageResource(R.mipmap.ic_launcher_round)
            }
        }
    }

    private fun setupAvatarList() {
        val avatars = loadAvatarsFromDrawable()
        val adapter = AvatarAdapter(avatars) { selected ->
            currentAvatarPath = selected.resourceId.toString()
            isCustomAvatar = false
            binding.ivAvatar.setImageResource(selected.resourceId)
        }
        binding.rvAvatarList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvAvatarList.adapter = adapter
    }

    private fun saveChanges() {
        val newName = binding.etName.text.toString()
        if (newName.isBlank()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedUser = User(
            id = 1, // Assuming single user with ID 1
            name = newName,
            avatarPath = currentAvatarPath,
            isCustomAvatar = isCustomAvatar
        )
        viewModel.saveUser(updatedUser)
        finish() // Go back to Dashboard
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
