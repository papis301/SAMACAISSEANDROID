package com.pisco.samacaisseandroid.java;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.pisco.samacaisseandroid.ProductManagementActivity;
import com.pisco.samacaisseandroid.R;
import com.pisco.samacaisseandroid.UserHistoryActivity;
import com.pisco.samacaisseandroid.UserManagementActivity;
import com.pisco.samacaisseandroid.ui.ClientManagementActivity;

public class AdminInterfaceActivity extends AppCompatActivity {

    Button btnUsers, btnProducts, btnClients, btnLogout, btnHistory;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_interface);

        btnUsers = findViewById(R.id.btnUsers);
        btnProducts = findViewById(R.id.btnProducts);
        btnClients = findViewById(R.id.btnClients);
        btnHistory = findViewById(R.id.historiqueuser);
        btnLogout = findViewById(R.id.btnLogout);

        // Redirection vers Gestion Utilisateurs
        btnUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminInterfaceActivity.this, UserManagementActivity.class));
            }
        });

        // Redirection vers Gestion Produits
        btnProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminInterfaceActivity.this, ManageProductsActivity.class));
            }
        });

        // Redirection vers Gestion Clients
        btnClients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminInterfaceActivity.this, ClientManagementActivity.class));
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminInterfaceActivity.this, UserHistoryActivity.class));
            }
        });

        // Déconnexion
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminInterfaceActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
