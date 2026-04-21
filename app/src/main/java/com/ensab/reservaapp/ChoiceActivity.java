package com.ensab.reservaapp;

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
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseHelper = new FirebaseHelper();
        
        // Ensure some data exists in Firestore for the app to function
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

        if (cardHotels != null) {
            cardHotels.setOnClickListener(v -> {
                Toast.makeText(this, "Hotels Selected", Toast.LENGTH_SHORT).show();
            });
        }

        if (cardRestaurants != null) {
            cardRestaurants.setOnClickListener(v -> Toast.makeText(this, "Restaurants Selected", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupNavigation() {
        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> navigateToProfile());
        }

        View ivProfile = findViewById(R.id.ivProfile);
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> navigateToProfile());
        }

        View navSaved = findViewById(R.id.navSaved);
        if (navSaved != null) {
            navSaved.setOnClickListener(v -> Toast.makeText(this, "Wishlists coming soon", Toast.LENGTH_SHORT).show());
        }

        View navBookings = findViewById(R.id.navBookings);
        if (navBookings != null) {
            navBookings.setOnClickListener(v -> Toast.makeText(this, "Trips coming soon", Toast.LENGTH_SHORT).show());
        }
        
        View navDiscover = findViewById(R.id.navDiscover);
        if (navDiscover != null) {
            navDiscover.setOnClickListener(v -> {
                // Already on Discover
            });
        }
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_out);
    }
}