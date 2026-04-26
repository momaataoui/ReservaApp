package com.ensab.reservaapp.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensab.reservaapp.R;
import com.ensab.reservaapp.data.NavigationHelper;
import com.ensab.reservaapp.model.Booking;
import com.ensab.reservaapp.view.adapter.BookingAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyBookingsActivity extends AppCompatActivity {

    private RecyclerView rvBookings;
    private BookingAdapter adapter;
    private List<Booking> bookingList;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.WHITE);
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(window.getDecorView());
        if (controller != null) controller.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_my_bookings);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        bookingList = new ArrayList<>();

        rvBookings = findViewById(R.id.rvBookings);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);

        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(bookingList);
        rvBookings.setAdapter(adapter);

        findViewById(R.id.btnExplore).setOnClickListener(v -> {
            NavigationHelper.fastNavigate(this, ChoiceActivity.class);
        });

        NavigationHelper.setSelectedItem(this, R.id.navBookings);
        setupNavigation();
        loadBookings();
    }

    private void loadBookings() {
        if (mAuth.getCurrentUser() == null) return;

        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("bookings")
            .whereEqualTo("userId", userId)
            .orderBy("checkIn", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                progressBar.setVisibility(View.GONE);
                bookingList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Booking booking = doc.toObject(Booking.class);
                    booking.setId(doc.getId());
                    bookingList.add(booking);
                }

                if (bookingList.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    rvBookings.setVisibility(View.GONE);
                } else {
                    emptyState.setVisibility(View.GONE);
                    rvBookings.setVisibility(View.VISIBLE);
                    adapter.updateBookings(bookingList);
                }
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Erreur lors du chargement des voyages", Toast.LENGTH_SHORT).show();
            });
    }

    private void setupNavigation() {
        findViewById(R.id.navDiscover).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ChoiceActivity.class));
        findViewById(R.id.navSaved).setOnClickListener(v -> NavigationHelper.fastNavigate(this, WishlistActivity.class));
        findViewById(R.id.navProfile).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ProfileActivity.class));
        findViewById(R.id.navBookings).setOnClickListener(v -> {
            // Already here
        });
    }
}
