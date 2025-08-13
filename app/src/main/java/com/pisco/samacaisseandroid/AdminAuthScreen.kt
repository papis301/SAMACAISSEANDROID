package com.pisco.samacaisseandroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pisco.samacaisseandroid.ui.ClientManagementActivity
import com.pisco.samacaisseandroid.ProductManagementActivity
import com.pisco.samacaisseandroid.ui.theme.SamaCaisseAndroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminAuthScreen : ComponentActivity() {
    private lateinit var dbHelper: AppDbHelper
    val context = this@AdminAuthScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = AppDbHelper(this)
        enableEdgeToEdge()

        setContent {
            SamaCaisseAndroidTheme {
                var loggedInAdmin by remember { mutableStateOf(false) }
                var loggedInUser by remember { mutableStateOf(false) }

                when {
                    loggedInAdmin -> AdminDashboardScreen(
                        onManageUsers = {
                            context.startActivity(Intent(context, UserManagementActivity::class.java))
                        },
                        onManageProducts = {
                            context.startActivity(Intent(context, ProductManagementActivity::class.java))
                        },
                        onManageClients = {
                            context.startActivity(Intent(context, ClientManagementActivity::class.java))
                        },
                        onLogout = {
                            loggedInAdmin = false
                            loggedInUser = false
                        }
                    )
                    loggedInUser -> UserDashboardScreen(
                        onLogout = {
                            loggedInAdmin = false
                            loggedInUser = false
                        }
                    )
                    else -> LoginOrCreateAdminScreen(
                        dbHelper = dbHelper,
                        onAdminLoggedIn = { loggedInAdmin = true },
                        onUserLoggedIn = { loggedInUser = true }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginOrCreateAdminScreen(
    dbHelper: AppDbHelper,
    onAdminLoggedIn: () -> Unit,
    onUserLoggedIn: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var adminExists by remember { mutableStateOf<Boolean?>(null) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        adminExists = withContext(Dispatchers.IO) { dbHelper.isAdminExists() }
        loading = false
    }

    if (loading || adminExists == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (adminExists == true) "Connexion" else "Créer un compte Admin",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nom d'utilisateur") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (adminExists == false) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = passwordConfirm,
                onValueChange = { passwordConfirm = it },
                label = { Text("Confirmer mot de passe") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
        }

        Button(
            onClick = {
                errorMessage = null
                coroutineScope.launch {
                    if (adminExists == true) {
                        val user = withContext(Dispatchers.IO) {
                            dbHelper.getUserByUsernameAndPassword(username.trim(), password)
                        }
                        if (user == null) {
                            errorMessage = "Identifiants invalides"
                        } else {
                            when (user.role) {
                                "admin" -> onAdminLoggedIn()
                                else -> onUserLoggedIn()
                            }
                        }
                    } else {
                        if (username.trim().isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
                            errorMessage = "Tous les champs sont obligatoires"
                            return@launch
                        }
                        if (password != passwordConfirm) {
                            errorMessage = "Les mots de passe ne correspondent pas"
                            return@launch
                        }
                        val created = withContext(Dispatchers.IO) {
                            dbHelper.createAdmin(username.trim(), password)
                        }
                        if (created) {
                            adminExists = true
                            username = ""
                            password = ""
                            passwordConfirm = ""
                        } else {
                            errorMessage = "Erreur lors de la création"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (adminExists == true) "Se connecter" else "Créer Admin")
        }
    }
}

@Composable
fun AdminDashboardScreen(
    onManageUsers: () -> Unit,
    onManageProducts: () -> Unit,
    onManageClients: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dashboard Admin", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(24.dp))

        Button(onClick = onManageUsers, modifier = Modifier.fillMaxWidth()) {
            Text("Gérer les utilisateurs")
        }

        Button(onClick = onManageProducts, modifier = Modifier.fillMaxWidth()) {
            Text("Gérer les produits")
        }

        Button(onClick = onManageClients, modifier = Modifier.fillMaxWidth()) {
            Text("Gérer les clients")
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Se déconnecter", color = Color.White)
        }
    }
}

@Composable
fun UserDashboardScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dashboard Utilisateur", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onLogout) {
            Text("Se déconnecter")
        }
    }
}
