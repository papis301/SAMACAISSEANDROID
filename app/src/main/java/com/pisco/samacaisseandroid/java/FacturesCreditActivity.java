package com.pisco.samacaisseandroid.java;

import static com.pisco.samacaisseandroid.AppDbHelper.TABLE_SALES;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.pisco.samacaisseandroid.AppDbHelper;
import com.pisco.samacaisseandroid.R;

import java.util.List;

public class FacturesCreditActivity extends AppCompatActivity {

    private AppDbHelper dbHelper;
    private ListView listFactures;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_factures_credit);

        listFactures = findViewById(R.id.listFacturesCredit);
        dbHelper = new AppDbHelper(this);


        List<AppDbHelper.Sale> creditSales = dbHelper.getCreditSales();

        if (creditSales.isEmpty()) {
            Toast.makeText(this, "Aucune facture à crédit", Toast.LENGTH_SHORT).show();
        } else {
            FactureCreditAdapter adapter = new FactureCreditAdapter(this, creditSales);
            listFactures.setAdapter(adapter);
        }
    }

}