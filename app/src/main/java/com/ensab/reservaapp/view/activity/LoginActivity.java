package com.ensab.reservaapp.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.ensab.reservaapp.R;
import com.ensab.reservaapp.databinding.ActivityLoginBinding;
import com.ensab.reservaapp.util.NavigationHelper;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * LoginActivity gère l'authentification des utilisateurs (Email/Mot de passe et Google).
 * Elle assure également la redirection vers le bon tableau de bord (Admin vs Client).
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    
    // Constantes pour la gestion de session locale
    private static final String SHARED_PREF_NAME = "user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        // Vérification si une session est déjà active
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) && currentUser != null) {
            checkUserRoleAndRedirect(currentUser);
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Correction du chevauchement avec la barre système
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Listeners pour les boutons de connexion
        binding.btnLogin.setOnClickListener(v -> handleEmailLogin());
        binding.btnGoogle.setOnClickListener(v -> signInWithGoogle());

        // Navigation vers Inscription et Mot de passe oublié
        binding.tvSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
        binding.tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    /**
     * Valide les champs et lance la connexion par Email.
     */
    private void handleEmailLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

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

    /**
     * Authentification Firebase Email/Password.
     */
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

    /**
     * Lance le flux de connexion Google via CredentialManager.
     */
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
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            });
    }

    /**
     * Lie le jeton Google ID à Firebase Auth.
     */
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

    /**
     * S'assure que l'utilisateur Google possède un document dans la collection 'users'.
     */
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
                        .addOnFailureListener(e -> checkUserRoleAndRedirect(user));
                } else {
                    checkUserRoleAndRedirect(user);
                }
            })
            .addOnFailureListener(e -> checkUserRoleAndRedirect(user));
    }

    /**
     * Vérifie le rôle de l'utilisateur dans Firestore et redirige.
     * Admin -> AdminDashboardActivity
     * Client -> ChoiceActivity
     */
    private void checkUserRoleAndRedirect(FirebaseUser user) {
        // S'assurer que les données de test existent avant de rediriger
        new com.ensab.reservaapp.data.FirebaseHelper().insertSampleDataIfEmpty(new com.ensab.reservaapp.data.FirebaseHelper.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String role = documentSnapshot.getString("role");
                        if ("admin".equalsIgnoreCase(role)) {
                            NavigationHelper.fastNavigate(LoginActivity.this, AdminDashboardActivity.class, true);
                        } else {
                            NavigationHelper.fastNavigate(LoginActivity.this, ChoiceActivity.class, true);
                        }
                    })
                    .addOnFailureListener(e -> NavigationHelper.fastNavigate(LoginActivity.this, ChoiceActivity.class, true));
            }

            @Override
            public void onFailure(String error) {
                // Même en cas d'échec d'insertion, on tente la redirection
                NavigationHelper.fastNavigate(LoginActivity.this, ChoiceActivity.class, true);
            }
        });
    }

    /**
     * Sauvegarde l'état de connexion localement.
     */
    private void saveSession(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }
}
