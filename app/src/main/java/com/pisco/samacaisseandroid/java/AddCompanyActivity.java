package com.pisco.samacaisseandroid.java;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.pisco.samacaisseandroid.AppDbHelper;
import com.pisco.samacaisseandroid.R;

public class AddCompanyActivity extends AppCompatActivity {

    EditText etCompanyName, etCompanyAddress, etCompanyPhone;
    Button btnSaveCompany;
    AppDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_company);

        etCompanyName = findViewById(R.id.etCompanyName);
        etCompanyAddress = findViewById(R.id.etCompanyAddress);
        etCompanyPhone = findViewById(R.id.etCompanyPhone);
        btnSaveCompany = findViewById(R.id.btnSaveCompany);
        dbHelper = new AppDbHelper(this);

        btnSaveCompany.setOnClickListener(v -> {
            String name = etCompanyName.getText().toString().trim();
            String address = etCompanyAddress.getText().toString().trim();
            String phone = etCompanyPhone.getText().toString().trim();

            if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("address", address);
            values.put("phone", phone);

            db.insert("company", null, values);
            db.close();

            Toast.makeText(this, "Entreprise enregistrée", Toast.LENGTH_SHORT).show();

            // Retour vers AdminInterfaceActivity
            Intent intent = new Intent(this, AdminInterfaceActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
