package com.ensab.reservaapp.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
        
        adapter.setOnBookingActionListener(new BookingAdapter.OnBookingActionListener() {
            @Override
            public void onCancel(Booking booking) {
                showCancelConfirmation(booking);
            }

            @Override
            public void onDelete(Booking booking) {
                showDeleteConfirmation(booking);
            }
        });
        
        rvBookings.setAdapter(adapter);

        findViewById(R.id.btnExplore).setOnClickListener(v -> {
            NavigationHelper.fastNavigate(this, ChoiceActivity.class);
        });

        NavigationHelper.setSelectedItem(this, R.id.navBookings);
        setupNavigation();
        loadBookings();
    }

    private void showCancelConfirmation(Booking booking) {
        new AlertDialog.Builder(this)
            .setTitle("Cancel Reservation")
            .setMessage("Are you sure you want to cancel your reservation at " + booking.getHotelName() + "?")
            .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                cancelBooking(booking);
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void showDeleteConfirmation(Booking booking) {
        new AlertDialog.Builder(this)
            .setTitle("Remove Trip")
            .setMessage("Are you sure you want to remove this trip record from your history?")
            .setPositiveButton("Remove", (dialog, which) -> {
                deleteBooking(booking);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void cancelBooking(Booking booking) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("bookings").document(booking.getId())
            .update("status", "Cancelled")
            .addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Reservation cancelled", Toast.LENGTH_SHORT).show();
                loadBookings(); // Refresh list
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error cancelling reservation", Toast.LENGTH_SHORT).show();
            });
    }

    private void deleteBooking(Booking booking) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("bookings").document(booking.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Trip removed", Toast.LENGTH_SHORT).show();
                loadBookings(); // Refresh list
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error removing trip", Toast.LENGTH_SHORT).show();
            });
    }

    private void loadBookings() {
        if (mAuth.getCurrentUser() == null) return;

        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("bookings")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                progressBar.setVisibility(View.GONE);
                bookingList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        Booking booking = new Booking();
                        booking.setId(doc.getId());
                        booking.setUserId(doc.getString("userId"));
                        booking.setHotelId(doc.getString("hotelId"));
                        booking.setHotelName(doc.getString("hotelName"));
                        booking.setHotelLocation(doc.getString("hotelLocation"));
                        booking.setHotelImageUrl(doc.getString("hotelImageUrl"));
                        booking.setStatus(doc.getString("status"));
                        
                        Long totalPrice = doc.getLong("totalPrice");
                        if (totalPrice != null) booking.setTotalPrice(totalPrice.intValue());

                        Long adults = doc.getLong("adults");
                        if (adults != null) booking.setAdults(adults.intValue());

                        Long children = doc.getLong("children");
                        if (children != null) booking.setChildren(children.intValue());

                        Object ci = doc.get("checkIn");
                        if (ci instanceof Long) {
                            booking.setCheckIn((Long) ci);
                        } else if (ci instanceof com.google.firebase.Timestamp) {
                            booking.setCheckIn(((com.google.firebase.Timestamp) ci).toDate().getTime());
                        }

                        Object co = doc.get("checkOut");
                        if (co instanceof Long) {
                            booking.setCheckOut((Long) co);
                        } else if (co instanceof com.google.firebase.Timestamp) {
                            booking.setCheckOut(((com.google.firebase.Timestamp) co).toDate().getTime());
                        }

                        bookingList.add(booking);
                    } catch (Exception e) {
                        Log.e("MyBookings", "Error parsing booking document: " + doc.getId(), e);
                    }
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
                Toast.makeText(this, "Error loading trips", Toast.LENGTH_SHORT).show();
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
