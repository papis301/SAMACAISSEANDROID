package com.pisco.samacaisseandroid.java;


import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pisco.samacaisseandroid.AppDbHelper;
import com.pisco.samacaisseandroid.MainActivity;
import com.pisco.samacaisseandroid.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;
    AppDbHelper dbHelper; // classe SQLite helper
    long userIdo = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        dbHelper = new AppDbHelper(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(username, password);
                }
            }
        });
    }

    private void loginUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT role FROM users WHERE username=? AND password=?",
                new String[]{username, password}
        );

        if (cursor.moveToFirst()) {
            String role = cursor.getString(0); // "admin" ou "employee"
            cursor.close();

            if (role.equalsIgnoreCase("admin")) {
                //Toast.makeText(this, "Admin ok", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(LoginActivity.this, AdminInterfaceActivity.class); // écran admin
                startActivity(intent);
                finish();
            } else {
                //Toast.makeText(this, "User ok", Toast.LENGTH_LONG).show();
                // ID de l'utilisateur connecté
                getUserId(username,password);


                String loginTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                 db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("user_id", userIdo);
                values.put("login_time", loginTime);

// Insérer la session
                long sessionId = db.insert("user_sessions", null, values);

// Stocker l'ID de session pour l'utiliser lors de la déconnexion
                SharedPreferences prefs = getSharedPreferences("session_prefs", MODE_PRIVATE);
                prefs.edit().putLong("current_session_id", sessionId).apply();
                Intent intent = new Intent(LoginActivity.this, CaisseActivity.class); // écran employé
                startActivity(intent);
                finish();
            }
        } else {
            cursor.close();
            Toast.makeText(this, "Identifiants incorrects", Toast.LENGTH_SHORT).show();
        }
    }

    // Java
    public int getUserId(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int userId = -1;

        Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE username=? AND password=?",
                new String[]{username, password}
        );

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            userIdo = userId;
           // Toast.makeText(this, "Identifiants "+userIdo, Toast.LENGTH_SHORT).show();

        }

        cursor.close();
        return userId;
    }

}
