package com.pisco.samacaisseandroid.java;


import android.content.Context;
import android.view.*;
import android.widget.*;
import com.pisco.samacaisseandroid.R;
import java.util.ArrayList;

public class CartAdapter extends ArrayAdapter<CartItem> {

    private final ArrayList<CartItem> cart;
    private final LayoutInflater inflater;

    public CartAdapter(Context context, ArrayList<CartItem> cart) {
        super(context, 0, cart);
        this.cart = cart;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_cart, parent, false);
        }

        CartItem item = cart.get(position);

        TextView txtCartItem = convertView.findViewById(R.id.txtCartItem);
        Button btnRemove = convertView.findViewById(R.id.btnRemove);

        txtCartItem.setText(item.product.name + " x" + item.quantity + " = " + item.getTotal() + " CFA");

        btnRemove.setOnClickListener(v -> {
            cart.remove(position);
            notifyDataSetChanged();
        });

        return convertView;
    }
}

