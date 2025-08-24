package com.pisco.samacaisseandroid.java;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pisco.samacaisseandroid.R;

import java.util.List;

public class AchatAdapter extends ArrayAdapter<Achat> {

    public AchatAdapter(Context context, List<Achat> Achats) {
        super(context, 0, Achats);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_achat, parent, false);
        }

        Achat Achat = getItem(position);

        TextView tvSupplier = convertView.findViewById(R.id.tvSupplier);
        TextView tvProduct = convertView.findViewById(R.id.tvProduct);
        TextView tvDetails = convertView.findViewById(R.id.tvDetails);

        tvSupplier.setText("Fournisseur: " + Achat.getSupplierName());
        tvProduct.setText("Produit: " + Achat.getProductName());
        tvDetails.setText("Qté: " + Achat.getQuantity() +
                " | Prix: " + Achat.getPrice() +
                " | Total: " + Achat.getTotal() +
                " | Date: " + Achat.getDate());

        return convertView;
    }
}
