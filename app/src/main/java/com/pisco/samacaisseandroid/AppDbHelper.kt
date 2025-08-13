package com.pisco.samacaisseandroid

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class AppDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "app.db"
        const val DATABASE_VERSION = 1

        // Tables
        const val TABLE_CLIENTS = "clients"
        const val TABLE_PRODUCTS = "products"
        const val TABLE_SALES = "sales"
        const val TABLE_SALES_ITEMS = "sales_items"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Création table users
        db.execSQL("""
        CREATE TABLE users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            password TEXT NOT NULL,
            role TEXT NOT NULL
        );
    """)
        // Création table clients
        db.execSQL("""
            CREATE TABLE $TABLE_CLIENTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT
            );
        """)

        // Création table produits
        db.execSQL("""
            CREATE TABLE $TABLE_PRODUCTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                price REAL NOT NULL,
                quantity REAL NOT NULL DEFAULT 0,
                date_added TEXT NOT NULL,
                unit TEXT NOT NULL, -- kg, litre, mètre...
                image_uri TEXT
            );
        """.trimIndent())

        // Création table ventes
        db.execSQL("""
            CREATE TABLE $TABLE_SALES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                client_id INTEGER,
                date TEXT NOT NULL,
                total REAL NOT NULL,
                FOREIGN KEY(client_id) REFERENCES $TABLE_CLIENTS(id)
            );
        """)

        // Détail des items d'une vente
        db.execSQL("""
            CREATE TABLE $TABLE_SALES_ITEMS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sale_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                price REAL NOT NULL,
                FOREIGN KEY(sale_id) REFERENCES $TABLE_SALES(id),
                FOREIGN KEY(product_id) REFERENCES $TABLE_PRODUCTS(id)
            );
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SALES_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SALES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLIENTS")
        onCreate(db)
    }

    // Vérifier si admin existe
    fun isAdminExists(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM users WHERE role = ?", arrayOf("admin"))
        var exists = false
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0
        }
        cursor.close()
        return exists
    }

    // Créer un nouvel admin
    fun createAdmin(username: String, password: String): Boolean {
        val db = writableDatabase

        // Vérifier que le username n'existe pas déjà
        val cursor = db.rawQuery("SELECT id FROM users WHERE username = ?", arrayOf(username))
        if (cursor.count > 0) {
            cursor.close()
            return false
        }
        cursor.close()

        val values = ContentValues().apply {
            put("username", username)
            put("password", password) // stocker en clair = pas sécurisé, à changer en production
            put("role", "admin")
        }
        val id = db.insert("users", null, values)
        return id != -1L
    }

    // Vérifier identifiants admin
    fun checkAdminCredentials(username: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM users WHERE username = ? AND password = ? AND role = ?",
            arrayOf(username, password, "admin")
        )
        val valid = cursor.count > 0
        cursor.close()
        return valid
    }

    // Récupérer tous les users (sauf admin)
    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, username, role FROM users WHERE role != ?", arrayOf("admin"))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val username = cursor.getString(1)
                val role = cursor.getString(2)
                users.add(User(id, username, role))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return users
    }

    // Ajouter un user
    fun addUser(username: String, password: String, role: String = "user"): Boolean {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT id FROM users WHERE username = ?", arrayOf(username))
        if (cursor.count > 0) {
            cursor.close()
            return false // username déjà utilisé
        }
        cursor.close()

        val values = ContentValues().apply {
            put("username", username)
            put("password", password) // hash à prévoir
            put("role", role)
        }
        val id = db.insert("users", null, values)
        return id != -1L
    }

    // Modifier user (username, password, role)
    fun updateUser(id: Int, username: String, password: String?, role: String): Boolean {
        val db = writableDatabase

        // Vérifier si le nouveau username est déjà pris par un autre user
        val cursor = db.rawQuery("SELECT id FROM users WHERE username = ? AND id != ?", arrayOf(username, id.toString()))
        if (cursor.count > 0) {
            cursor.close()
            return false
        }
        cursor.close()

        val values = ContentValues().apply {
            put("username", username)
            if (password != null) put("password", password)
            put("role", role)
        }

        val rows = db.update("users", values, "id = ?", arrayOf(id.toString()))
        return rows > 0
    }

    // Supprimer user
    fun deleteUser(id: Int): Boolean {
        val db = writableDatabase
        val rows = db.delete("users", "id = ?", arrayOf(id.toString()))
        return rows > 0
    }



    suspend fun getAllUsersSuspend(): List<User> = withContext(Dispatchers.IO) {
        val users = mutableListOf<User>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, username, role FROM users WHERE role != ?", arrayOf("admin"))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val username = cursor.getString(1)
                val role = cursor.getString(2)
                users.add(User(id, username, role))
            } while (cursor.moveToNext())
        }
        cursor.close()
        users
    }

    suspend fun addUserSuspend(username: String, password: String, role: String = "user"): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT id FROM users WHERE username = ?", arrayOf(username))
        if (cursor.count > 0) {
            cursor.close()
            return@withContext false
        }
        cursor.close()

        val values = ContentValues().apply {
            put("username", username)
            put("password", password)
            put("role", role)
        }
        val id = db.insert("users", null, values)
        id != -1L
    }

    suspend fun updateUserSuspend(id: Int, username: String, password: String?, role: String): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase

        val cursor = db.rawQuery("SELECT id FROM users WHERE username = ? AND id != ?", arrayOf(username, id.toString()))
        if (cursor.count > 0) {
            cursor.close()
            return@withContext false
        }
        cursor.close()

        val values = ContentValues().apply {
            put("username", username)
            if (password != null) put("password", password)
            put("role", role)
        }

        val rows = db.update("users", values, "id = ?", arrayOf(id.toString()))
        rows > 0
    }

    suspend fun deleteUserSuspend(id: Int): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val rows = db.delete("users", "id = ?", arrayOf(id.toString()))
        rows > 0
    }

    suspend fun addProductSuspend(
        name: String,
        price: Double,
        quantity: Double,
        unit: String,
        dateAdded: String,
        imageUri: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("price", price)
            put("quantity", quantity)
            put("unit", unit)
            put("date_added", dateAdded)
            put("image_uri", imageUri)
        }
        val id = db.insert(TABLE_PRODUCTS, null, values)
        id != -1L
    }

    suspend fun getAllProductsSuspend(): List<Product> = withContext(Dispatchers.IO) {
        val products = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, name, price, quantity, date_added, image_uri, unit FROM $TABLE_PRODUCTS", null)
        if (cursor.moveToFirst()) {
            do {
                products.add(
                    Product(
                        id = cursor.getInt(0),
                        name = cursor.getString(1),
                        price = cursor.getDouble(2),
                        quantity = cursor.getDouble(3),
                        dateAdded = cursor.getString(4),
                        imageUri = cursor.getString(5),
                        unit = cursor.getString(6)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        products
    }

    suspend fun updateProductSuspend(
        id: Int,
        name: String,
        price: Double,
        quantity: Double,
        unit: String,
        dateAdded: String,
        imageUri: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("price", price)
            put("quantity", quantity)
            put("unit", unit)
            put("date_added", dateAdded)
            put("image_uri", imageUri)
        }
        val rows = db.update(TABLE_PRODUCTS, values, "id = ?", arrayOf(id.toString()))
        rows > 0
    }

    suspend fun deleteProductSuspend(id: Int): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val rows = db.delete(TABLE_PRODUCTS, "id = ?", arrayOf(id.toString()))
        rows > 0
    }

    // Récupérer tous les clients (suspend)
    suspend fun getAllClientsSuspend(): List<Client> = withContext(Dispatchers.IO) {
        val clients = mutableListOf<Client>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, name, phone FROM $TABLE_CLIENTS", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val name = cursor.getString(1)
                val phone = cursor.getString(2)
                clients.add(Client(id, name, phone))
            } while (cursor.moveToNext())
        }
        cursor.close()
        clients
    }

    suspend fun addClientSuspend(name: String, phone: String?): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("phone", phone)
        }
        val id = db.insert(TABLE_CLIENTS, null, values)
        id != -1L
    }

    suspend fun updateClientSuspend(id: Int, name: String, phone: String?): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("phone", phone)
        }
        val rows = db.update(TABLE_CLIENTS, values, "id = ?", arrayOf(id.toString()))
        rows > 0
    }

    suspend fun deleteClientSuspend(id: Int): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val rows = db.delete(TABLE_CLIENTS, "id = ?", arrayOf(id.toString()))
        rows > 0
    }

    suspend fun getUserByUsernameAndPassword(username: String, password: String): User? = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, username, role FROM users WHERE username = ? AND password = ?",
            arrayOf(username, password)
        )
        var user: User? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(0)
            val uname = cursor.getString(1)
            val role = cursor.getString(2)
            user = User(id, uname, role)
        }
        cursor.close()
        user
    }
    fun checkIfAdminExists(): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM users WHERE role = ?",
            arrayOf("admin")
        )
        var exists = false
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0
        }
        cursor.close()
        db.close()
        return exists
    }





    // Supprimer un produit
    fun deleteProduct(id: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_PRODUCTS, "id = ?", arrayOf(id.toString()))
        return result > 0
    }



    // Ajouter un produit
    fun addProduct(product: Product): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", product.name)
            put("price", product.price)
            put("quantity", product.quantity)
            put("unit", product.unit)
            put("image_uri", product.imageUri) // ⚠ nom exact de la colonne
            put("date_added", product.dateAdded) // ⚠ nom exact de la colonne
        }
        val result = db.insert(TABLE_PRODUCTS, null, values)
        return result != -1L
    }

    // Mettre à jour un produit
    fun updateProduct(product: Product): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", product.name)
            put("price", product.price)
            put("quantity", product.quantity)
            put("unit", product.unit)
            put("image_uri", product.imageUri)
            put("date_added", product.dateAdded)
        }
        val result = db.update(
            TABLE_PRODUCTS,
            values,
            "id = ?",
            arrayOf(product.id.toString())
        )
        return result > 0
    }

    // Récupérer tous les produits
    fun getAllProducts(): List<Product> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRODUCTS,
            arrayOf("id", "name", "price", "quantity", "unit", "image_uri", "date_added"),
            null, null, null, null, "id DESC"
        )

        val products = mutableListOf<Product>()
        cursor.use {
            while (it.moveToNext()) {
                products.add(
                    Product(
                        id = it.getInt(it.getColumnIndexOrThrow("id")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        price = it.getDouble(it.getColumnIndexOrThrow("price")),
                        quantity = it.getDouble(it.getColumnIndexOrThrow("quantity")),
                        unit = it.getString(it.getColumnIndexOrThrow("unit")),
                        imageUri = it.getString(it.getColumnIndexOrThrow("image_uri")), // ⚠ nom exact
                        dateAdded = it.getString(it.getColumnIndexOrThrow("date_added")) // ⚠ nom exact
                    )
                )
            }
        }
        return products
    }

}
