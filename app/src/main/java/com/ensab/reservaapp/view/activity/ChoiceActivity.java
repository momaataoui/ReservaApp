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
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.ensab.reservaapp.R;
import com.ensab.reservaapp.data.FirebaseHelper;
import com.ensab.reservaapp.data.NavigationHelper;
import com.google.android.material.card.MaterialCardView;

public class ChoiceActivity extends AppCompatActivity {

    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Window window = getWindow();
        window.setStatusBarColor(Color.WHITE);
        WindowInsetsControllerCompat windowInsetsController = 
                ViewCompat.getWindowInsetsController(window.getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }

        setContentView(R.layout.activity_choice);
        
        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });
        }

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
        MaterialCardView cardHotels = findViewById(R.id.cardHotels);
        MaterialCardView cardRestaurants = findViewById(R.id.cardRestaurants);
        MaterialCardView cardSmartAI = findViewById(R.id.cardSmartAI);

        if (cardHotels != null) {
            cardHotels.setOnClickListener(v -> NavigationHelper.fastNavigate(this, HotelListActivity.class));
        }

        if (cardRestaurants != null) {
            cardRestaurants.setOnClickListener(v -> Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show());
        }

        if (cardSmartAI != null) {
            cardSmartAI.setOnClickListener(v -> NavigationHelper.fastNavigate(this, SmartSearchActivity.class));
        }
    }

    private void setupNavigation() {
        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> NavigationHelper.fastNavigate(this, ProfileActivity.class));
        }

        View navSaved = findViewById(R.id.navSaved);
        if (navSaved != null) {
            navSaved.setOnClickListener(v -> NavigationHelper.fastNavigate(this, WishlistActivity.class));
        }

        View navBookings = findViewById(R.id.navBookings);
        if (navBookings != null) {
            navBookings.setOnClickListener(v -> NavigationHelper.fastNavigate(this, MyBookingsActivity.class));
        }
        
        View navDiscover = findViewById(R.id.navDiscover);
        if (navDiscover != null) {
            navDiscover.setOnClickListener(v -> {
                // Already on Discover
            });
        }
    }
}
