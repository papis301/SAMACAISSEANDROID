package com.pisco.samacaisseandroid.java;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.pisco.samacaisseandroid.AppDbHelper;
import com.pisco.samacaisseandroid.R;

import java.util.ArrayList;
import java.util.List;

public class SupplierListActivity extends AppCompatActivity {

    private AppDbHelper dbHelper;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<Supplier> suppliers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_list);

        dbHelper = new AppDbHelper(this);
        listView = findViewById(R.id.lvSuppliers);

        loadSuppliers();

        Button btnAddSupplier = findViewById(R.id.btnAddSupplier);
        btnAddSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSupplierDialog(null);
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            showSupplierDialog(suppliers.get(position));
        });
    }

    private void loadSuppliers() {
        suppliers = dbHelper.getAllSuppliers();
        List<String> supplierNames = new ArrayList<>();
        for (Supplier s : suppliers) {
            supplierNames.add(s.getName());
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, supplierNames);
        listView.setAdapter(adapter);
    }

    private void showSupplierDialog(Supplier supplier) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_supplier, null);

        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);
        EditText etAddress = dialogView.findViewById(R.id.etAddress);

        if (supplier != null) {
            etName.setText(supplier.getName());
            etPhone.setText(supplier.getPhone());
            etAddress.setText(supplier.getAddress());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(supplier == null ? "Ajouter un fournisseur" : "Modifier le fournisseur");
        builder.setView(dialogView);

        builder.setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = etName.getText().toString();
                String phone = etPhone.getText().toString();
                String address = etAddress.getText().toString();

                if (supplier == null) {
                    dbHelper.addSupplier(name, phone, address);
                } else {
                    dbHelper.updateSupplier(supplier.getId(), name, phone, address);
                }
                loadSuppliers();
            }
        });

        builder.setNegativeButton("Annuler", null);

        if (supplier != null) {
            builder.setNeutralButton("Supprimer", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dbHelper.deleteSupplier(supplier.getId());
                    loadSuppliers();
                }
            });
        }

        builder.show();
    }
}