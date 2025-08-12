package com.pisco.samacaisseandroid.ui

import ProductManagementScreen
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.pisco.samacaisseandroid.AppDbHelper

class ProductManagementActivity : ComponentActivity() {
    private lateinit var dbHelper: AppDbHelper

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = AppDbHelper(this)

        setContent {
            ProductManagementScreen(dbHelper = dbHelper)
        }
    }
}
