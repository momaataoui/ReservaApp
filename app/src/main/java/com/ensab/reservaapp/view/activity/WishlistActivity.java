package com.ensab.reservaapp.view.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.data.NavigationHelper;
import com.ensab.reservaapp.model.Hotel;
import com.ensab.reservaapp.view.adapter.HotelAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private static final String TAG = "WishlistActivity";
    private RecyclerView rvWishlist;
    private HotelAdapter adapter;
    private List<Hotel> wishlistHotels;
    private LinearLayout emptyState;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        wishlistHotels = new ArrayList<>();

        rvWishlist = findViewById(R.id.rvWishlist);
        emptyState = findViewById(R.id.emptyState);

        rvWishlist.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HotelAdapter(wishlistHotels, this);
        rvWishlist.setAdapter(adapter);

        NavigationHelper.setSelectedItem(this, R.id.navSaved);
        setupNavigation();
        loadWishlist();
    }

    private void loadWishlist() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> favorites = (List<String>) documentSnapshot.get("favorites");
                    
                    if (favorites == null || favorites.isEmpty()) {
                        updateUI(new ArrayList<>());
                    } else {
                        fetchHotelsByIds(favorites);
                    }
                } else {
                    updateUI(new ArrayList<>());
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading favorites: " + e.getMessage());
                updateUI(new ArrayList<>());
            });
    }

    private void fetchHotelsByIds(List<String> ids) {
        // Firestore whereIn limit is 30.
        List<String> limitedIds = ids.size() > 30 ? ids.subList(0, 30) : ids;

        db.collection("hotels")
            .whereIn(FieldPath.documentId(), limitedIds)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Hotel> fetchedHotels = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        Hotel hotel = doc.toObject(Hotel.class);
                        hotel.setId(doc.getId());
                        
                        // Robust price handling
                        if (doc.contains("price_per_night")) {
                            Object priceObj = doc.get("price_per_night");
                            if (priceObj instanceof Number) {
                                hotel.setPrice_per_night(((Number) priceObj).doubleValue());
                            }
                        }
                        
                        fetchedHotels.add(hotel);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing hotel: " + e.getMessage());
                    }
                }
                updateUI(fetchedHotels);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching hotels: " + e.getMessage());
                updateUI(new ArrayList<>());
            });
    }

    private void updateUI(List<Hotel> hotels) {
        this.wishlistHotels = hotels;
        adapter.updateHotels(wishlistHotels);
        
        if (hotels.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvWishlist.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvWishlist.setVisibility(View.VISIBLE);
        }
    }

    private void setupNavigation() {
        findViewById(R.id.navDiscover).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ChoiceActivity.class));
        findViewById(R.id.navBookings).setOnClickListener(v -> NavigationHelper.fastNavigate(this, MyBookingsActivity.class));
        findViewById(R.id.navProfile).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ProfileActivity.class));
    }
}
