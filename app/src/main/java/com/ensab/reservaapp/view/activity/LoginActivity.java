package com.ensab.reservaapp.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.ensab.reservaapp.R;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private MaterialCardView btnGoogle;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) && currentUser != null) {
            checkUserRoleAndRedirect(currentUser);
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        TextView tvSignUp = findViewById(R.id.tvSignUp);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> handleEmailLogin());
        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        tvSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void handleEmailLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer l'email et le mot de passe", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Veuillez entrer un email valide", Toast.LENGTH_SHORT).show();
            return;
        }
        loginUser(email, password);
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        saveSession(email);
                        checkUserRoleAndRedirect(user);
                    }
                } else {
                    Toast.makeText(this, "Échec : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void signInWithGoogle() {
        CredentialManager credentialManager = CredentialManager.create(this);
        String clientId = getString(R.string.default_web_client_id);
        
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        Log.d(TAG, "Lancement de la connexion Google...");

        credentialManager.getCredentialAsync(this, request, null, Runnable::run, 
            new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                @Override
                public void onResult(GetCredentialResponse result) {
                    Credential credential = result.getCredential();
                    try {
                        GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                        firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur credential: " + e.getMessage());
                    }
                }
                @Override
                public void onError(@NonNull GetCredentialException e) {
                    Log.e(TAG, "ERREUR GOOGLE: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        saveSession(user.getEmail());
                        createUserInFirestoreIfNotExist(user);
                    }
                } else {
                    Toast.makeText(this, "Erreur Firebase: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void createUserInFirestoreIfNotExist(FirebaseUser user) {
        db.collection("users").document(user.getUid()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("fullName", user.getDisplayName());
                    userData.put("email", user.getEmail());
                    userData.put("role", "user");
                    userData.put("profileImage", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");

                    db.collection("users").document(user.getUid()).set(userData)
                        .addOnSuccessListener(aVoid -> checkUserRoleAndRedirect(user))
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Erreur création Firestore: " + e.getMessage());
                            checkUserRoleAndRedirect(user);
                        });
                } else {
                    checkUserRoleAndRedirect(user);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lecture Firestore: " + e.getMessage());
                checkUserRoleAndRedirect(user);
            });
    }

    private void checkUserRoleAndRedirect(FirebaseUser user) {
        db.collection("users").document(user.getUid()).get()
            .addOnSuccessListener(documentSnapshot -> {
                String role = documentSnapshot.getString("role");
                if ("admin".equals(role)) {
                    // Pour le moment, redirection vers ChoiceActivity car admin n'est pas implémenté
                    // Mais on loggue l'info pour préparer l'avenir
                    Log.d(TAG, "Admin connecté !");
                    startActivity(new Intent(this, ChoiceActivity.class));
                } else {
                    startActivity(new Intent(this, ChoiceActivity.class));
                }
                finish();
            })
            .addOnFailureListener(e -> {
                startActivity(new Intent(this, ChoiceActivity.class));
                finish();
            });
    }

    private void saveSession(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }
}
