package com.pisco.samacaisseandroid.java;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.pisco.samacaisseandroid.AppDbHelper;
import com.pisco.samacaisseandroid.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class FactureActivity extends AppCompatActivity {

    TextView txtFacture;
    AppDbHelper AppDbHelper;
    private Button btnShare, btnPrint;
    private long saleId;
    StringBuilder facturestring ;
    double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        txtFacture = new TextView(this);
        setContentView(R.layout.factureactivity);

        AppDbHelper = new AppDbHelper(this);

        txtFacture = findViewById(R.id.txtFacture);
        btnShare = findViewById(R.id.btnShare);
        btnPrint = findViewById(R.id.btnPrint);

        saleId = getIntent().getLongExtra("sale_id", -1);

        if (saleId != -1) {
            showFacture(saleId);
        }

        // Bouton partager
        btnShare.setOnClickListener(v -> {
            File file = generateFacturePdf(txtFacture.getText().toString(), saleId);
            if (file != null) {
                shareFacture(file);
            }
        });

        // Bouton imprimer (Bluetooth)
        btnPrint.setOnClickListener(v -> {
            //printBluetooth(txtFacture.getText().toString());
            Intent intent = new Intent(FactureActivity.this, ImprimerKotlin.class);
            intent.putExtra("lafacture", (CharSequence) facturestring);// écran admin
            intent.putExtra("total", total);
            startActivity(intent);
            finish();
        });
    }

    // Générer le PDF
    private File generateFacturePdf(String factureText, long saleId) {
        try {
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(12f);

            String[] lines = factureText.split("\n");
            float y = 20f;
            for (String line : lines) {
                canvas.drawText(line, 10f, y, paint);
                y += paint.getTextSize() + 5;
            }

            pdfDocument.finishPage(page);

            File file = new File(getCacheDir(), "facture_" + saleId + ".pdf");
            pdfDocument.writeTo(new FileOutputStream(file));
            pdfDocument.close();

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Partager le PDF
    private void shareFacture(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Partager la facture"));
    }

    // Impression Bluetooth ESC/POS
    private void printBluetooth(String factureText) {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "Activez le Bluetooth", Toast.LENGTH_SHORT).show();
                return;
            }

            // Récupérer l’imprimante déjà appairée
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            BluetoothDevice printer = null;
            for (BluetoothDevice device : pairedDevices) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                if (device.getName().toLowerCase().contains("printer")) { // adapter selon ton imprimante
                    printer = device;
                    break;
                }
            }

            if (printer == null) {
                Toast.makeText(this, "Aucune imprimante trouvée", Toast.LENGTH_SHORT).show();
                return;
            }

            BluetoothSocket socket = printer.createRfcommSocketToServiceRecord(
                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")); // SPP UUID
            socket.connect();

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(factureText.getBytes("UTF-8"));
            outputStream.write("\n\n\n".getBytes()); // saut de ligne
            outputStream.flush();

            socket.close();
            Toast.makeText(this, "Impression réussie", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur impression", Toast.LENGTH_SHORT).show();
        }
    }




    private void showFacture(long saleId) {
        SQLiteDatabase db = AppDbHelper.getReadableDatabase();
        StringBuilder sb = new StringBuilder();

        // 🔹 Récupérer infos entreprise depuis la table company
        Cursor companyCursor = db.rawQuery("SELECT name, address, phone FROM company LIMIT 1", null);
        if (companyCursor != null && companyCursor.moveToFirst()) {
            String companyName = companyCursor.getString(companyCursor.getColumnIndexOrThrow("name"));
            String companyAddress = companyCursor.getString(companyCursor.getColumnIndexOrThrow("address"));
            String companyPhone = companyCursor.getString(companyCursor.getColumnIndexOrThrow("phone"));

            sb.append(companyName).append("\n");
            sb.append("Adresse: ").append(companyAddress).append("\n");
            sb.append("Tel: ").append(companyPhone).append("\n");
            sb.append("=============================\n");

            companyCursor.close();
        } else {
            sb.append("Entreprise non définie\n=============================\n");
        }

        // 🔹 Récupérer la vente
        Cursor sale = db.rawQuery("SELECT * FROM sales WHERE id=?", new String[]{String.valueOf(saleId)});
        if (sale.moveToFirst()) {
            sb.append("FACTURE N°").append(saleId).append("\n");
            sb.append("Date: ").append(sale.getString(sale.getColumnIndexOrThrow("date"))).append("\n");

            // 🔹 Récupérer le client si existant
            int clientId = sale.getInt(sale.getColumnIndexOrThrow("client_id"));
            if (clientId > 0) {
                Cursor clientCursor = db.rawQuery("SELECT name, phone FROM clients WHERE id=?", new String[]{String.valueOf(clientId)});
                if (clientCursor.moveToFirst()) {
                    String clientName = clientCursor.getString(clientCursor.getColumnIndexOrThrow("name"));
                    String clientPhone = clientCursor.getString(clientCursor.getColumnIndexOrThrow("phone"));
                    sb.append("Client: ").append(clientName).append(" (").append(clientPhone).append(")\n");
                }
                clientCursor.close();
            } else {
                sb.append("Client: Aucun\n");
            }

            sb.append("=============================\n");

            // 🔹 Produits
            Cursor items = db.rawQuery(
                    "SELECT si.quantity, si.price, p.name " +
                            "FROM sales_items si " +
                            "JOIN products p ON si.product_id = p.id " +
                            "WHERE si.sale_id=?",
                    new String[]{String.valueOf(saleId)}
            );

            double total = Double.parseDouble(sale.getString(sale.getColumnIndexOrThrow("total")));
            while (items.moveToNext()) {
                String name = items.getString(items.getColumnIndexOrThrow("name"));
                double qty = items.getDouble(items.getColumnIndexOrThrow("quantity"));
                double price = items.getDouble(items.getColumnIndexOrThrow("price"));
                double lineTotal = qty * price;

                sb.append(name)
                        .append(" x").append(qty)
                        .append(" @ ").append(price)
                        .append(" = ").append(lineTotal)
                        .append(" CFA\n");
            }
            items.close();

            sb.append("=============================\n");
            sb.append("TOTAL: ").append(sale.getString(sale.getColumnIndexOrThrow("total"))).append(" CFA");
        }
        sale.close();

        txtFacture.setText(sb.toString());
        facturestring = new StringBuilder(sb.toString());
    }


