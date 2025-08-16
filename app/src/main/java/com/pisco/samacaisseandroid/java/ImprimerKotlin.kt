package com.pisco.samacaisseandroid.java

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.pisco.samacaisseandroid.R

class ImprimerKotlin : AppCompatActivity() {

    private var printerConnection: BluetoothConnection? = null


    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            // No-op for example
        }
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_imprimer_kotlin)

        requestPermissions.launch(arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        ))

        val factureString = intent.getStringExtra("lafacture")

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Enable Bluetooth and retry", Toast.LENGTH_SHORT).show()
//            return@setOnClickListener
        }
        try {
            val printer = BluetoothPrintersConnections.selectFirstPaired()
            if (printer != null) {
                printerConnection = printer
                Toast.makeText(this, "Printer selected: ${'$'}{printer.address}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No paired printers found. Pair printer first.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error selecting printer: ${'$'}{e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }


        val connection = printerConnection
        if (connection == null) {
            Toast.makeText(this, "No printer connected", Toast.LENGTH_SHORT).show()
//            return@setOnClickListener
        }
        Thread {
            try {
                val printer = EscPosPrinter(connection, 203, 48f, 32)
                printer.printFormattedText(factureString)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }
}