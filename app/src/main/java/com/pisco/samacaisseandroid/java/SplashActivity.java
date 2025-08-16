package com.pisco.samacaisseandroid.java;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.pisco.samacaisseandroid.AppDbHelper;

public class SplashActivity extends AppCompatActivity {

    private AppDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new AppDbHelper(this);

        // Vérifie si un admin existe déjà
        if (dbHelper.isAdminExists()) {
            // Admin trouvé → Aller à l'écran de connexion
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
        } else {
            // Aucun admin → Aller à l'inscription de l'admin
            Intent intent = new Intent(SplashActivity.this, RegisterAdminActivity.class);
            startActivity(intent);
        }

        finish(); // Ferme SplashActivity pour éviter de revenir dessus
    }
}
