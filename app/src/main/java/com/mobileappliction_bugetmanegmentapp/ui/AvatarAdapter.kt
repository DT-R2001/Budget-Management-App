package com.mobileappliction_bugetmanegmentapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobileappliction_bugetmanegmentapp.databinding.ItemAvatarSelectionBinding

data class AvatarItem(val resourceId: Int, val profession: String)

class AvatarAdapter(
    private val avatars: List<AvatarItem>,
    private val onAvatarClick: (AvatarItem) -> Unit
) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

    inner class AvatarViewHolder(private val binding: ItemAvatarSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(avatar: AvatarItem) {
            binding.ivAvatarItem.setImageResource(avatar.resourceId)
            binding.tvProfession.text = avatar.profession
            
            binding.root.setOnClickListener {
                onAvatarClick(avatar)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val binding = ItemAvatarSelectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AvatarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        holder.bind(avatars[position])
    }

    override fun getItemCount() = avatars.size
}
