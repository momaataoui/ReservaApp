package com.ensab.reservaapp.repository;

import com.ensab.reservaapp.model.Hotel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HotelRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface HotelCallback {
        void onCallback(List<Hotel> hotels);
        void onError(Exception e);
    }

    public void getAllHotels(HotelCallback callback) {
        db.collection("hotels").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Hotel> hotels = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Hotel hotel = doc.toObject(Hotel.class);
                    hotel.setId(doc.getId());
                    // Support for both naming conventions
                    if (doc.contains("price_per_night")) {
                        hotel.setPrice_per_night(doc.getDouble("price_per_night"));
                    } else if (doc.contains("price")) {
                        hotel.setPrice_per_night(doc.getDouble("price"));
                    }
                    hotels.add(hotel);
                }
                callback.onCallback(hotels);
            } else {
                callback.onError(task.getException());
            }
        });
    }
}