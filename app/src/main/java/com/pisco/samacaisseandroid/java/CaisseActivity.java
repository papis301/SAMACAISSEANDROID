package com.pisco.samacaisseandroid.java;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.pisco.samacaisseandroid.AppDbHelper;
import com.pisco.samacaisseandroid.R;

import java.text.SimpleDateFormat;
import java.util.*;

public class CaisseActivity extends AppCompatActivity {

    ListView listProducts, listCart;
    Button btnValidate;
    ArrayAdapter<String> productAdapter;
    CartAdapter cartAdapter;
    ArrayList<Product> products = new ArrayList<>();
    ArrayList<CartItem> cart = new ArrayList<>();
    AppDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caisse);

        listProducts = findViewById(R.id.listProducts);
        listCart = findViewById(R.id.listCart);
        btnValidate = findViewById(R.id.btnValidate);
        dbHelper = new AppDbHelper(this);

        // Charger produits depuis SQLite
        loadProductsFromDb();

        // Adapter Produits
        ArrayList<String> productNames = new ArrayList<>();
        for (Product p : products) productNames.add(p.name + " - " + p.price + " CFA");
        productAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productNames);
        listProducts.setAdapter(productAdapter);

        // Clic produit → dialogue quantité
        listProducts.setOnItemClickListener((parent, view, position, id) -> {
            Product selected = products.get(position);
            showQuantityDialog(selected);
        });

        // Clic panier → suppression
        listCart.setOnItemClickListener((parent, view, position, id) -> {
            cart.remove(position);
            refreshCart();
        });

        // Valider facture
        btnValidate.setOnClickListener(v -> saveSale());
    }

    private void loadProductsFromDb() {
        products.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT id, name, price FROM products", null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                double price = cursor.getDouble(2);
                products.add(new Product(id, name, price));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void showQuantityDialog(Product product) {
        EditText input = new EditText(this);
        input.setHint("Quantité");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(this)
                .setTitle("Quantité pour " + product.name)
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String val = input.getText().toString();
                    if (!val.isEmpty()) {
                        int qty = Integer.parseInt(val);
                        cart.add(new CartItem(product, qty));
                        refreshCart();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void refreshCart() {
        ArrayList<String> cartNames = new ArrayList<>();
        for (CartItem c : cart) {
            cartNames.add(c.product.name + " x" + c.quantity + " = " + c.getTotal() + " CFA");
        }
        cartAdapter = new CartAdapter(this, cart);
        listCart.setAdapter(cartAdapter);
    }

//    private void saveSale() {
//        double total = 0;
//        for (CartItem c : cart) total += c.getTotal();
//
//
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("total", total);
//        values.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
//        long saleId = db.insert("sales", null, values);
//
//        for (CartItem c : cart) {
//            ContentValues item = new ContentValues();
//            item.put("sale_id", saleId);
//            item.put("product_name", c.product.name);
//            item.put("qty", c.quantity);
//            item.put("price", c.product.price);
//            item.put("total", c.getTotal());
//            db.insert("sales_item", null, item);
//        }
//
//        // Afficher facture
//        Intent intent = new Intent(this, FactureActivity.class);
//        intent.putExtra("sale_id", saleId);
//        startActivity(intent);
//        cart.clear();
//        refreshCart();
//    }
private void saveSale() {
    long saleId = dbHelper.saveSaleWithItems(cart);

    if (saleId != -1) {
        // Afficher facture
        Intent intent = new Intent(this, FactureActivity.class);
        intent.putExtra("sale_id", saleId);
        startActivity(intent);

        // Vider le panier
        cart.clear();
        refreshCart();
    } else {
        Toast.makeText(this, "Erreur lors de l'enregistrement de la vente", Toast.LENGTH_SHORT).show();
    }
}

}
