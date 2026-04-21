package com.ensab.reservaapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public interface Callback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    // --- Hotels ---
    public void getHotels(OnCompleteListener<QuerySnapshot> callback) {
        db.collection("hotels").get().addOnCompleteListener(callback);
    }

    // --- Restaurants ---
    public void getRestaurants(OnCompleteListener<QuerySnapshot> callback) {
        db.collection("restaurants").get().addOnCompleteListener(callback);
    }

    // --- Sample Data Insertion ---
    public void insertSampleDataIfEmpty(Callback<Void> callback) {
        db.collection("hotels").limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().isEmpty()) {
                // Insert Hotels
                insertHotel("Royal Mansour", "Marrakech", "Palatial hotel with riads and luxury spas.", 1200, 4.9);
                insertHotel("Sofitel Casablanca", "Casablanca", "Modern art-deco style with panoramic views.", 250, 4.5);

                // Insert Restaurants
                insertRestaurant("Le Grand Table Marocaine", "Marrakech", "Moroccan Haute Cuisine", "$$$$", 4.8);
                insertRestaurant("Rick's Café", "Casablanca", "International/Seafood", "$$$", 4.6);
                
                callback.onSuccess(null);
            } else if (!task.isSuccessful()) {
                callback.onFailure(task.getException().getMessage());
            }
        });
    }

    private void insertHotel(String name, String city, String desc, double price, double rating) {
        Map<String, Object> hotel = new HashMap<>();
        hotel.put("name", name);
        hotel.put("city", city);
        hotel.put("description", desc);
        hotel.put("price_per_night", price);
        hotel.put("rating", rating);
        db.collection("hotels").add(hotel);
    }

    private void insertRestaurant(String name, String city, String cuisine, String priceRange, double rating) {
        Map<String, Object> resto = new HashMap<>();
        resto.put("name", name);
        resto.put("city", city);
        resto.put("cuisine", cuisine);
        resto.put("price_range", priceRange);
        resto.put("rating", rating);
        db.collection("restaurants").add(resto);
    }
}
