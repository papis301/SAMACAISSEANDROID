package com.pisco.samacaisseandroid.java;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pisco.samacaisseandroid.AppDbHelper;
import com.pisco.samacaisseandroid.R;

import java.util.List;

public class AchatsListeActivity extends AppCompatActivity {

    private ListView lvPurchases;
    private AppDbHelper dbHelper;
    private FloatingActionButton fabAddPurchase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achats_liste);

        lvPurchases = findViewById(R.id.lvPurchases);
        fabAddPurchase = findViewById(R.id.fabAddPurchase);

        dbHelper = new AppDbHelper(this);

        List<Achat> purchases = dbHelper.getAllPurchases();
        AchatAdapter adapter = new AchatAdapter(this, purchases);
        lvPurchases.setAdapter(adapter);

        // Action bouton + → aller vers l’écran d’ajout d’achat
        fabAddPurchase.setOnClickListener(v -> {
            Intent intent = new Intent(AchatsListeActivity.this, AchatFormActivity.class);
            startActivity(intent);
        });
    }
}
