package com.pisco.samacaisseandroid.java;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pisco.samacaisseandroid.AppDbHelper;
import com.pisco.samacaisseandroid.R;
import com.pisco.samacaisseandroid.UserHistoryActivity;
import com.pisco.samacaisseandroid.UserManagementActivity;
import com.pisco.samacaisseandroid.ui.ClientManagementActivity;

public class AdminInterfaceActivity extends AppCompatActivity {

    Button btnUsers, btnProducts, btnClients, btnLogout, btnHistory, btnfour,
            btnachat, btncompta;
    private AppDbHelper dbHelper;
    TextView tvCompanyName, tvCompanyAddress, tvCompanyPhone;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_interface);

        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvCompanyAddress = findViewById(R.id.tvCompanyAddress);
        tvCompanyPhone = findViewById(R.id.tvCompanyPhone);
        // ✅ Initialisation DB
        dbHelper = new AppDbHelper(this);

        btnUsers = findViewById(R.id.btnUsers);
        btnProducts = findViewById(R.id.btnProducts);
        btnClients = findViewById(R.id.btnClients);
        btnHistory = findViewById(R.id.historiqueuser);
        btnLogout = findViewById(R.id.btnLogout);
        btnfour = findViewById(R.id.btnfournisseu);
        btnachat = findViewById(R.id.btnachats);
        btncompta = findViewById(R.id.compta);

        // Vérifier si l'entreprise existe
        Cursor cursor = dbHelper.getReadableDatabase()
                .rawQuery("SELECT name, address, phone FROM company LIMIT 1", null);

        if (cursor != null && cursor.moveToFirst()) {
            String companyName = cursor.getString(0);
            String companyAddress = cursor.getString(1);
            String companyPhone = cursor.getString(2);

            Toast.makeText(this,
                    "Entreprise : " + companyName + "\nAdresse : " + companyAddress + "\nTéléphone : " + companyPhone,
                    Toast.LENGTH_LONG).show();

            cursor.close();
        } else {
            // Redirection si l’entreprise n’est pas encore définie
            Intent intent = new Intent(this, AddCompanyActivity.class);
            startActivity(intent);
            finish();
        }

        Cursor cursor1 = dbHelper.getCompany();
        if (cursor1 != null && cursor1.moveToFirst()) {
            String name = cursor1.getString(cursor1.getColumnIndexOrThrow("name"));
            String address = cursor1.getString(cursor1.getColumnIndexOrThrow("address"));
            String phone = cursor1.getString(cursor1.getColumnIndexOrThrow("phone"));

            tvCompanyName.setText(name);
            tvCompanyAddress.setText("Adresse : " + address);
            tvCompanyPhone.setText("Téléphone : " + phone);
        } else {
            tvCompanyName.setText("Aucune entreprise définie");
            tvCompanyAddress.setText("");
            tvCompanyPhone.setText("");
        }

        btncompta.setOnClickListener(v -> startActivity(new Intent(AdminInterfaceActivity.this, SalesPurchasesActivity.class)));


        // Redirection vers Gestion achats
        btnachat.setOnClickListener(v -> startActivity(new Intent(AdminInterfaceActivity.this, AchatsListeActivity.class)));

        // Redirection vers Gestion fournisseurs
        btnfour.setOnClickListener(v -> startActivity(new Intent(AdminInterfaceActivity.this, FournisseurListActivity.class)));

        // Redirection vers Gestion Utilisateurs
        btnUsers.setOnClickListener(v -> startActivity(new Intent(AdminInterfaceActivity.this, UserManagementActivity.class)));

        // Redirection vers Gestion Produits
        btnProducts.setOnClickListener(v -> startActivity(new Intent(AdminInterfaceActivity.this, ManageProductsActivity.class)));

        // Redirection vers Gestion Clients
        btnClients.setOnClickListener(v -> startActivity(new Intent(AdminInterfaceActivity.this, ClientManagementActivity.class)));

        // Redirection historique utilisateurs
        btnHistory.setOnClickListener(v -> startActivity(new Intent(AdminInterfaceActivity.this, UserHistoryActivity.class)));

        // Déconnexion
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminInterfaceActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
