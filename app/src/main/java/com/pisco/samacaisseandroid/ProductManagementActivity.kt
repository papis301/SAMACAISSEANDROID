package com.pisco.samacaisseandroid

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pisco.samacaisseandroid.Productkotlin
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class ProductManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = AppDbHelper(this)

        setContent {
            ProductManagementScreen(dbHelper)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementScreen(dbHelper: AppDbHelper) {
    val coroutineScope = rememberCoroutineScope()
    var products by remember { mutableStateOf<List<Productkotlin>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Champs du formulaire
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("kg") }
    var expanded by remember { mutableStateOf(false) }
    var editingProductId by remember { mutableStateOf<Int?>(null) }

    // Erreurs
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }

    val units = listOf("kg", "litre", "mètre", "pièce")

    // Charger produits au démarrage
    LaunchedEffect(Unit) {
       // products = dbHelper.getAllProducts()
        isLoading = false
    }

    fun resetForm() {
        name = ""
        price = ""
        quantity = ""
        selectedUnit = units.first()
        editingProductId = null
        nameError = null
        priceError = null
        quantityError = null
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Gestion des Produits") }) },
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
                    // Liste des produits
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(products) { product ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Préremplir formulaire pour édition
                                        editingProductId = product.id
                                        name = product.name
                                        price = product.price.toString()
                                        quantity = product.quantity.toString()
                                        selectedUnit = product.unit
                                    }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(product.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Prix: ${product.price} | Qté: ${product.quantity} ${product.unit}")
                                    Text("Ajouté le: ${product.dateAdded}")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = if (editingProductId == null) "Ajouter un produit" else "Modifier le produit",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(8.dp))

                    // Nom
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; nameError = null },
                        label = { Text("Nom") },
                        isError = nameError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))

                    // Prix
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it; priceError = null },
                        label = { Text("Prix") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = priceError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    priceError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))

                    // Quantité
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it; quantityError = null },
                        label = { Text("Quantité") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = quantityError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    quantityError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))

                    // Unité Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedUnit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unité") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        selectedUnit = unit
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Boutons Ajouter/Modifier/Supprimer
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                // Validation
                                var valid = true
                                if (name.isBlank()) { nameError = "Nom obligatoire"; valid = false }
                                if (price.toDoubleOrNull() == null) { priceError = "Prix invalide"; valid = false }
                                if (quantity.toDoubleOrNull() == null) { quantityError = "Quantité invalide"; valid = false }

                                if (valid) {
                                    val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                    val product = Productkotlin(
                                        id = editingProductId ?: 0,
                                        name = name,
                                        price = price.toDouble(),
                                        quantity = quantity.toDouble(),
                                        unit = selectedUnit,
                                        imageUri = null,
                                        dateAdded = now
                                    )

//                                    coroutineScope.launch {
//                                        val success = if (editingProductId == null) {
//                                            //dbHelper.addProduct(product)
//                                        } else {
//                                            //dbHelper.updateProduct(product)
//                                        }
//                                        if (success) {
//                                           // products = dbHelper.getAllProducts()
//                                            resetForm()
//                                        }
//                                    }
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
                                        dbHelper.deleteProduct(editingProductId!!)
                                       // products = dbHelper.getAllProducts()
                                        resetForm()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) { Text("Supprimer") }
                        }
                    }
                }
            }
        }
    )
}
