package com.ensab.reservaapp.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.ensab.reservaapp.R;
import com.ensab.reservaapp.data.FirebaseHelper;
import com.ensab.reservaapp.databinding.ActivityChoiceBinding;
import com.ensab.reservaapp.util.NavigationHelper;
import com.google.android.material.card.MaterialCardView;

public class ChoiceActivity extends AppCompatActivity {

    private ActivityChoiceBinding binding;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Window window = getWindow();
        window.setStatusBarColor(Color.WHITE);
        WindowInsetsControllerCompat windowInsetsController = 
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }

        binding = ActivityChoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseHelper = new FirebaseHelper();
        
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

        NavigationHelper.setSelectedItem(this, R.id.navDiscover);
        setupCards();
        setupNavigation();
    }

    private void setupCards() {
        binding.cardHotels.setOnClickListener(v -> NavigationHelper.fastNavigate(this, HotelListActivity.class));
        binding.cardRestaurants.setOnClickListener(v -> Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show());
        binding.cardSmartAI.setOnClickListener(v -> NavigationHelper.fastNavigate(this, SmartSearchActivity.class));
    }

    private void setupNavigation() {
        binding.bottomNav.getRoot().findViewById(R.id.navProfile).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ProfileActivity.class));
        binding.bottomNav.getRoot().findViewById(R.id.navSaved).setOnClickListener(v -> NavigationHelper.fastNavigate(this, WishlistActivity.class));
        binding.bottomNav.getRoot().findViewById(R.id.navBookings).setOnClickListener(v -> NavigationHelper.fastNavigate(this, MyBookingsActivity.class));
        binding.bottomNav.getRoot().findViewById(R.id.navDiscover).setOnClickListener(v -> {
            // Already on Discover
        });
    }
}
