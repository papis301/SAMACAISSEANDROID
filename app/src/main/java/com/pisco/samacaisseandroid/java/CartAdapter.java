package com.pisco.samacaisseandroid.java;


import android.content.Context;
import android.text.InputType;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;

import com.pisco.samacaisseandroid.R;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends BaseAdapter {
    private Context context;
    private List<CartItem> cart;
    private Runnable onCartChanged; // callback pour refresh


    public CartAdapter(Context context, List<CartItem> cart, Runnable onCartChanged) {
        this.context = context;
        this.cart = cart;
        this.onCartChanged = onCartChanged;
    }

    @Override
    public int getCount() {
        return cart.size();
    }

    @Override
    public Object getItem(int position) {
        return cart.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        }

        CartItem item = cart.get(position);

        TextView txtName = convertView.findViewById(R.id.txtProductName);
        TextView txtQuantity = convertView.findViewById(R.id.txtQuantity);
        TextView txtTotal = convertView.findViewById(R.id.txtTotal);
        Button btnEdit = convertView.findViewById(R.id.btnEdit);
        Button btnDelete = convertView.findViewById(R.id.btnDelete);

        txtName.setText(item.getName());
        txtQuantity.setText("x " + item.getQuantity());
        txtTotal.setText(" = " + item.getTotal() + " CFA");

        // Bouton modifier quantité
        btnEdit.setOnClickListener(v -> {
            EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setText(String.valueOf(item.getQuantity()));

            new AlertDialog.Builder(context)
                    .setTitle("Modifier quantité")
                    .setView(input)
                    .setPositiveButton("Valider", (d, w) -> {
                        String value = input.getText().toString().trim();
                        if (!value.isEmpty()) {
                            int newQty = Integer.parseInt(value);
                            if (newQty > 0) {
                                item.setQuantity(newQty);
                            } else {
                                cart.remove(position);
                            }
                            notifyDataSetChanged();
                            onCartChanged.run();
                        }
                    })
                    .setNegativeButton("Annuler", (d, w) -> d.dismiss())
                    .show();
        });

        // Bouton supprimer
        btnDelete.setOnClickListener(v -> {
            cart.remove(position);
            notifyDataSetChanged();
            onCartChanged.run();
        });

        return convertView;
    }
}
