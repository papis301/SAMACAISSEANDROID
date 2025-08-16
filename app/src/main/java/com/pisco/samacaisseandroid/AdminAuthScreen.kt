package com.pisco.samacaisseandroid

import ads_mobile_sdk.h5
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pisco.samacaisseandroid.ui.ClientManagementActivity
import com.pisco.samacaisseandroid.ui.UserManagementScreen
import com.pisco.samacaisseandroid.ui.theme.SamaCaisseAndroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminAuthScreen : ComponentActivity() {
    private lateinit var dbHelper: AppDbHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = AppDbHelper(this)
        enableEdgeToEdge()
        setContent {
            SamaCaisseAndroidTheme {

                var loggedIn by remember { mutableStateOf(false) }
                if (!loggedIn) {
                    AdminAuthScreen(dbHelper = dbHelper) {
                        loggedIn = true
                    }
                } else {
                    // Écran principal après connexion admin
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Bienvenue Admin !", style = MaterialTheme.typography.titleLarge)

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                // Navigation ou lancement de la gestion utilisateurs
                                startActivity(
                                    Intent(this@AdminAuthScreen, UserManagementActivity::class.java)
                                )
                            }
                        ) {
                            Text("Gérer les utilisateurs")
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                // Navigation ou lancement de la gestion utilisateurs
                                startActivity(
                                    Intent(this@AdminAuthScreen, ProductManagementActivity::class.java)
                                )
                            }
                        ) {
                            Text("Gérer les produits")
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(onClick = {
                            startActivity(Intent(this@AdminAuthScreen, ClientManagementActivity::class.java))
                        }) {
                            Text("Gérer les clients")
                        }
                    }

                    // Ou lancer le dashboard admin
                }

            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SamaCaisseAndroidTheme {
        Greeting("Android")
    }
}

@Composable
fun AdminAuthScreen(
    dbHelper: AppDbHelper,
    onAdminLoggedIn: () -> Unit
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

    if (loading) {
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
            text = if (adminExists == true) "Connexion Admin" else "Créer un compte Admin",
            style = MaterialTheme.typography.bodyLarge
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
                        // Connexion admin
                        val valid = withContext(Dispatchers.IO) {
                            dbHelper.checkAdminCredentials(username.trim(), password)
                        }
                        if (valid) {
                            onAdminLoggedIn()
                        } else {
                            errorMessage = "Identifiants invalides"
                        }
                    } else {
                        // Création admin
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
                            errorMessage = "Erreur lors de la création, nom d'utilisateur peut-être déjà pris"
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