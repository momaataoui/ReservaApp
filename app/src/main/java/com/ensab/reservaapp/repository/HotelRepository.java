package com.ensab.reservaapp.repository;

import com.ensab.reservaapp.model.Hotel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HotelRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface HotelCallback {
        void onCallback(List<Hotel> hotels);
        void onError(Exception e);
    }

    public interface FavoritesCallback {
        void onCallback(List<String> favoriteIds);
        void onError(Exception e);
    }

    private com.google.firebase.firestore.ListenerRegistration favoritesListener;

    public void observeFavorites(FavoritesCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onCallback(new ArrayList<>());
            return;
        }

        if (favoritesListener != null) {
            favoritesListener.remove();
        }

        favoritesListener = db.collection("users").document(auth.getCurrentUser().getUid())
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        callback.onError(e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<String> favorites = (List<String>) documentSnapshot.get("favorites");
                        callback.onCallback(favorites != null ? favorites : new ArrayList<>());
                    } else {
                        callback.onCallback(new ArrayList<>());
                    }
                });
    }

    public void getAllHotels(HotelCallback callback) {
        db.collection("hotels").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Hotel> hotels = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Hotel hotel = doc.toObject(Hotel.class);
                    hotel.setId(doc.getId());
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

    public void getHotelsByIds(List<String> ids, HotelCallback callback) {
        if (ids == null || ids.isEmpty()) {
            callback.onCallback(new ArrayList<>());
            return;
        }

        List<String> limitedIds = ids.size() > 30 ? ids.subList(0, 30) : ids;

        db.collection("hotels")
                .whereIn(FieldPath.documentId(), limitedIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Hotel> fetchedHotels = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Hotel hotel = doc.toObject(Hotel.class);
                        hotel.setId(doc.getId());
                        if (doc.contains("price_per_night")) {
                            hotel.setPrice_per_night(doc.getDouble("price_per_night"));
                        }
                        fetchedHotels.add(hotel);
                    }
                    callback.onCallback(fetchedHotels);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getFavorites(FavoritesCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onCallback(new ArrayList<>());
            return;
        }

        db.collection("users").document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> favorites = (List<String>) documentSnapshot.get("favorites");
                        callback.onCallback(favorites != null ? favorites : new ArrayList<>());
                    } else {
                        callback.onCallback(new ArrayList<>());
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void toggleFavorite(String hotelId, boolean isFavorite, Runnable onSuccess, java.util.function.Consumer<Exception> onError) {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        if (isFavorite) {
            db.collection("users").document(userId)
                    .update("favorites", FieldValue.arrayRemove(hotelId))
                    .addOnSuccessListener(aVoid -> onSuccess.run())
                    .addOnFailureListener(onError::accept);
        } else {
            db.collection("users").document(userId)
                    .update("favorites", FieldValue.arrayUnion(hotelId))
                    .addOnSuccessListener(aVoid -> onSuccess.run())
                    .addOnFailureListener(e -> {
                        java.util.Map<String, Object> data = new java.util.HashMap<>();
                        data.put("favorites", java.util.Arrays.asList(hotelId));
                        db.collection("users").document(userId).set(data, com.google.firebase.firestore.SetOptions.merge())
                                .addOnSuccessListener(aVoid -> onSuccess.run())
                                .addOnFailureListener(onError::accept);
                    });
        }
    }
}
