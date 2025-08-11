package com.pisco.samacaisseandroid

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections

class ConnecteImprimantee : AppCompatActivity() {
    private var printerConnection: BluetoothConnection? = null

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            // No-op for example
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_connecte_imprimantee)

            requestPermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION
            ))

            val btnConnect = findViewById<Button>(R.id.btnConnect)
            val btnPrint = findViewById<Button>(R.id.btnPrint)

            btnConnect.setOnClickListener {
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                    Toast.makeText(this, "Enable Bluetooth and retry", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
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
            }

            btnPrint.setOnClickListener {
                val connection = printerConnection
                if (connection == null) {
                    Toast.makeText(this, "No printer connected", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                Thread {
                    try {
                        val printer = EscPosPrinter(connection, 203, 48f, 32)
                        printer.printFormattedText(
                            """[C]<b><font size='big'>TICKET</font></b>
                        [L]Produit A      [R]2000 CFA
                        [L]Produit B      [R]1500 CFA
                        [L]------------------------
                        [R]TOTAL : 3500 CFA
                        [C]Merci pour votre achat !
                        """
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            }
        }
}