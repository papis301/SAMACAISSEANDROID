package com.pisco.samacaisseandroid.java;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pisco.samacaisseandroid.AppDbHelper;
import com.pisco.samacaisseandroid.R;

import java.util.List;

public class FactureCreditAdapter extends ArrayAdapter<AppDbHelper.Sale> {

    public FactureCreditAdapter(Context context, List<AppDbHelper.Sale> factures) {
        super(context, 0, factures);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppDbHelper.Sale facture = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_facture_credit, parent, false);
        }

        TextView txtClient = convertView.findViewById(R.id.txtClientName);
        TextView txtTotal = convertView.findViewById(R.id.txtTotal);
        TextView txtDate = convertView.findViewById(R.id.txtDate);

        // ⚠️ tu dois t’assurer que la table "sales" a bien clientName, sinon afficher "Inconnu"
        txtClient.setText("Client: " + (facture.getClientName() != null ? facture.getClientName() : "Inconnu"));
        txtTotal.setText("Total: " + facture.getTotal() + " CFA");
        txtDate.setText("Date: " + facture.getDate());

        return convertView;
    }
}
