package com.pisco.samacaisseandroid.java;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pisco.samacaisseandroid.AppDbHelper;
import com.pisco.samacaisseandroid.R;
import com.pisco.samacaisseandroid.UserHistoryActivity;
import com.pisco.samacaisseandroid.UserManagementActivity;
import com.pisco.samacaisseandroid.ui.ClientManagementActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminInterfaceActivity extends AppCompatActivity {

    Button btnUsers, btnProducts, btnClients, btnLogout, btnHistory, btnfour,
            btnachat, btncompta, btnSubscribe;
    private AppDbHelper dbHelper;
    private FirebaseFirestore db;
    private String tel, nomentreprise, adresse;
    TextView tvCompanyName, tvCompanyAddress, tvCompanyPhone;
    //FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final int RC_SIGN_IN = 1001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_interface);

        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvCompanyAddress = findViewById(R.id.tvCompanyAddress);
        tvCompanyPhone = findViewById(R.id.tvCompanyPhone);
        // ✅ Initialisation DB
        dbHelper = new AppDbHelper(this);

        btnUsers = findViewById(R.id.btnUsers);
        btnProducts = findViewById(R.id.btnProducts);
        btnClients = findViewById(R.id.btnClients);
        btnHistory = findViewById(R.id.historiqueuser);
        btnLogout = findViewById(R.id.btnLogout);
        btnfour = findViewById(R.id.btnfournisseu);
        btnachat = findViewById(R.id.btnachats);
        btncompta = findViewById(R.id.compta);
        btnSubscribe = findViewById(R.id.btnSubscribe);
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();

// Configurer Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // récupéré depuis google-services.json
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnSubscribe.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                // Pas connecté → lancer Google Sign-In
                signIn();
            } else {
                // Déjà connecté → enregistrer abonnement
                saveSubscription(currentUser);
            }
        });


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            checkIfUserSubscribed(currentUser, btnSubscribe);
        }

        checkMonthlyPayment(currentUser);

        //btnSubscribe.setOnClickListener(v -> saveSubscription(user));

        // Vérifier si l'entreprise existe
        Cursor cursor = dbHelper.getReadableDatabase()
                .rawQuery("SELECT name, address, phone FROM company LIMIT 1", null);

        if (cursor != null && cursor.moveToFirst()) {
            String companyName = cursor.getString(0);
            String companyAddress = cursor.getString(1);
            String companyPhone = cursor.getString(2);

            Toast.makeText(this,
                    "Entreprise : " + companyName + "\nAdresse : " + companyAddress + "\nTéléphone : " + companyPhone,
                    Toast.LENGTH_LONG).show();

            cursor.close();
        } else {
            // Redirection si l’entreprise n’est pas encore définie
            Intent intent = new Intent(this, AddCompanyActivity.class);
            startActivity(intent);
            finish();
        }

        Cursor cursor1 = dbHelper.getCompany();
        if (cursor1 != null && cursor1.moveToFirst()) {
            String name = cursor1.getString(cursor1.getColumnIndexOrThrow("name"));
            String address = cursor1.getString(cursor1.getColumnIndexOrThrow("address"));
            String phone = cursor1.getString(cursor1.getColumnIndexOrThrow("phone"));

            tel = phone;
            nomentreprise = name;
            adresse = address;

            tvCompanyName.setText(name);
            tvCompanyAddress.setText("Adresse : " + address);
            tvCompanyPhone.setText("Téléphone : " + phone);
        } else {
            tvCompanyName.setText("Aucune entreprise définie");
            tvCompanyAddress.setText("");
            tvCompanyPhone.setText("");
        }

        btncompta.setOnClickListener(v -> startActivity(new Intent(AdminInterfaceActivity.this, SalesPurchasesActivity.class)));


        // Redirection vers Gestion achats
        btnachat.setOnClickListener(v -> startActivity(new Intent(AdminInterfaceActivity.this, AchatsListeActivity.class)));

        // Redirection vers Gestion fournisseurs
        btnfour.setOnClickListener(v -> startActivity(new Intent(AdminInterfaceActivity.this, FournisseurListActivity.class)));

        // Redirection vers Gestion Utilisateurs
        btnUsers.setOnClickListener(v -> startActivity(new Intent(AdminInterfaceActivity.this, UserManagementActivity.class)));

        // Redirection vers Gestion Produits
        btnProducts.setOnClickListener(v -> startActivity(new Intent(AdminInterfaceActivity.this, ManageProductsActivity.class)));

        // Redirection vers Gestion Clients
        btnClients.setOnClickListener(v -> startActivity(new Intent(AdminInterfaceActivity.this, ClientManagementActivity.class)));

        // Redirection historique utilisateurs
        btnHistory.setOnClickListener(v -> startActivity(new Intent(AdminInterfaceActivity.this, UserHistoryActivity.class)));

        // Déconnexion
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminInterfaceActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }



