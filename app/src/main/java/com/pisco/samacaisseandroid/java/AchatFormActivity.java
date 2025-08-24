package com.pisco.samacaisseandroid.java;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pisco.samacaisseandroid.AppDbHelper;
import com.pisco.samacaisseandroid.Product;
import com.pisco.samacaisseandroid.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AchatFormActivity extends AppCompatActivity {

    private Spinner spSupplier, spProduct;
    private EditText etQuantity, etPrice, etDate;
    private Button btnSavePurchase;
    private AppDbHelper dbHelper;

    private List<Supplier> suppliers;
    private @NotNull List<com.pisco.samacaisseandroid.@NotNull Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achat_form);

        dbHelper = new AppDbHelper(this);

        spSupplier = findViewById(R.id.spSupplier);
        spProduct = findViewById(R.id.spProduct);
        etQuantity = findViewById(R.id.etQuantity);
        etPrice = findViewById(R.id.etPrice);
        etDate = findViewById(R.id.etDate);
        btnSavePurchase = findViewById(R.id.btnSavePurchase);

        loadSuppliers();
        loadProducts();

        btnSavePurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePurchase();
            }
        });
    }

    private void loadSuppliers() {
        suppliers = dbHelper.getAllSuppliers();
        List<String> supplierNames = new ArrayList<>();
        for (Supplier s : suppliers) {
            supplierNames.add(s.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, supplierNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSupplier.setAdapter(adapter);
    }

    private void loadProducts() {
        products = dbHelper.getAllProducts();
        List<String> productNames = new ArrayList<>();
        for (@NotNull Product p : products) {
            productNames.add(p.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, productNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spProduct.setAdapter(adapter);
    }

    private void savePurchase() {
        if (suppliers.isEmpty() || products.isEmpty()) {
            Toast.makeText(this, "Ajoutez d'abord un fournisseur et un produit.", Toast.LENGTH_SHORT).show();
            return;
        }

        int supplierId = suppliers.get(spSupplier.getSelectedItemPosition()).getId();
        int productId = products.get(spProduct.getSelectedItemPosition()).getId();
        int quantity = Integer.parseInt(etQuantity.getText().toString());
        double price = Double.parseDouble(etPrice.getText().toString());
        String date = etDate.getText().toString();

        dbHelper.addPurchase(supplierId, productId, quantity, price, date);

        Toast.makeText(this, "Achat enregistré avec succès", Toast.LENGTH_SHORT).show();
        finish();
    }
}
