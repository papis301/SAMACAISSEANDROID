package com.pisco.samacaisseandroid.java;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.pisco.samacaisseandroid.AppDbHelper;

import java.util.List;

public class SaleAdapter extends RecyclerView.Adapter<SaleAdapter.ViewHolder> {

    private List<AppDbHelper.Sale> items;

    public SaleAdapter(List<AppDbHelper.Sale> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInfo;
        public ViewHolder(View itemView) {
            super(itemView);
            tvInfo = itemView.findViewById(android.R.id.text1);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View tv = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(tv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppDbHelper.Sale item = items.get(position);
        holder.tvInfo.setText(item.getDate() + " - Total: " + item.getTotal());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

