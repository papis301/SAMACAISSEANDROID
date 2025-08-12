package com.pisco.samacaisseandroid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: AppDbHelper

    private lateinit var titleText: TextView
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var passwordConfirmInput: EditText
    private lateinit var actionButton: Button

    private var isCreateMode = false // true = création admin, false = login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        dbHelper = AppDbHelper(this)

        titleText = findViewById(R.id.titleText)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        passwordConfirmInput = findViewById(R.id.passwordConfirmInput)
        actionButton = findViewById(R.id.actionButton)

        // Vérifie si admin existe
        isCreateMode = !dbHelper.isAdminExists()

        if (isCreateMode) {
            setupCreateMode()
        } else {
            setupLoginMode()
        }

        actionButton.setOnClickListener {
            if (isCreateMode) {
                handleCreateAdmin()
            } else {
                handleLoginAdmin()
            }
        }
    }

    private fun setupCreateMode() {
        titleText.text = "Créer un compte Admin"
        passwordConfirmInput.visibility = View.VISIBLE
        actionButton.text = "Créer Admin"
    }

    private fun setupLoginMode() {
        titleText.text = "Connexion Admin"
        passwordConfirmInput.visibility = View.GONE
        actionButton.text = "Se connecter"
    }

    private fun handleCreateAdmin() {
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val passwordConfirm = passwordConfirmInput.text.toString()

        if (username.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
            Toast.makeText(this, "Tous les champs sont obligatoires", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != passwordConfirm) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
            return
        }

        val created = dbHelper.createAdmin(username, password)
        if (created) {
            Toast.makeText(this, "Admin créé avec succès, veuillez vous connecter", Toast.LENGTH_LONG).show()
            isCreateMode = false
            setupLoginMode()
            clearInputs()
        } else {
            Toast.makeText(this, "Erreur lors de la création de l’admin (username peut-être déjà pris)", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleLoginAdmin() {
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        val valid = dbHelper.checkAdminCredentials(username, password)
        if (valid) {
            Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show()
            // TODO: Lancer l'activité principale (Dashboard, POS, etc.)
            // startActivity(Intent(this, MainActivity::class.java))
            //finish()
            startActivity(Intent(this, UserManagementActivity::class.java))
            finish()

        } else {
            Toast.makeText(this, "Identifiants invalides", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearInputs() {
        usernameInput.text.clear()
        passwordInput.text.clear()
        passwordConfirmInput.text.clear()
    }
}
