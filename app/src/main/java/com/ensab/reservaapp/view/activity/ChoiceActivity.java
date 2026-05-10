package com.ensab.reservaapp.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.view.GravityCompat;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.data.FirebaseHelper;
import com.ensab.reservaapp.databinding.ActivityChoiceBinding;
import com.ensab.reservaapp.util.NavigationHelper;
/**
 * ChoiceActivity est l'écran d'accueil principal pour les clients.
 * Elle permet de choisir entre la recherche d'hôtels, de restaurants (coming soon) 
 * ou d'utiliser l'IA pour une recherche intelligente.
 */
public class ChoiceActivity extends AppCompatActivity {

    private ActivityChoiceBinding binding;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configuration de la barre de statut pour un look propre (texte noir sur fond blanc)
        Window window = getWindow();
        window.setStatusBarColor(Color.WHITE);
        WindowInsetsControllerCompat windowInsetsController = 
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }

        binding = ActivityChoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Ajustement du padding pour éviter que le contenu ne soit caché par les barres système
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseHelper = new FirebaseHelper();
        
        // Initialisation de données de test dans Firestore si la base est vide
        firebaseHelper.insertSampleDataIfEmpty(new FirebaseHelper.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d("Firebase", "Sample data initialized if it was empty");
            }

            @Override
            public void onFailure(String error) {
                Log.e("Firebase", "Error checking/initializing data: " + error);
            }
        });

        // Mise en évidence de l'icône "Discover" dans la barre de navigation basse
        NavigationHelper.setSelectedItem(this, R.id.navDiscover);
        
        setupCards();      // Configuration des clics sur les cartes principales
        setupNavigation(); // Configuration de la barre de navigation basse
        setupDrawer();     // Configuration du menu latéral
    }

    /**
     * Configure le menu latéral (Drawer) et ses actions de navigation.
     */
    private void setupDrawer() {
        // Ouvre le menu quand on clique sur l'icône "Hamburger"
        binding.ivMenu.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));

        // Gestion des clics sur les items du menu latéral
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Déjà sur l'accueil
            } else if (id == R.id.nav_hotels) {
                NavigationHelper.fastNavigate(this, HotelListActivity.class);
            } else if (id == R.id.nav_bookings) {
                NavigationHelper.fastNavigate(this, MyBookingsActivity.class);
            } else if (id == R.id.nav_wishlist) {
                NavigationHelper.fastNavigate(this, WishlistActivity.class);
            } else if (id == R.id.nav_help || id == R.id.nav_settings) {
                Toast.makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show();
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Mise à jour de l'en-tête du menu avec les infos de l'utilisateur connecté
        View headerView = binding.navigationView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.tvDrawerName);
        TextView tvEmail = headerView.findViewById(R.id.tvDrawerEmail);
        
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
        }
    }

    /**
     * Configure les actions sur les cartes centrales (Hôtels, Restaurants, IA).
     */
    private void setupCards() {
        binding.cardHotels.setOnClickListener(v -> NavigationHelper.fastNavigate(this, HotelListActivity.class));
        binding.cardRestaurants.setOnClickListener(v -> Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show());
        binding.cardSmartAI.setOnClickListener(v -> NavigationHelper.fastNavigate(this, SmartSearchActivity.class));
    }

    /**
     * Configure les boutons de la barre de navigation inférieure.
     */
    private void setupNavigation() {
        binding.bottomNav.getRoot().findViewById(R.id.navProfile).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ProfileActivity.class));
        binding.bottomNav.getRoot().findViewById(R.id.navSaved).setOnClickListener(v -> NavigationHelper.fastNavigate(this, WishlistActivity.class));
        binding.bottomNav.getRoot().findViewById(R.id.navBookings).setOnClickListener(v -> NavigationHelper.fastNavigate(this, MyBookingsActivity.class));
        binding.bottomNav.getRoot().findViewById(R.id.navDiscover).setOnClickListener(v -> {
            // Déjà sur l'onglet Découverte
        });
    }
}
