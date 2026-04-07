package com.ensab.reservaapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.google.android.material.card.MaterialCardView;

public class ChoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup Status Bar
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

        setupNavigation();
    }

    private void setupNavigation() {
        MaterialCardView cardHotels = findViewById(R.id.cardHotels);
        MaterialCardView cardRestaurants = findViewById(R.id.cardRestaurants);

        cardHotels.setOnClickListener(v -> Toast.makeText(this, "Hotels Selected", Toast.LENGTH_SHORT).show());
        cardRestaurants.setOnClickListener(v -> Toast.makeText(this, "Restaurants Selected", Toast.LENGTH_SHORT).show());

        // Navigate to Profile with Custom Fade & Scale Animation
        findViewById(R.id.navProfile).setOnClickListener(v -> navigateToProfile());
        findViewById(R.id.ivProfile).setOnClickListener(v -> navigateToProfile());
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        // Custom animation: Fade-in + Scale-up for the new activity
        overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_out);
    }
}