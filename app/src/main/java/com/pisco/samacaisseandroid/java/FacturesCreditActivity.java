package com.pisco.samacaisseandroid.java;

import android.database.Cursor;
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

        loadFacturesCredit();

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }

    private void loadFacturesCredit() {
        Cursor cursor = dbHelper.getReadableDatabase()
                .rawQuery("SELECT _id, clientName, total, date FROM sales WHERE type='credit'", null);

        if (cursor != null && cursor.getCount() > 0) {
            String[] from = {"clientName", "total", "date"};
            int[] to = {R.id.txtClientName, R.id.txtTotal, R.id.txtDate};

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this,
                    R.layout.item_facture_credit,
                    cursor,
                    from,
                    to,
                    0
            );
            listFactures.setAdapter(adapter);
        } else {
            Toast.makeText(this, "Aucune facture à crédit trouvée", Toast.LENGTH_SHORT).show();
        }
    }
}