package com.pisco.samacaisseandroid.java;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.pisco.samacaisseandroid.AppDbHelper;
import com.pisco.samacaisseandroid.R;

import java.text.SimpleDateFormat;
import java.util.*;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.SearchView;



public class CaisseActivity extends AppCompatActivity {

    ListView listProducts, listCart;
    Button btnValidate;
    ArrayAdapter<String> productAdapter;
    CartAdapter cartAdapter;
    ArrayList<Product> products = new ArrayList<>();
    ArrayList<CartItem> cart = new ArrayList<>();
    AppDbHelper dbHelper;
    Toolbar toolbar;
    TextView txtTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caisse);

        listProducts = findViewById(R.id.listProducts);
        listCart = findViewById(R.id.listCart);
        btnValidate = findViewById(R.id.btnValidate);
        dbHelper = new AppDbHelper(this);
        txtTotal = findViewById(R.id.txtTotal);

         toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Caisse");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_connect_printer) {
                Toast.makeText(this, "Connexion imprimante...", Toast.LENGTH_SHORT).show();
                // 👉 Ici tu mets ton code de connexion Bluetooth
                return true;
            }
            return false;
        });


        // Charger produits depuis SQLite
        loadProductsFromDb();

        // Adapter Produits
        ArrayList<String> productNames = new ArrayList<>();
        for (Product p : products) productNames.add(p.getName() + " - " + p.getPrice() + " CFA");
        productAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productNames);
        listProducts.setAdapter(productAdapter);

        // Clic produit → dialogue quantité
        listProducts.setOnItemClickListener((parent, view, position, id) -> {
            Product selected = products.get(position);
            showQuantityDialog(selected);
        });

//        // Clic panier → suppression
//        listCart.setOnItemClickListener((parent, view, position, id) -> {
//            cart.remove(position);
//            refreshCart();
//        });
        listCart.setOnItemClickListener((parent, view, position, id) -> {
            CartItem item = cart.get(position);

            new AlertDialog.Builder(this)
                    .setTitle("Action sur " + item.getName())
                    .setMessage("Que voulez-vous faire ?")
                    .setPositiveButton("Modifier quantité", (dialog, which) -> {
                        // Ouvrir un second dialog avec EditText pour modifier
                        EditText input = new EditText(this);
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        input.setText(String.valueOf(item.getQuantity()));

                        new AlertDialog.Builder(this)
                                .setTitle("Modifier la quantité")
                                .setView(input)
                                .setPositiveButton("Valider", (d, w) -> {
                                    String value = input.getText().toString().trim();
                                    if (!value.isEmpty()) {
                                        int newQuantity = Integer.parseInt(value);
                                        if (newQuantity > 0) {
                                            item.setQuantity(newQuantity);
                                        } else {
                                            cart.remove(position); // 0 → suppression
                                        }
                                        refreshCart();
                                    }
                                })
                                .setNegativeButton("Annuler", (d, w) -> d.dismiss())
                                .show();
                    })
                    .setNegativeButton("Supprimer", (dialog, which) -> {
                        cart.remove(position);
                        refreshCart();
                    })
                    .setNeutralButton("Annuler", (dialog, which) -> dialog.dismiss())
                    .show();
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
                .setTitle("Quantité pour " + product.getName())
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
        // Calcul du total
        double total = 0;
        for (CartItem item : cart) {
            total += item.getTotal();
        }

        // Mise à jour du TextView total

        txtTotal.setText("Total: " + total + " CFA");

        // Mettre à jour l'adapter du panier
            cartAdapter = new CartAdapter(this, cart, this::refreshCart);
            listCart.setAdapter(cartAdapter);


//
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.caisse_menu, menu);

        // Récupérer SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItem logout = menu.findItem(R.id.action_logout);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("Rechercher un produit...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                productAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                productAdapter.getFilter().filter(newText);
                return false;
            }
        });

        //MenuItem logout1 = logout;
        MenuItem menuItem = logout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                // Déconnexion
                SharedPreferences prefs = getSharedPreferences("session_prefs", MODE_PRIVATE);
                long sessionId = prefs.getLong("current_session_id", -1);

                if (sessionId != -1) {
                    String logoutTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("logout_time", logoutTime);

                    db.update("user_sessions", values, "id=?", new String[]{String.valueOf(sessionId)});
                }
                Toast.makeText(getApplicationContext(), "Déconnexion...", Toast.LENGTH_SHORT).show();

                // Vider le panier si nécessaire
                cart.clear();
                refreshCart();

                // Rediriger vers LoginActivity et fermer la CaisseActivity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // ferme CaisseActivity
                return true;
            }
        });


        return true;
    }




}
