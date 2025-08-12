package com.pisco.samacaisseandroid.ui

import ClientManagementScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pisco.samacaisseandroid.AppDbHelper

class ClientManagementActivity : ComponentActivity() {

    private lateinit var dbHelper: AppDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = AppDbHelper(this)

        setContent {
            ClientManagementScreen(dbHelper)
        }
    }
}
