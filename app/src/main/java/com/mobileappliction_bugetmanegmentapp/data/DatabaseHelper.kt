package com.mobileappliction_bugetmanegmentapp.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "BudgetApp.db"
        private const val DATABASE_VERSION = 3 // Updated for category support

        // Table Names
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val TABLE_USER = "user"
        private const val TABLE_CATEGORIES = "categories"

        // Transaction Columns
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_NOTE = "note"
        private const val COLUMN_CATEGORY_ID = "category_id"

        // User Columns
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_NAME = "name"
        private const val COLUMN_AVATAR_PATH = "avatar_path"
        private const val COLUMN_IS_CUSTOM_AVATAR = "is_custom_avatar"
        private const val COLUMN_CURRENCY = "currency"

        // Category Columns
        private const val COLUMN_CATEGORY_NAME = "name"
        private const val COLUMN_CATEGORY_COLOR = "color"
        private const val COLUMN_CATEGORY_TYPE = "type"
        private const val COLUMN_IS_DEFAULT = "is_default"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create categories table
        val createCategoriesTable = ("CREATE TABLE " + TABLE_CATEGORIES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CATEGORY_NAME + " TEXT UNIQUE,"
                + COLUMN_CATEGORY_COLOR + " TEXT,"
                + COLUMN_CATEGORY_TYPE + " TEXT,"
                + COLUMN_IS_DEFAULT + " INTEGER" + ")")
        db.execSQL(createCategoriesTable)

        // Create transactions table with category foreign key
        val createTransactionsTable = ("CREATE TABLE " + TABLE_TRANSACTIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_AMOUNT + " REAL,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_NOTE + " TEXT,"
                + COLUMN_CATEGORY_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_ID + ")" + ")")
        db.execSQL(createTransactionsTable)

        val createUserTable = ("CREATE TABLE " + TABLE_USER + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_NAME + " TEXT,"
                + COLUMN_AVATAR_PATH + " TEXT,"
                + COLUMN_IS_CUSTOM_AVATAR + " INTEGER,"
                + COLUMN_CURRENCY + " TEXT" + ")") // Boolean stored as 0/1
        db.execSQL(createUserTable)
        
        // Insert default categories
        insertDefaultCategories(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add currency column if upgrading from v1
            try {
                db.execSQL("ALTER TABLE $TABLE_USER ADD COLUMN $COLUMN_CURRENCY TEXT DEFAULT '$'")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        if (oldVersion < 3) {
            // Add category support
            try {
                // Create categories table
                val createCategoriesTable = ("CREATE TABLE " + TABLE_CATEGORIES + "("
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_CATEGORY_NAME + " TEXT UNIQUE,"
                        + COLUMN_CATEGORY_COLOR + " TEXT,"
                        + COLUMN_CATEGORY_TYPE + " TEXT,"
                        + COLUMN_IS_DEFAULT + " INTEGER" + ")")
                db.execSQL(createCategoriesTable)
                
                // Insert default categories
                insertDefaultCategories(db)
                
                // Add category_id column to transactions
                db.execSQL("ALTER TABLE $TABLE_TRANSACTIONS ADD COLUMN $COLUMN_CATEGORY_ID INTEGER")
                
                // Set all existing transactions to Uncategorized
                val uncategorizedId = db.rawQuery(
                    "SELECT $COLUMN_ID FROM $TABLE_CATEGORIES WHERE $COLUMN_CATEGORY_NAME = 'Uncategorized' LIMIT 1",
                    null
                ).use { cursor ->
                    if (cursor.moveToFirst()) cursor.getInt(0) else 0
                }
                
                if (uncategorizedId > 0) {
                    db.execSQL("UPDATE $TABLE_TRANSACTIONS SET $COLUMN_CATEGORY_ID = $uncategorizedId")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun insertDefaultCategories(db: SQLiteDatabase) {
        val defaultCategories = listOf(
            // Income categories
            Triple("Salary", "#4CAF50", "Income"),
            Triple("Business", "#8BC34A", "Income"),
            Triple("Gifts", "#CDDC39", "Income"),
            Triple("Investment", "#009688", "Income"),
            // Expense categories
            Triple("Food", "#FF9800", "Expense"),
            Triple("Transport", "#03A9F4", "Expense"),
            Triple("Shopping", "#E91E63", "Expense"),
            Triple("Bills", "#9C27B0", "Expense"),
            Triple("Entertainment", "#F44336", "Expense"),
            Triple("Health", "#FF5722", "Expense"),
            Triple("Education", "#3F51B5", "Expense"),
            // Universal categories
            Triple("Other", "#9E9E9E", "Both"),
            Triple("Uncategorized", "#757575", "Both")
        )
        
        defaultCategories.forEach { (name, color, type) ->
            val values = ContentValues()
            values.put(COLUMN_CATEGORY_NAME, name)
            values.put(COLUMN_CATEGORY_COLOR, color)
            values.put(COLUMN_CATEGORY_TYPE, type)
            values.put(COLUMN_IS_DEFAULT, 1)
            db.insert(TABLE_CATEGORIES, null, values)
        }
    }

    // Transaction Operations
    fun addTransaction(transaction: Transaction, categoryId: Int? = null): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_TITLE, transaction.title)
        values.put(COLUMN_AMOUNT, transaction.amount)
        values.put(COLUMN_TYPE, transaction.type)
        values.put(COLUMN_DATE, transaction.date)
        values.put(COLUMN_NOTE, transaction.note)
        
        // If categoryId is provided, use it; otherwise find Uncategorized
        val catId = categoryId ?: run {
            val cursor = db.rawQuery(
                "SELECT $COLUMN_ID FROM $TABLE_CATEGORIES WHERE $COLUMN_CATEGORY_NAME = 'Uncategorized' LIMIT 1",
                null
            )
            cursor.use {
                if (it.moveToFirst()) it.getInt(0) else null
            }
        }
        
        if (catId != null) {
            values.put(COLUMN_CATEGORY_ID, catId)
        }

        val id = db.insert(TABLE_TRANSACTIONS, null, values)
        db.close()
        return id
    }

    fun getAllTransactions(): List<Transaction> {
        val transactionList = ArrayList<Transaction>()
        // Join with categories to get category name and color
        val selectQuery = """SELECT t.*, c.$COLUMN_CATEGORY_NAME, c.$COLUMN_CATEGORY_COLOR 
            FROM $TABLE_TRANSACTIONS t 
            LEFT JOIN $TABLE_CATEGORIES c ON t.$COLUMN_CATEGORY_ID = c.$COLUMN_ID 
            ORDER BY t.$COLUMN_ID DESC"""
        val db = this.readableDatabase
        val cursor: Cursor? = db.rawQuery(selectQuery, null)

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                    val title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE))
                    val amount = it.getDouble(it.getColumnIndexOrThrow(COLUMN_AMOUNT))
                    val type = it.getString(it.getColumnIndexOrThrow(COLUMN_TYPE))
                    val date = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE))
                    val note = it.getString(it.getColumnIndexOrThrow(COLUMN_NOTE))
                    val categoryName = it.getString(it.getColumnIndex(COLUMN_CATEGORY_NAME)) ?: "Uncategorized"
                    val categoryColor = it.getString(it.getColumnIndex(COLUMN_CATEGORY_COLOR)) ?: "#757575"

                    val transaction = Transaction(id, title, amount, type, date, note, categoryName, categoryColor)
                    transactionList.add(transaction)
                } while (it.moveToNext())
            }
        }
        return transactionList
    }

    fun deleteTransaction(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_TRANSACTIONS, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    fun updateAllTransactionAmounts(rate: Double) {
        val db = this.writableDatabase
        // SQL: UPDATE transactions SET amount = amount * rate
        val sql = "UPDATE $TABLE_TRANSACTIONS SET $COLUMN_AMOUNT = $COLUMN_AMOUNT * $rate"
        db.execSQL(sql)
        db.close()
    }


    // User Operations
    fun saveUser(user: User) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USER_NAME, user.name)
        values.put(COLUMN_AVATAR_PATH, user.avatarPath)
        values.put(COLUMN_IS_CUSTOM_AVATAR, if (user.isCustomAvatar) 1 else 0)
        values.put(COLUMN_CURRENCY, user.currency)

        var updatedRows = 0
        
        // 1. Try to update by ID if available
        if (user.id > 0) {
            updatedRows = db.update(TABLE_USER, values, "$COLUMN_USER_ID = ?", arrayOf(user.id.toString()))
        }
        
        // 2. If no update happened (ID=0 or ID not found), check if ANY row exists (Singleton Logic)
        if (updatedRows == 0) {
            val cursor = db.rawQuery("SELECT $COLUMN_USER_ID FROM $TABLE_USER LIMIT 1", null)
            if (cursor.moveToFirst()) {
                val existingId = cursor.getInt(0)
                updatedRows = db.update(TABLE_USER, values, "$COLUMN_USER_ID = ?", arrayOf(existingId.toString()))
            }
            cursor.close()
        }

        // 3. If still no update (Table empty), Insert
        if (updatedRows == 0) {
            db.insert(TABLE_USER, null, values)
        }
        
        db.close()
    }

    fun getUser(): User? {
        val db = this.readableDatabase
        val selectQuery = "SELECT  * FROM $TABLE_USER LIMIT 1"
        val cursor = db.rawQuery(selectQuery, null)
        var user: User? = null

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_USER_ID))
                val name = it.getString(it.getColumnIndexOrThrow(COLUMN_USER_NAME))
                val avatarPath = it.getString(it.getColumnIndexOrThrow(COLUMN_AVATAR_PATH))
                val isCustom = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_CUSTOM_AVATAR)) == 1
                val currency = it.getString(it.getColumnIndex(COLUMN_CURRENCY).takeIf { idx -> idx != -1 } ?: it.getColumnIndexOrThrow(COLUMN_CURRENCY)) ?: "$"
                user = User(id, name, avatarPath, isCustom, currency)
            }
        }
        return user
    }

    // Category Operations
    fun addCategory(category: Category): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_CATEGORY_NAME, category.name)
        values.put(COLUMN_CATEGORY_COLOR, category.color)
        values.put(COLUMN_CATEGORY_TYPE, category.type)
        values.put(COLUMN_IS_DEFAULT, if (category.isDefault) 1 else 0)

        val id = db.insert(TABLE_CATEGORIES, null, values)
        db.close()
        return id
    }

    fun getAllCategories(): List<Category> {
        val categoryList = ArrayList<Category>()
        val selectQuery = "SELECT * FROM $TABLE_CATEGORIES ORDER BY $COLUMN_IS_DEFAULT DESC, $COLUMN_CATEGORY_NAME ASC"
        val db = this.readableDatabase
        val cursor: Cursor? = db.rawQuery(selectQuery, null)

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                    val name = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME))
                    val color = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY_COLOR))
                    val type = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY_TYPE))
                    val isDefault = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_DEFAULT)) == 1

                    val category = Category(id, name, color, type, isDefault)
                    categoryList.add(category)
                } while (it.moveToNext())
            }
        }
        return categoryList
    }

    fun getCategoriesByType(type: String): List<Category> {
        val categoryList = ArrayList<Category>()
        // Get categories that match the type or are "Both"
        val selectQuery = "SELECT * FROM $TABLE_CATEGORIES WHERE $COLUMN_CATEGORY_TYPE = ? OR $COLUMN_CATEGORY_TYPE = 'Both' ORDER BY $COLUMN_IS_DEFAULT DESC, $COLUMN_CATEGORY_NAME ASC"
        val db = this.readableDatabase
        val cursor: Cursor? = db.rawQuery(selectQuery, arrayOf(type))

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                    val name = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME))
                    val color = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY_COLOR))
                    val catType = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY_TYPE))
                    val isDefault = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_DEFAULT)) == 1

                    val category = Category(id, name, color, catType, isDefault)
                    categoryList.add(category)
                } while (it.moveToNext())
            }
        }
        return categoryList
    }

    fun updateCategoryColor(id: Int, color: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_CATEGORY_COLOR, color)
        db.update(TABLE_CATEGORIES, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteCategory(id: Int): Boolean {
        val db = this.writableDatabase
        // Check if it's a default category
        val cursor = db.rawQuery(
            "SELECT $COLUMN_IS_DEFAULT FROM $TABLE_CATEGORIES WHERE $COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        
        val isDefault = cursor.use {
            if (it.moveToFirst()) it.getInt(0) == 1 else true
        }
        
        if (isDefault) {
            db.close()
            return false // Cannot delete default categories
        }
        
        // Set transactions with this category to Uncategorized
        val uncategorizedId = db.rawQuery(
            "SELECT $COLUMN_ID FROM $TABLE_CATEGORIES WHERE $COLUMN_CATEGORY_NAME = 'Uncategorized' LIMIT 1",
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
        
        if (uncategorizedId > 0) {
            db.execSQL("UPDATE $TABLE_TRANSACTIONS SET $COLUMN_CATEGORY_ID = $uncategorizedId WHERE $COLUMN_CATEGORY_ID = $id")
        }
        
        db.delete(TABLE_CATEGORIES, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return true
    }
}
