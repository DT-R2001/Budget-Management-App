package com.mobileappliction_bugetmanegmentapp.data

data class User(
    val id: Int = 0,
    val name: String,
    val avatarPath: String,
    val isCustomAvatar: Boolean,
    val currency: String = "$" 
)
