import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pisco.samacaisseandroid.AppDbHelper
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementScreen(dbHelper: AppDbHelper) {
    val coroutineScope = rememberCoroutineScope()

    var products by remember { mutableStateOf<List<AppDbHelper.Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Champs du formulaire
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf("") }
    var editingProductId by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Charger la liste des produits au lancement
    LaunchedEffect(Unit) {
        products = dbHelper.getAllProductsSuspend()
        isLoading = false
    }

    fun resetForm() {
        name = ""
        price = ""
        quantity = ""
        imageUri = ""
        editingProductId = null
        errorMessage = null
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gestion des Produits") })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(products) { product ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Remplir le formulaire pour édition
                                        editingProductId = product.id
                                        name = product.name
                                        price = product.price.toString()
                                        quantity = product.quantity.toString()
                                        imageUri = product.imageUri ?: ""
                                        errorMessage = null
                                    }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Prix: ${product.price} | Qté: ${product.quantity}")
                                    Text(text = "Ajouté le: ${product.dateAdded}")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = if (editingProductId == null) "Ajouter un produit" else "Modifier le produit",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Prix") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        //keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantité") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                       // keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = imageUri,
                        onValueChange = { imageUri = it },
                        label = { Text("URI de l'image") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                errorMessage = null
                                val p = price.toDoubleOrNull()
                                val q = quantity.toDoubleOrNull()
                                if (name.isBlank() || p == null || q == null) {
                                    errorMessage = "Veuillez remplir correctement tous les champs."
                                    return@Button
                                }
                                val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                coroutineScope.launch {
                                    val success = if (editingProductId == null) {
                                        dbHelper.addProductSuspend(name, p, q, now, if (imageUri.isBlank()) null else imageUri)
                                    } else {
                                        dbHelper.updateProductSuspend(editingProductId!!, name, p, q, now, if (imageUri.isBlank()) null else imageUri)
                                    }
                                    if (success) {
                                        products = dbHelper.getAllProductsSuspend()
                                        resetForm()
                                    } else {
                                        errorMessage = "Erreur lors de l'opération"
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (editingProductId == null) "Ajouter" else "Modifier")
                        }

                        if (editingProductId != null) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val success = dbHelper.deleteProductSuspend(editingProductId!!)
                                        if (success) {
                                            products = dbHelper.getAllProductsSuspend()
                                            resetForm()
                                        } else {
                                            errorMessage = "Erreur suppression"
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Supprimer")
                            }
                        }
                    }
                }
            }
        }
    )
}