//    private void showFacture(long saleId) {
//        SQLiteDatabase db = AppDbHelper.getReadableDatabase();
//        Cursor sale = db.rawQuery("SELECT * FROM sales WHERE id=?", new String[]{String.valueOf(saleId)});
//        StringBuilder sb = new StringBuilder();
//
//        if (sale.moveToFirst()) {
//            sb.append("FACTURE N°").append(saleId).append("\n");
//            sb.append("Date: ").append(sale.getString(sale.getColumnIndexOrThrow("date"))).append("\n");
//
//            // Récupérer le client si existant
//            int clientId = sale.getInt(sale.getColumnIndexOrThrow("client_id"));
//            if (clientId > 0) {
//                Cursor clientCursor = db.rawQuery("SELECT name, phone FROM clients WHERE id=?", new String[]{String.valueOf(clientId)});
//                if (clientCursor.moveToFirst()) {
//                    String clientName = clientCursor.getString(clientCursor.getColumnIndexOrThrow("name"));
//                    String clientPhone = clientCursor.getString(clientCursor.getColumnIndexOrThrow("phone"));
//                    sb.append("Client: ").append(clientName).append(" (").append(clientPhone).append(")\n");
//                }
//                clientCursor.close();
//            } else {
//                sb.append("Client: Aucun\n");
//            }
//
//            sb.append("=================================\n");
//
//            // 🔥 Utiliser un JOIN pour récupérer le nom du produit
//            Cursor items = db.rawQuery(
//                    "SELECT si.quantity, si.price, p.name " +
//                            "FROM sales_items si " +
//                            "JOIN products p ON si.product_id = p.id " +
//                            "WHERE si.sale_id=?",
//                    new String[]{String.valueOf(saleId)}
//            );
//
//            total = Double.parseDouble(sale.getString(sale.getColumnIndexOrThrow("total")));
//            while (items.moveToNext()) {
//                String name = items.getString(items.getColumnIndexOrThrow("name"));
//                double qty = items.getDouble(items.getColumnIndexOrThrow("quantity"));
//                double price = items.getDouble(items.getColumnIndexOrThrow("price"));
//                double lineTotal = qty * price;
//
//                sb.append(name)
//                        .append(" x").append(qty)
//                        .append(" @ ").append(price)
//                        .append(" = ").append(lineTotal)
//                        .append(" CFA\n");
//            }
//            items.close();
//
//            sb.append("=================================\n");
//            sb.append("TOTAL: ").append(sale.getString(sale.getColumnIndexOrThrow("total"))).append(" CFA");
//        }
//        sale.close();
//
//        txtFacture.setText(sb.toString());
//        facturestring = new StringBuilder(sb.toString());
//    }


}
