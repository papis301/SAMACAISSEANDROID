package com.pisco.samacaisseandroid.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pisco.samacaisseandroid.AppDbHelper
import com.pisco.samacaisseandroid.User
import kotlinx.coroutines.launch

@Composable
fun UserManagementScreen() {
    val context = LocalContext.current
    val dbHelper = remember { AppDbHelper(context) }
    val scope = rememberCoroutineScope()

    var users by remember { mutableStateOf(listOf<User>()) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("user") }

    var editUser: User? by remember { mutableStateOf(null) }
    var showDialog by remember { mutableStateOf(false) }

    // Charger la liste au démarrage
    LaunchedEffect(Unit) {
        users = dbHelper.getAllUsersSuspend()
    }

    fun refreshUsers() {
        scope.launch {
            users = dbHelper.getAllUsersSuspend()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gestion des utilisateurs",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            editUser = null
            username = ""
            password = ""
            role = "user"
            showDialog = true
        }) {
            Text("Ajouter un utilisateur")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(users) { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(user.username, style = MaterialTheme.typography.bodyLarge)
                            Text("Rôle : ${user.role}", style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            TextButton(onClick = {
                                editUser = user
                                username = user.username
                                password = ""
                                role = user.role
                                showDialog = true
                            }) {
                                Text("Modifier")
                            }
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        dbHelper.deleteUserSuspend(user.id)
                                        refreshUsers()
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Supprimer")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val success = if (editUser == null) {
                            dbHelper.addUserSuspend(username, password, role)
                        } else {
                            dbHelper.updateUserSuspend(editUser!!.id, username, if (password.isBlank()) null else password, role)
                        }
                        if (success) {
                            refreshUsers()
                            showDialog = false
                        }
                    }
                }) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Annuler")
                }
            },
            title = {
                Text(if (editUser == null) "Ajouter un utilisateur" else "Modifier un utilisateur")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Nom d'utilisateur") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mot de passe") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Rôle") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}
