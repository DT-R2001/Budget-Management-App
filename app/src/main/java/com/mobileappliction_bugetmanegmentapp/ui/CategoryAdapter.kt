package com.mobileappliction_bugetmanegmentapp.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mobileappliction_bugetmanegmentapp.R
import com.mobileappliction_bugetmanegmentapp.data.Category

class CategoryAdapter(
    context: Context,
    private val categories: List<Category>
) : ArrayAdapter<Category>(context, 0, categories) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_category,
            parent,
            false
        )

        val category = getItem(position)
        val colorIndicator = view.findViewById<View>(R.id.viewColorIndicator)
        val categoryName = view.findViewById<TextView>(R.id.tvCategoryName)

        category?.let {
            categoryName.text = it.name
            
            // Set color indicator
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            try {
                drawable.setColor(Color.parseColor(it.color))
            } catch (e: Exception) {
                drawable.setColor(Color.parseColor("#757575"))
            }
            colorIndicator.background = drawable
        }

        return view
    }
}