//    private void saveSubscription(FirebaseUser user) {
//        if (tel == null || tel.trim().isEmpty()) {
//            Toast.makeText(this, "⚠️ Impossible d’enregistrer : numéro de téléphone admin vide.", Toast.LENGTH_LONG).show();
//            return; // Stop exécution
//        }
//        String uid = user.getUid(); // ID unique Firebase Auth
//        String email = user.getEmail();
//
//        String currentMonth = new SimpleDateFormat("MM", Locale.getDefault()).format(new Date());
//        String currentYear = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
//
//        Map<String, Object> paiement = new HashMap<>();
//        paiement.put("userId", uid);
//        paiement.put("email", email);
//        paiement.put("mois", currentMonth);
//        paiement.put("annee", currentYear);
//        paiement.put("status", true);
//        paiement.put("telephone", tel);
//
//        // 🔥 Ici on force l’ID du document = UID Firebase
//        db.collection("paiements")
//                .document(uid)
//                .set(paiement)
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(this, "Abonnement enregistré ✅", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
//                    Log.d("erreur firebase", e.getMessage());
//                });
//    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Échec Google Sign-In : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(this, "Connecté : " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            // 🔥 Initialise les 12 mois dès la première inscription
                            initializeMonthsForNewUser(db, user, tel);
                            Intent intent = new Intent(this, AdminInterfaceActivity.class);
                            startActivity(intent);
                            finish();

                        }
                    } else {
                        Toast.makeText(this, "Échec connexion Firebase", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkIfUserSubscribed(FirebaseUser user, Button btnSAbonner) {
        if (user == null) return;

        String email = user.getEmail();

        db.collection("paiements")
                .whereEqualTo("email", email) // 🔍 Vérifie si l'email existe déjà
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // ✅ L'utilisateur existe déjà → on grise le bouton
                        btnSAbonner.setEnabled(false);
                        btnSAbonner.setText("Déjà abonné");

                        updateAdminButtons(true);
                    } else {
                        // ✅ Pas encore abonné → bouton actif
                        btnSAbonner.setEnabled(true);
                        btnSAbonner.setText("S’abonner");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur Firestore : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateAdminButtons(boolean isSubscribed) {
        btnHistory.setEnabled(isSubscribed);
        btnfour.setEnabled(isSubscribed);
        btnachat.setEnabled(isSubscribed);
        btncompta.setEnabled(isSubscribed);
    }

    /**
     * Création des 12 mois avec statut "non payé" (à appeler lors de l’inscription)
     */
    @SuppressLint("DefaultLocale")
    public static void initializeMonthsForNewUser(FirebaseFirestore db, FirebaseUser user, String telephone) {
        if (user == null) return;

        String uid = user.getUid();
        String email = user.getEmail();

        Map<String, Object> mois = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("status", false); // non payé par défaut
            mois.put(String.format("%02d", i), data);
        }

        Map<String, Object> paiement = new HashMap<>();
        paiement.put("uid", uid);
        paiement.put("email", email);
        paiement.put("telephone", telephone);
        paiement.put("mois", mois);

        db.collection("paiements").document(uid).set(paiement);

    }


    private void markMonthAsPaid(FirebaseUser user) {
        String uid = user.getUid();
        String email = user.getEmail();
        String currentMonth = new SimpleDateFormat("MM", Locale.getDefault()).format(new Date());

        // Vérifier si email existe déjà
        db.collection("paiements")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // ✅ Email existe déjà
                        Toast.makeText(this, "Cet email est déjà enregistré comme abonné", Toast.LENGTH_LONG).show();
                        btnSubscribe.setEnabled(false);
                        btnSubscribe.setText("Déjà abonné");
                    } else {
                        // ❌ Pas encore abonné → enregistrer paiement
                        db.collection("paiements").document(uid)
                                .update("mois." + currentMonth + ".status", true,
                                        "email", email) // on ajoute l'email si pas encore mis
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Paiement validé pour le mois " + currentMonth, Toast.LENGTH_SHORT).show();
                                    btnSubscribe.setEnabled(false);
                                    btnSubscribe.setText("Déjà abonné");
                                    updateAdminButtons(true);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Erreur mise à jour : " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur Firestore : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private void saveSubscription(FirebaseUser user) {
        if (tel == null || tel.trim().isEmpty()) {
            Toast.makeText(this, "⚠️ Numéro de téléphone vide.", Toast.LENGTH_LONG).show();
            return;
        }
        String uid = user.getUid();
        String email = user.getEmail();
        String currentMonth = new SimpleDateFormat("MM", Locale.getDefault()).format(new Date());

        db.collection("paiements")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // ✅ Email déjà abonné
                        Toast.makeText(this, "Cet email est déjà enregistré comme abonné", Toast.LENGTH_LONG).show();
                        btnSubscribe.setEnabled(false);
                        btnSubscribe.setText("Déjà abonné");
                    } else {
                        // ❌ Nouvel abonné → mise à jour mois courant
                        db.collection("paiements").document(uid)
                                .update("mois." + currentMonth + ".status", true,
                                        "email", email,
                                        "telephone", tel)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Abonnement enregistré ✅", Toast.LENGTH_SHORT).show();
                                    btnSubscribe.setEnabled(false);
                                    btnSubscribe.setText("Déjà abonné");
                                    updateAdminButtons(true);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Erreur Firestore : " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                });
    }

    private void checkMonthlyPayment(FirebaseUser user) {
        if (user == null) return;

        String uid = user.getUid();
        String currentDay = new SimpleDateFormat("dd", Locale.getDefault()).format(new Date());
        String currentMonth = new SimpleDateFormat("MM", Locale.getDefault()).format(new Date());

        // On ne fait le check que le 1er du mois
        //if (!currentDay.equals("01")) return;

        db.collection("paiements")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> moisMap = (Map<String, Object>) documentSnapshot.get("mois");
                        if (moisMap != null) {
                            Map<String, Object> monthData = (Map<String, Object>) moisMap.get(currentMonth);
                            if (monthData != null && monthData.containsKey("status")) {
                                boolean isPaid = (boolean) monthData.get("status");
                                if (!isPaid) {
                                    // ❌ Mois non payé
                                    Toast.makeText(this, "⚠️ Abonnement non payé pour ce mois !", Toast.LENGTH_LONG).show();
                                    disableFeaturesDueToNonPayment();
                                } else {
                                    // ✅ Mois payé
                                    enableFeatures();
                                }
                            }
                        }
                    } else {
                        // Pas encore d'enregistrement pour l'utilisateur
                        Toast.makeText(this, "⚠️ Vous n'avez pas d'abonnement actif !", Toast.LENGTH_LONG).show();
                        disableFeaturesDueToNonPayment();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur vérification paiement : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Méthodes pour activer/désactiver les fonctionnalités
    private void disableFeaturesDueToNonPayment() {

        btnHistory.setEnabled(false);
        btnHistory.setBackgroundColor(getResources().getColor(R.color.gray)); // Couleur grise

        btnfour.setEnabled(false);
        btnfour.setBackgroundColor(getResources().getColor(R.color.gray));

        btnachat.setEnabled(false);
        btnachat.setBackgroundColor(getResources().getColor(R.color.gray));

        btncompta.setEnabled(false);
        btncompta.setBackgroundColor(getResources().getColor(R.color.gray));

        btnSubscribe.setEnabled(true);
        btnSubscribe.setBackgroundColor(getResources().getColor(R.color.purple_500)); // Couleur normale
    }

    private void enableFeatures() {
        btnHistory.setEnabled(true);
        btnfour.setEnabled(true);
        btnachat.setEnabled(true);
        btncompta.setEnabled(true);
        btnSubscribe.setEnabled(false);
        btnSubscribe.setText("Déjà abonné");
    }

}
