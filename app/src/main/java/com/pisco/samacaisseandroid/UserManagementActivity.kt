package com.pisco.samacaisseandroid

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UserManagementActivity : AppCompatActivity() {

    private lateinit var dbHelper: AppDbHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private var userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        dbHelper = AppDbHelper(this)

        recyclerView = findViewById(R.id.recyclerViewUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadUsers()

        findViewById<FloatingActionButton>(R.id.fabAddUser).setOnClickListener {
            showAddUserDialog()
        }
    }

    private fun loadUsers() {
        userList = dbHelper.getAllUsers().toMutableList()
        adapter = UserAdapter(userList,
            onEdit = { user -> showEditUserDialog(user) },
            onDelete = { user -> confirmDeleteUser(user) }
        )
        recyclerView.adapter = adapter
    }

    private fun showAddUserDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_user_form, null)
        val usernameInput = dialogView.findViewById<EditText>(R.id.usernameInput)
        val passwordInput = dialogView.findViewById<EditText>(R.id.passwordInput)

        AlertDialog.Builder(this)
            .setTitle("Ajouter un utilisateur")
            .setView(dialogView)
            .setPositiveButton("Ajouter") { _, _ ->
                val username = usernameInput.text.toString().trim()
                val password = passwordInput.text.toString()

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val success = dbHelper.addUser(username, password)
                if (success) {
                    Toast.makeText(this, "Utilisateur ajouté", Toast.LENGTH_SHORT).show()
                    loadUsers()
                } else {
                    Toast.makeText(this, "Nom d’utilisateur déjà utilisé", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showEditUserDialog(user: User) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_user_form, null)
        val usernameInput = dialogView.findViewById<EditText>(R.id.usernameInput)
        val passwordInput = dialogView.findViewById<EditText>(R.id.passwordInput)

        usernameInput.setText(user.username)

        AlertDialog.Builder(this)
            .setTitle("Modifier utilisateur")
            .setView(dialogView)
            .setPositiveButton("Modifier") { _, _ ->
                val username = usernameInput.text.toString().trim()
                val password = passwordInput.text.toString().takeIf { it.isNotEmpty() } // mot de passe facultatif

                if (username.isEmpty()) {
                    Toast.makeText(this, "Nom d’utilisateur obligatoire", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val success = dbHelper.updateUser(user.id, username, password, user.role)
                if (success) {
                    Toast.makeText(this, "Utilisateur modifié", Toast.LENGTH_SHORT).show()
                    loadUsers()
                } else {
                    Toast.makeText(this, "Erreur ou nom déjà utilisé", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun confirmDeleteUser(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Supprimer utilisateur")
            .setMessage("Supprimer ${user.username} ?")
            .setPositiveButton("Oui") { _, _ ->
                val success = dbHelper.deleteUser(user.id)
                if (success) {
                    Toast.makeText(this, "Utilisateur supprimé", Toast.LENGTH_SHORT).show()
                    loadUsers()
                } else {
                    Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Non", null)
            .show()
    }
}
