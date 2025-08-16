package com.pisco.samacaisseandroid.java;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.pisco.samacaisseandroid.AppDbHelper;

public class FactureActivity extends AppCompatActivity {

    TextView txtFacture;
    AppDbHelper AppDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        txtFacture = new TextView(this);
        setContentView(txtFacture);

        AppDbHelper = new AppDbHelper(this);
        long saleId = getIntent().getLongExtra("sale_id", -1);

        if (saleId != -1) {
            showFacture(saleId);
        }
    }

    private void showFacture(long saleId) {
        SQLiteDatabase db = AppDbHelper.getReadableDatabase();
        Cursor sale = db.rawQuery("SELECT * FROM sales WHERE id=?", new String[]{String.valueOf(saleId)});
        StringBuilder sb = new StringBuilder();

        if (sale.moveToFirst()) {
            sb.append("FACTURE N°").append(saleId).append("\n");
            sb.append("Date: ").append(sale.getString(sale.getColumnIndexOrThrow("date"))).append("\n");
            sb.append("=================================\n");

            // 🔥 Utiliser un JOIN pour récupérer le nom du produit
            Cursor items = db.rawQuery(
                    "SELECT si.quantity, si.price, p.name AS product_name " +
                            "FROM sales_items si " +
                            "JOIN products p ON si.product_id = p.id " +
                            "WHERE si.sale_id=?",
                    new String[]{String.valueOf(saleId)}
            );

            double total = 0;
            while (items.moveToNext()) {
                String name = items.getString(items.getColumnIndexOrThrow("product_name"));
                int qty = items.getInt(items.getColumnIndexOrThrow("quantity"));
                double price = items.getDouble(items.getColumnIndexOrThrow("price"));
                double lineTotal = qty * price; // ✅ calcul du total par ligne
                total += lineTotal;

                sb.append(name)
                        .append(" x").append(qty)
                        .append(" @ ").append(price)
                        .append(" = ").append(lineTotal)
                        .append(" CFA\n");
            }
            items.close();

            sb.append("=================================\n");
            sb.append("TOTAL: ").append(sale.getString(sale.getColumnIndexOrThrow("total"))).append(" CFA\n");
        }
        sale.close();

        txtFacture.setText(sb.toString());
    }

}
