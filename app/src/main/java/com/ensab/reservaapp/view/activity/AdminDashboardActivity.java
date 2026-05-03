package com.ensab.reservaapp.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ensab.reservaapp.databinding.ActivityAdminDashboardBinding;
import com.ensab.reservaapp.repository.AdminRepository;
import com.ensab.reservaapp.util.NavigationHelper;
import com.google.firebase.auth.FirebaseAuth;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import java.util.Map;

/**
 * AdminDashboardActivity est le point d'entrée principal pour les administrateurs.
 * Elle affiche un résumé des statistiques et permet de naviguer vers les modules de gestion.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private AdminRepository adminRepository;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        adminRepository = new AdminRepository();

        // Correction pour que le contenu ne passe pas sous la barre de statut (heure, batterie)
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupClickListeners();
        loadStats(); // Chargement initial des statistiques
    }

    /**
     * Configure les actions sur les boutons du tableau de bord.
     */
    private void setupClickListeners() {
        // Déconnexion
        binding.btnLogout.setOnClickListener(v -> logout());
        
        // Navigation vers la gestion des hôtels
        binding.btnManageHotels.setOnClickListener(v -> 
            NavigationHelper.fastNavigate(this, AdminHotelsActivity.class));
            
        // Navigation vers la gestion des réservations
        binding.btnManageBookings.setOnClickListener(v -> 
            NavigationHelper.fastNavigate(this, AdminReservationsActivity.class));
            
        // Navigation vers la gestion des utilisateurs
        binding.btnManageUsers.setOnClickListener(v -> 
            NavigationHelper.fastNavigate(this, AdminUsersActivity.class));

        // Bouton spécial permettant à l'admin de voir l'application comme un client
        binding.btnGoToClient.setOnClickListener(v -> 
            NavigationHelper.fastNavigate(this, ChoiceActivity.class));

        // Boutons secondaires avec messages temporaires
        binding.btnMenu.setOnClickListener(v -> 
            Toast.makeText(this, "Menu coming soon", Toast.LENGTH_SHORT).show());

        binding.btnNotifications.setOnClickListener(v -> 
            Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show());
    }

    /**
     * Charge les statistiques en temps réel depuis Firestore via le Repository.
     */
    private void loadStats() {
        adminRepository.getStats(new AdminRepository.AdminCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> stats) {
                // Affichage du nombre total de réservations
                binding.tvTotalBookings.setText(String.valueOf(stats.get("totalBookings")));
                
                // Affichage du revenu total formaté en DH
                double revenue = (double) stats.get("totalRevenue");
                binding.tvTotalRevenue.setText(String.format("%.0f DH", revenue));
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminDashboardActivity.this, "Error loading stats: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Déconnecte l'utilisateur de Firebase et réinitialise la session locale.
     */
    private void logout() {
        mAuth.signOut();
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
        // Retour à l'écran de connexion
        NavigationHelper.fastNavigate(this, LoginActivity.class, true);
    }
}
