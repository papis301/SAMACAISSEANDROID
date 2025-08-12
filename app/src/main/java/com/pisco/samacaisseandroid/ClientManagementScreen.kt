import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pisco.samacaisseandroid.AppDbHelper
import com.pisco.samacaisseandroid.Client
import kotlinx.coroutines.launch

@Composable
fun ClientManagementScreen(dbHelper: AppDbHelper) {
    val scope = rememberCoroutineScope()

    var clients by remember { mutableStateOf<List<Client>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var editingClient: Client? by remember { mutableStateOf(null) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        clients = dbHelper.getAllClientsSuspend()
    }

    fun refreshClients() {
        scope.launch {
            clients = dbHelper.getAllClientsSuspend()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Gestion des clients", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            editingClient = null
            name = ""
            phone = ""
            showDialog = true
        }) {
            Text("Ajouter un client")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(clients) { client ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(client.name, style = MaterialTheme.typography.bodyLarge)
                            Text(client.phone ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            TextButton(onClick = {
                                editingClient = client
                                name = client.name
                                phone = client.phone ?: ""
                                showDialog = true
                            }) {
                                Text("Modifier")
                            }
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        dbHelper.deleteClientSuspend(client.id)
                                        refreshClients()
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
                        val success = if (editingClient == null) {
                            dbHelper.addClientSuspend(name.trim(), phone.trim().ifEmpty { null })
                        } else {
                            dbHelper.updateClientSuspend(editingClient!!.id, name.trim(), phone.trim().ifEmpty { null })
                        }
                        if (success) {
                            refreshClients()
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
            title = { Text(if (editingClient == null) "Ajouter un client" else "Modifier un client") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Téléphone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}
