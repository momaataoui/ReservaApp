package com.ensab.reservaapp.view.activity;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.ensab.reservaapp.databinding.ActivitySignupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

/**
 * SignUpActivity gère la création de nouveaux comptes utilisateurs.
 * Elle enregistre les identifiants dans Firebase Auth et les informations de profil dans Firestore.
 */
public class SignUpActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Correction du chevauchement avec la barre système
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Gestion du clic sur le bouton S'inscrire
        binding.btnSignUp.setOnClickListener(v -> {
            String fullName = binding.etFullName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            // Validation basique des champs
            if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Veuillez entrer un email valide", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(fullName, email, phone, password);
            }
        });

        // Retour à l'écran de connexion
        binding.tvLogin.setOnClickListener(v -> finish());
    }

    /**
     * Crée l'utilisateur dans Firebase Auth et envoie un mail de vérification.
     */
    private void registerUser(String fullName, String email, String phone, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Envoi systématique d'un mail de vérification pour sécuriser l'accès
                        user.sendEmailVerification()
                            .addOnCompleteListener(verifyTask -> {
                                if (verifyTask.isSuccessful()) {
                                    saveUserToFirestore(user.getUid(), fullName, email, phone);
                                } else {
                                    Toast.makeText(this, "Erreur lors de l'envoi du mail de vérification.", Toast.LENGTH_SHORT).show();
                                }
                            });
                    }
                } else {
                    Toast.makeText(this, "Échec de l'authentification : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    /**
     * Enregistre les informations complémentaires (nom, téléphone, rôle) dans Firestore.
     * Par défaut, tout nouvel inscrit a le rôle 'client'.
     */
    private void saveUserToFirestore(String userId, String fullName, String email, String phone) {
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("email", email);
        user.put("phone", phone);
        user.put("role", "client"); // Rôle par défaut
        user.put("profileImage", "");

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Inscription réussie ! Veuillez vérifier votre email.", Toast.LENGTH_LONG).show();
                mAuth.signOut(); // On déconnecte pour forcer la connexion après vérification
                finish();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Erreur Firestore : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}