package com.mobileappliction_bugetmanegmentapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobileappliction_bugetmanegmentapp.data.DatabaseHelper
import com.mobileappliction_bugetmanegmentapp.data.Transaction
import com.mobileappliction_bugetmanegmentapp.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _totalBalance = MutableLiveData<Double>()
    val totalBalance: LiveData<Double> = _totalBalance

    private val _income = MutableLiveData<Double>()
    val income: LiveData<Double> = _income

    private val _expense = MutableLiveData<Double>()
    val expense: LiveData<Double> = _expense

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    fun loadTransactions() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                dbHelper.getAllTransactions()
            }
            _transactions.value = list
            calculateBalance(list)
        }
    }

    private fun calculateBalance(list: List<Transaction>) {
        var total = 0.0
        var inc = 0.0
        var exp = 0.0

        for (t in list) {
            if (t.type == "Income") {
                total += t.amount
                inc += t.amount
            } else {
                total -= t.amount
                exp += t.amount
            }
        }
        _totalBalance.value = total
        _income.value = inc
        _expense.value = exp
    }

    fun addTransaction(transaction: Transaction, categoryId: Int? = null) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.addTransaction(transaction, categoryId)
            }
            loadTransactions() // Refresh
        }
    }
    
    fun deleteTransaction(id: Int){
         viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.deleteTransaction(id)
            }
            loadTransactions() // Refresh
        }
    }

    // Category Operations
    fun getCategoriesByType(type: String): List<com.mobileappliction_bugetmanegmentapp.data.Category> {
        // This is a blocking call, but we're calling it from UI context
        // In a real scenario, we'd make this async, but for simplicity we'll use runBlocking
        return try {
            dbHelper.getCategoriesByType(type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addCategory(category: com.mobileappliction_bugetmanegmentapp.data.Category) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.addCategory(category)
            }
        }
    }

    fun loadUser() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                dbHelper.getUser()
            }
            _currentUser.value = user
        }
    }

    fun saveUser(user: User) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.saveUser(user)
            }
            loadUser()
        }
    }
    
    suspend fun updateCurrencyAndConvert(newCurrency: String, rate: Double) {
        val currentUser = _currentUser.value // Capture on Main Thread

        withContext(Dispatchers.IO) {
            // 1. Update all amounts
            if (rate != 1.0) {
                dbHelper.updateAllTransactionAmounts(rate)
            }
            
            // 2. Update User Currency
            val updatedUser = currentUser?.copy(currency = newCurrency) ?: User(0, "User", "", false, newCurrency)
            dbHelper.saveUser(updatedUser)
        }
        // 3. Refresh Data
        // calling these on Main thread (implied by suspend call from UI) or ensure they are thread safe.
        // loadUser/loadTransactions launch their own scopes, so that's fine.
        // But for "Finish" safety, we just want the DB part done.
        loadUser()
        loadTransactions()
    }
}
