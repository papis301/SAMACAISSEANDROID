package com.pisco.samacaisseandroid.java;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pisco.samacaisseandroid.AppDbHelper;
import com.pisco.samacaisseandroid.R;

import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product> {
    private Context context;
    private List<Product> products;
    private AppDbHelper db;

    public ProductAdapter(Context context, List<Product> products, AppDbHelper db) {
        super(context, 0, products);
        this.context = context;
        this.products = products;
        this.db = db;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        }

        Product product = products.get(position);

        TextView tvInfo = convertView.findViewById(R.id.tvProductInfo);
        Button btnEdit = convertView.findViewById(R.id.btnEdit);
        Button btnDelete = convertView.findViewById(R.id.btnDelete);

        // Infos affichées
        tvInfo.setText(product.getName() + " - " + product.getPrice() + " CFA / " + product.getQuantity() + " " + product.getUnit());

        // Bouton Modifier
        btnEdit.setOnClickListener(v -> {
            if (context instanceof ManageProductsActivity) {
                ((ManageProductsActivity) context).showEditDialog(product);
            }
        });

        // Bouton Supprimer
        btnDelete.setOnClickListener(v -> {
            db.deleteProduct(product.getId());
            products.remove(position);
            notifyDataSetChanged();
            Toast.makeText(context, "Produit supprimé", Toast.LENGTH_SHORT).show();
        });

        return convertView;
    }
}

