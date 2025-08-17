package com.pisco.samacaisseandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pisco.samacaisseandroid.AppDbHelper

class UserHistoryActivity : ComponentActivity() {

    private lateinit var dbHelper: AppDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = AppDbHelper(this)

        setContent {
            MaterialTheme {
                UserHistoryScreen(dbHelper)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHistoryScreen(dbHelper: AppDbHelper) {
    var sessions by remember { mutableStateOf(listOf<UserSession>()) }

    // Charger les sessions depuis SQLite
    LaunchedEffect(Unit) {
        sessions = loadUserSessions(dbHelper)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Historique des utilisateurs") })
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(sessions) { session ->
                UserSessionItem(session)
            }
        }
    }
}

@Composable
fun UserSessionItem(session: UserSession) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFF0F0F0))
            .padding(8.dp)
    ) {
        Text(text = session.username, style = MaterialTheme.typography.titleLarge)
        Text(text = "Login : ${session.loginTime}")
        Text(text = "Logout : ${session.logoutTime ?: "En cours"}")
    }
}

fun loadUserSessions(dbHelper: AppDbHelper): List<UserSession> {
    val list = mutableListOf<UserSession>()
    val db = dbHelper.readableDatabase
    val cursor = db.rawQuery(
        "SELECT u.username, s.login_time, s.logout_time " +
                "FROM user_sessions s " +
                "INNER JOIN users u ON s.user_id = u.id " +
                "ORDER BY s.login_time DESC", null
    )

    if (cursor.moveToFirst()) {
        do {
            val username = cursor.getString(0)
            val login = cursor.getString(1)
            val logout = cursor.getString(2)
            list.add(UserSession(username, login, logout))
        } while (cursor.moveToNext())
    }
    cursor.close()
    return list
}



