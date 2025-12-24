package com.mobileappliction_bugetmanegmentapp.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mobileappliction_bugetmanegmentapp.data.Category
import com.mobileappliction_bugetmanegmentapp.data.Transaction
import com.mobileappliction_bugetmanegmentapp.databinding.ActivityAddTransactionBinding
import com.mobileappliction_bugetmanegmentapp.databinding.DialogAddCategoryBinding
import com.mobileappliction_bugetmanegmentapp.viewmodel.BudgetViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var viewModel: BudgetViewModel
    private var categories = listOf<Category>()
    private var selectedCategory: Category? = null
    private var selectedColor = "#9E9E9E"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[BudgetViewModel::class.java]

        setupTransactionTypeListener()
        loadCategories()
        
        binding.tvAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun setupTransactionTypeListener() {
        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            val type = if (binding.rbIncome.isChecked) "Income" else "Expense"
            loadCategories(type)
        }
    }

    private fun loadCategories(type: String? = null) {
        val transactionType = type ?: if (binding.rbIncome.isChecked) "Income" else "Expense"
        categories = viewModel.getCategoriesByType(transactionType)
        
        val adapter = CategoryAdapter(this, categories)
        binding.spinnerCategory.adapter = adapter
        
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory = null
            }
        }
        
        // Select first category by default
        if (categories.isNotEmpty()) {
            selectedCategory = categories[0]
        }
    }

    private fun showAddCategoryDialog() {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // Set up color palette
        val colors = listOf(
            "#4CAF50", "#8BC34A", "#CDDC39", "#FF9800",
            "#F44336", "#E91E63", "#9C27B0", "#3F51B5",
            "#03A9F4", "#009688", "#FF5722", "#9E9E9E"
        )

        selectedColor = colors[0]
        setupColorPalette(dialogBinding, colors)
        updateColorPreview(dialogBinding.viewSelectedColor, selectedColor)

        dialogBinding.root.findViewById<View>(android.R.id.button1)?.setOnClickListener {
            dialog.dismiss()
        }

        // Add Save and Cancel buttons programmatically
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save") { _, _ ->
            val categoryName = dialogBinding.etCategoryName.text.toString()
            if (categoryName.isBlank()) {
                Toast.makeText(this, "Category name required", Toast.LENGTH_SHORT).show()
                return@setButton
            }

            val categoryType = if (dialogBinding.rbIncome.isChecked) "Income" else "Expense"
            val newCategory = Category(
                name = categoryName,
                color = selectedColor,
                type = categoryType,
                isDefault = false
            )

            viewModel.addCategory(newCategory)
            Toast.makeText(this, "Category added!", Toast.LENGTH_SHORT).show()
            
            // Reload categories
            loadCategories()
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { _, _ ->
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupColorPalette(dialogBinding: DialogAddCategoryBinding, colors: List<String>) {
        val gridLayout = dialogBinding.colorPalette
        gridLayout.removeAllViews()

        colors.forEach { color ->
            val colorView = View(this)
            val size = (40 * resources.displayMetrics.density).toInt()
            val margin = (8 * resources.displayMetrics.density).toInt()
            
            val params = GridLayout.LayoutParams()
            params.width = size
            params.height = size
            params.setMargins(margin, margin, margin, margin)
            colorView.layoutParams = params

            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(Color.parseColor(color))
            drawable.setStroke(4, Color.LTGRAY)
            colorView.background = drawable

            colorView.setOnClickListener {
                selectedColor = color
                updateColorPreview(dialogBinding.viewSelectedColor, color)
                
                // Update all color views to show selection
                for (i in 0 until gridLayout.childCount) {
                    val child = gridLayout.getChildAt(i)
                    val childDrawable = (child.background as? GradientDrawable) ?: continue
                    if (i == colors.indexOf(color)) {
                        childDrawable.setStroke(6, Color.BLACK)
                    } else {
                        childDrawable.setStroke(4, Color.LTGRAY)
                    }
                }
            }

            gridLayout.addView(colorView)
        }
    }

    private fun updateColorPreview(view: View, color: String) {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(Color.parseColor(color))
        view.background = drawable
    }

    private fun saveTransaction() {
        val title = binding.etTitle.text.toString()
        val amountStr = binding.etAmount.text.toString()
        val note = binding.etNote.text.toString()
        
        binding.tilTitle.error = null
        binding.tilAmount.error = null
        
        if (title.isBlank()) {
            binding.tilTitle.error = "Title required"
            return
        }
        if (amountStr.isBlank()) {
            binding.tilAmount.error = "Amount required"
            return
        }

        val amount = try {
            amountStr.toDouble()
        } catch (e: NumberFormatException) {
            binding.tilAmount.error = "Invalid amount"
            return
        }

        if (selectedCategory == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val type = if (binding.rbIncome.isChecked) "Income" else "Expense"
        val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())

        val transaction = Transaction(
            title = title,
            amount = amount,
            type = type,
            date = date,
            note = note,
            category = selectedCategory!!.name,
            categoryColor = selectedCategory!!.color
        )

        viewModel.addTransaction(transaction, selectedCategory!!.id)
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
