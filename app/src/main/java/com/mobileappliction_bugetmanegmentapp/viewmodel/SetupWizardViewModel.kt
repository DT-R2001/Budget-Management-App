package com.mobileappliction_bugetmanegmentapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobileappliction_bugetmanegmentapp.data.User

class SetupWizardViewModel : ViewModel() {

    private val _name = MutableLiveData<String>("")
    val name: LiveData<String> = _name

    private val _avatarPath = MutableLiveData<String>("")
    val avatarPath: LiveData<String> = _avatarPath

    private val _isCustomAvatar = MutableLiveData<Boolean>(false)
    val isCustomAvatar: LiveData<Boolean> = _isCustomAvatar

    private val _currency = MutableLiveData<String>("$")
    val currency: LiveData<String> = _currency

    fun setName(newName: String) {
        _name.value = newName
    }

    fun setAvatar(path: String, isCustom: Boolean) {
        _avatarPath.value = path
        _isCustomAvatar.value = isCustom
    }
    
    fun setCurrency(newCurrency: String) {
        _currency.value = newCurrency
    }
}
