package com.pisco.samacaisseandroid.java;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pisco.samacaisseandroid.AppDbHelper;
import com.pisco.samacaisseandroid.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ManageProductsActivity extends AppCompatActivity {

    private ListView listProducts;
    private Button btnAddProduct;
    private ArrayAdapter<String> adapter;
    private ArrayList<Product> products = new ArrayList<>();
    private AppDbHelper dbHelper;

    private static final int PICK_IMAGE_REQUEST = 100;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        listProducts = findViewById(R.id.listProducts);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        dbHelper = new AppDbHelper(this);

        loadProductsFromDb();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getProductNames());
        listProducts.setAdapter(adapter);

        btnAddProduct.setOnClickListener(v -> showAddProductDialog());

        // clic long pour modifier ou supprimer
        listProducts.setOnItemLongClickListener((parent, view, position, id) -> {
            Product product = products.get(position);
            showEditDeleteDialog(product);
            return true;
        });
    }

    private void loadProductsFromDb() {
        products.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name, price, quantity, unit, image_uri FROM products", null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                double price = cursor.getDouble(2);
                double quantity = cursor.getDouble(3);
                String unit = cursor.getString(4);
                String imageUri = cursor.getString(5);
                products.add(new Product(id, name, price, quantity, unit, imageUri));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private ArrayList<String> getProductNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Product p : products) {
            names.add(p.getName() + " - " + p.getPrice() + " CFA / " + p.getQuantity() + " " + p.getUnit());
        }
        return names;
    }

    private void showAddProductDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        EditText inputName = dialogView.findViewById(R.id.inputName);
        EditText inputPrice = dialogView.findViewById(R.id.inputPrice);
        EditText inputQuantity = dialogView.findViewById(R.id.inputQuantity);
        Spinner spinnerUnit = dialogView.findViewById(R.id.spinnerUnit);
        Button btnChooseImage = dialogView.findViewById(R.id.btnChooseImage);

        // Spinner unité
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"kg", "litre", "mètre"});
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Choisir Image"), PICK_IMAGE_REQUEST);
        });

        new AlertDialog.Builder(this)
                .setTitle("Ajouter Produit")
                .setView(dialogView)
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    String name = inputName.getText().toString().trim();
                    double price = Double.parseDouble(inputPrice.getText().toString().trim());
                    double quantity = Double.parseDouble(inputQuantity.getText().toString().trim());
                    String unit = spinnerUnit.getSelectedItem().toString();

                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("name", name);
                    values.put("price", price);
                    values.put("quantity", quantity);
                    values.put("unit", unit);
                    values.put("date_added", now);
                    if (selectedImageUri != null) {
                        values.put("image_uri", selectedImageUri.toString());
                    }
                    db.insert("products", null, values);

                    loadProductsFromDb();
                    adapter.clear();
                    adapter.addAll(getProductNames());
                    adapter.notifyDataSetChanged();
                    selectedImageUri = null;
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showEditDeleteDialog(Product product) {
        new AlertDialog.Builder(this)
                .setTitle(product.getName())
                .setItems(new String[]{"Modifier", "Supprimer"}, (dialog, which) -> {
                    if (which == 0) showEditProductDialog(product);
                    else deleteProduct(product);
                }).show();
    }

    private void showEditProductDialog(Product product) {
        // Même logique que l'ajout mais prérempli
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        EditText inputName = dialogView.findViewById(R.id.inputName);
        EditText inputPrice = dialogView.findViewById(R.id.inputPrice);
        EditText inputQuantity = dialogView.findViewById(R.id.inputQuantity);
        Spinner spinnerUnit = dialogView.findViewById(R.id.spinnerUnit);
        Button btnChooseImage = dialogView.findViewById(R.id.btnChooseImage);

        inputName.setText(product.getName());
        inputPrice.setText(String.valueOf(product.getPrice()));
        inputQuantity.setText(String.valueOf(product.getQuantity()));

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"kg", "litre", "mètre"});
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);
        spinnerUnit.setSelection(unitAdapter.getPosition(product.getUnit()));

        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Choisir Image"), PICK_IMAGE_REQUEST);
        });

        new AlertDialog.Builder(this)
                .setTitle("Modifier Produit")
                .setView(dialogView)
                .setPositiveButton("Modifier", (dialog, which) -> {
                    String name = inputName.getText().toString().trim();
                    double price = Double.parseDouble(inputPrice.getText().toString().trim());
                    double quantity = Double.parseDouble(inputQuantity.getText().toString().trim());
                    String unit = spinnerUnit.getSelectedItem().toString();
                    String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("name", name);
                    values.put("price", price);
                    values.put("quantity", quantity);
                    values.put("unit", unit);
                    values.put("date_added", now);
                    if (selectedImageUri != null) {
                        values.put("image_uri", selectedImageUri.toString());
                    }

                    db.update("products", values, "id=?", new String[]{String.valueOf(product.getId())});

                    loadProductsFromDb();
                    adapter.clear();
                    adapter.addAll(getProductNames());
                    adapter.notifyDataSetChanged();
                    selectedImageUri = null;
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteProduct(Product product) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("products", "id=?", new String[]{String.valueOf(product.getId())});
        loadProductsFromDb();
        adapter.clear();
        adapter.addAll(getProductNames());
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
        }
    }
}
