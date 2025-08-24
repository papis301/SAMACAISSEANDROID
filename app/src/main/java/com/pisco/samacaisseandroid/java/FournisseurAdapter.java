package com.pisco.samacaisseandroid.java;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pisco.samacaisseandroid.R;

import java.util.List;

public class FournisseurAdapter extends ArrayAdapter<Supplier> {

    public FournisseurAdapter(Context context, List<Supplier> suppliers) {
        super(context, 0, suppliers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Supplier supplier = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_supplier, parent, false);
        }

        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvPhone = convertView.findViewById(R.id.tvPhone);
        TextView tvAddress = convertView.findViewById(R.id.tvAddress);

        tvName.setText(supplier.getName());
        tvPhone.setText("📞 " + supplier.getPhone());
        tvAddress.setText("📍 " + supplier.getAddress());

        return convertView;
    }
}

