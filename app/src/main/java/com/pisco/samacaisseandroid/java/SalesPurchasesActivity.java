package com.pisco.samacaisseandroid.java;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pisco.samacaisseandroid.AppDbHelper;
import com.pisco.samacaisseandroid.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SalesPurchasesActivity extends AppCompatActivity {

    private AppDbHelper dbHelper;
    private RecyclerView rvPurchases, rvSales;
    private Spinner spinnerFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_purchases);

        dbHelper = new AppDbHelper(this);

        rvPurchases = findViewById(R.id.rvPurchases);
        rvSales = findViewById(R.id.rvSales);
        spinnerFilter = findViewById(R.id.spinnerFilter);

        rvPurchases.setLayoutManager(new LinearLayoutManager(this));
        rvSales.setLayoutManager(new LinearLayoutManager(this));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filter = parent.getItemAtPosition(position).toString();
                loadData(filter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        loadData("Tous");
    }

    private void loadData(String filter) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = null;

        switch (filter) {
            case "Cette semaine":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                startDate = sdf.format(calendar.getTime());
                break;
            case "Ce mois":
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = sdf.format(calendar.getTime());
                break;
            case "Tous":
                startDate = null;
                break;
        }

        // Charger achats
        List<Achat> purchases = dbHelper.getPurchasesFiltered(startDate);
        PurchaseAdapter purchaseAdapter = new PurchaseAdapter(purchases);
        rvPurchases.setAdapter(purchaseAdapter);

        // Charger ventes
        List<AppDbHelper.Sale> sales = dbHelper.getSalesFiltered(startDate);
        SaleAdapter saleAdapter = new SaleAdapter(sales);
        rvSales.setAdapter(saleAdapter);
    }
}
