package com.pisco.samacaisseandroid

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.pisco.samacaisseandroid.ui.ClientManagementActivity
import com.pisco.samacaisseandroid.ProductManagementActivity
import com.pisco.samacaisseandroid.ui.theme.SamaCaisseAndroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.pisco.samacaisseandroid.CaisseScreen


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
                var products by remember { mutableStateOf(listOf<Product>()) }
                var cart by remember { mutableStateOf(mutableListOf<CartItem>()) }

                LaunchedEffect(Unit) {
                    products = withContext(Dispatchers.IO) { dbHelper.getAllProducts() }
                }

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
//
                    loggedInUser -> CaisseScreen(
                        products = products,
                        dbHelper = dbHelper,
                        onLogout = {
                            loggedInAdmin = false
                            loggedInUser = false
                            cart.clear()
                        },
                        cart = cart,
                        onCartUpdated = { updatedCart ->
                            cart = updatedCart.toMutableList()
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
fun CaisseScreen(
    products: List<Product>,
    dbHelper: AppDbHelper,
    onLogout: () -> Unit,
    cart: List<CartItem>,
    onCartUpdated: (List<CartItem>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantityInput by remember { mutableStateOf("1") }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Row(Modifier.fillMaxSize()) {
        // Partie gauche : produits + recherche
        Column(
            Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            SearchBar(searchQuery) { query -> searchQuery = query }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts) { product ->
                    ProductCard(product) {
                        selectedProduct = product
                        quantityInput = "" // valeur par défaut
                    }
                }
            }
        }

        // Partie droite : panier (tablette)
        if (isTablet()) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFFF5F5F5))
                    .padding(8.dp)
            ) {
                CartSection(cart) { updatedCart ->
                    onCartUpdated(updatedCart)
                }
            }
        }
    }

    // Panier mobile (en bas)
    if (!isTablet()) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(8.dp)
        ) {
            CartSection(cart) { updatedCart ->
                onCartUpdated(updatedCart)
            }
        }
    }

    if (selectedProduct != null) {
        AlertDialog(
            onDismissRequest = { selectedProduct = null },
            title = { Text("Quantité pour ${selectedProduct!!.name}") },
            text = {
                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.isNotEmpty()) {
                            quantityInput = newValue
                        }
                    },
                    label = { Text("Quantité") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(onClick = {
                    val qty = quantityInput.toIntOrNull() ?: 1
                    val existing = cart.find { it.product.id == selectedProduct!!.id }
                    val updatedCart = if (existing != null) {
                        cart.map {
                            if (it.product.id == selectedProduct!!.id)
                                it.copy(quantity = qty)
                            else it
                        }
                    } else {
                        cart + CartItem(selectedProduct!!, qty)
                    }
                    onCartUpdated(updatedCart)
                    selectedProduct = null
                }) {
                    Text("Valider")
                }
            },
            dismissButton = {
                Button(onClick = { selectedProduct = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}



@Composable
fun ProductCard(product: Product, onAdd: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable { onAdd() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            product.imageUri?.let {
                androidx.compose.foundation.Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = product.name,
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            Text(product.name, maxLines = 1)
            Text("${product.price} FCFA", color = Color.Gray)
        }
    }
}


data class CartItem(val product: Product, var quantity: Int)

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


//@Composable
//fun SearchBar(query: String, onValueChange: (String) -> Unit) {
//    OutlinedTextField(
//        value = query,
//        onValueChange = onValueChange,
//        label = { Text("Rechercher un produit") },
//        modifier = Modifier.fillMaxWidth(),
//        singleLine = true
//    )
//}


//@Composable
//fun CartSection(cart: List<Product>, onCartChange: (List<Product>) -> Unit) {
//    Column(
//        Modifier
//            .fillMaxHeight()
//            .padding(8.dp)
//            .background(Color(0xFFF5F5F5))
//    ) {
//        Text("Panier", fontWeight = FontWeight.Bold, fontSize = 20.sp)
//        LazyColumn {
//            items(cart) { product ->
//                Row(
//                    Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(product.name)
//                    Text("${product.price} FCFA")
//                }
//            }
//        }
//        Spacer(Modifier.height(8.dp))
//        val total = cart.sumOf { it.price }
//        Text("Total : $total FCFA", fontWeight = FontWeight.Bold)
//        Button(
//            onClick = { /* TODO: Valider la vente */ },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Valider")
//        }
//    }
//}

@Composable
fun UserScreen(products: List<Product>, onLogout: () -> Unit, dbHelper: AppDbHelper) {
    var searchQuery by remember { mutableStateOf("") }
    var cart by remember { mutableStateOf(mutableListOf<CartItem>()) }
    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Row(Modifier.fillMaxSize()) {
        // Partie produits + recherche
        Column(
            Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            SearchBar(searchQuery) { query -> searchQuery = query }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts) { product ->
                    ProductCard(product) {
                        // Ajouter ou modifier quantité
                        val existing = cart.find { it.product.id == product.id }
                        if (existing != null) {
                            existing.quantity += 1
                        } else {
                            cart.add(CartItem(product, 1))
                        }
                    }
                }
            }
        }

        // Partie droite : panier (tablette)
        if (isTablet()) {
            CartSection(cart) { updatedCart -> cart = updatedCart.toMutableList() }
        }
    }

    // Panier mobile (en bas)
    if (!isTablet()) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(8.dp)
        ) {
            CartSection(cart) { updatedCart -> cart = updatedCart.toMutableList() }
        }
    }
}

@Composable
fun SearchBar(query: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onValueChange,
        label = { Text("Rechercher un produit") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}


@Composable
fun CartSection(cart: List<CartItem>, onCartChange: (List<CartItem>) -> Unit) {
    Column(
        Modifier
            .fillMaxHeight()
            .background(Color(0xFFF5F5F5))
            .padding(8.dp)
    ) {
        Text("Panier", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        LazyColumn {
            items(cart) { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.product.name)
                    Row {
                        IconButton(onClick = {
                            if (item.quantity > 1) item.quantity--
                            onCartChange(cart)
                        }) { Text("-") }
                        Text(item.quantity.toString(), Modifier.align(Alignment.CenterVertically))
                        IconButton(onClick = {
                            item.quantity++
                            onCartChange(cart)
                        }) { Text("+") }
                    }
                    Text("${item.product.price * item.quantity} FCFA")
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        val total = cart.sumOf { it.product.price * it.quantity }
        Text("Total : $total FCFA", fontWeight = FontWeight.Bold)

        Button(onClick = { /* TODO: Valider la vente */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Valider")
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = { onCartChange(emptyList()) }, modifier = Modifier.fillMaxWidth()) {
            Text("Vider le panier")
        }
    }
}

fun isTablet(): Boolean {
    return Resources.getSystem().configuration.smallestScreenWidthDp >= 600
}

