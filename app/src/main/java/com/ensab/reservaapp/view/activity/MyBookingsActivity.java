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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensab.reservaapp.R;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.databinding.ActivityMyBookingsBinding;
import com.ensab.reservaapp.util.NavigationHelper;
import com.ensab.reservaapp.model.Booking;
import com.ensab.reservaapp.view.adapter.BookingAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyBookingsActivity extends AppCompatActivity {

    private ActivityMyBookingsBinding binding;
    private BookingAdapter adapter;
    private List<Booking> bookingList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.WHITE);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) controller.setAppearanceLightStatusBars(true);

        binding = ActivityMyBookingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Fix for navigation bar overlap
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.bottomNavContainer.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        bookingList = new ArrayList<>();

        binding.rvBookings.setLayoutManager(new LinearLayoutManager(this));
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
        
        binding.rvBookings.setAdapter(adapter);

        binding.btnExplore.setOnClickListener(v -> {
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
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("bookings").document(booking.getId())
            .update("status", "Cancelled")
            .addOnSuccessListener(aVoid -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Reservation cancelled", Toast.LENGTH_SHORT).show();
                loadBookings(); // Refresh list
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error cancelling reservation", Toast.LENGTH_SHORT).show();
            });
    }

    private void deleteBooking(Booking booking) {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("bookings").document(booking.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Trip removed", Toast.LENGTH_SHORT).show();
                loadBookings(); // Refresh list
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error removing trip", Toast.LENGTH_SHORT).show();
            });
    }

    private com.google.firebase.firestore.ListenerRegistration bookingsListener;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bookingsListener != null) {
            bookingsListener.remove();
        }
    }

    private void loadBookings() {
        if (mAuth.getCurrentUser() == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();

        if (bookingsListener != null) {
            bookingsListener.remove();
        }

        bookingsListener = db.collection("bookings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener((queryDocumentSnapshots, e) -> {
                binding.progressBar.setVisibility(View.GONE);
                
                if (e != null) {
                    Log.e("MyBookings", "Listen failed.", e);
                    Toast.makeText(this, "Error loading trips", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (queryDocumentSnapshots != null) {
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
                        } catch (Exception ex) {
                            Log.e("MyBookings", "Error parsing booking document: " + doc.getId(), ex);
                        }
                    }

                    if (bookingList.isEmpty()) {
                        binding.emptyState.setVisibility(View.VISIBLE);
                        binding.rvBookings.setVisibility(View.GONE);
                    } else {
                        binding.emptyState.setVisibility(View.GONE);
                        binding.rvBookings.setVisibility(View.VISIBLE);
                        adapter.updateBookings(bookingList);
                    }
                }
            });
    }

    private void setupNavigation() {
        binding.bottomNavContainer.findViewById(R.id.navDiscover).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ChoiceActivity.class));
        binding.bottomNavContainer.findViewById(R.id.navSaved).setOnClickListener(v -> NavigationHelper.fastNavigate(this, WishlistActivity.class));
        binding.bottomNavContainer.findViewById(R.id.navProfile).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ProfileActivity.class));
    }
}
