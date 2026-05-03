package com.ensab.reservaapp.repository;

import com.ensab.reservaapp.model.Hotel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * HotelRepository centralise l'accès aux données des hôtels et des interactions utilisateurs (favoris, réservations).
 * Utilise Firestore pour le stockage et Firebase Auth pour l'identification.
 */
public class HotelRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    /**
     * Interface de retour pour les listes d'hôtels.
     */
    public interface HotelCallback {
        void onCallback(List<Hotel> hotels);
        void onError(Exception e);
    }

    /**
     * Interface de retour pour les IDs des favoris.
     */
    public interface FavoritesCallback {
        void onCallback(List<String> favoriteIds);
        void onError(Exception e);
    }

    private com.google.firebase.firestore.ListenerRegistration favoritesListener;

    /**
     * Observe en temps réel les changements dans la liste des favoris de l'utilisateur.
     * Met à jour l'UI instantanément quand un hôtel est ajouté/supprimé des favoris.
     */
    public void observeFavorites(FavoritesCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onCallback(new ArrayList<>());
            return;
        }

        // Nettoyage de l'ancien listener pour éviter les fuites de mémoire
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
                        Object favs = documentSnapshot.get("favorites");
                        if (favs instanceof List) {
                            callback.onCallback((List<String>) favs);
                        } else {
                            callback.onCallback(new ArrayList<>());
                        }
                    } else {
                        callback.onCallback(new ArrayList<>());
                    }
                });
    }

    /**
     * Récupère une fois la liste des favoris (version non-observée).
     */
    public void getFavorites(FavoritesCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onCallback(new ArrayList<>());
            return;
        }

        db.collection("users").document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object favs = documentSnapshot.get("favorites");
                        if (favs instanceof List) {
                            callback.onCallback((List<String>) favs);
                        } else {
                            callback.onCallback(new ArrayList<>());
                        }
                    } else {
                        callback.onCallback(new ArrayList<>());
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Récupère tous les hôtels disponibles dans Firestore.
     */
    public void getAllHotels(HotelCallback callback) {
        db.collection("hotels").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Hotel> hotels = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Hotel hotel = doc.toObject(Hotel.class);
                    hotel.setId(doc.getId());
                    // Gestion de la compatibilité des noms de champs (price vs price_per_night)
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

    /**
     * Récupère une liste d'hôtels spécifique basée sur leurs IDs (utilisé pour la Wishlist).
     */
    public void getHotelsByIds(List<String> ids, HotelCallback callback) {
        if (ids == null || ids.isEmpty()) {
            callback.onCallback(new ArrayList<>());
            return;
        }

        // Limitation Firestore : 'whereIn' est limité à 10 ou 30 items
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

    /**
     * Récupère les détails d'un seul hôtel via son ID.
     */
    public void getHotelById(String id, java.util.function.Consumer<Hotel> onSuccess, java.util.function.Consumer<Exception> onError) {
        db.collection("hotels").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Hotel hotel = documentSnapshot.toObject(Hotel.class);
                        if (hotel != null) {
                            hotel.setId(documentSnapshot.getId());
                            if (documentSnapshot.contains("price_per_night")) {
                                hotel.setPrice_per_night(documentSnapshot.getDouble("price_per_night"));
                            }
                            onSuccess.accept(hotel);
                        } else {
                            onError.accept(new Exception("Failed to parse hotel"));
                        }
                    } else {
                        onError.accept(new Exception("Hotel not found"));
                    }
                })
                .addOnFailureListener(onError::accept);
    }

    /**
     * Ajoute ou supprime un hôtel de la liste des favoris de l'utilisateur.
     */
    public void toggleFavorite(String hotelId, boolean isFavorite, Runnable onSuccess, java.util.function.Consumer<Exception> onError) {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        if (isFavorite) {
            // Déjà en favori -> On le retire
            db.collection("users").document(userId)
                    .update("favorites", FieldValue.arrayRemove(hotelId))
                    .addOnSuccessListener(aVoid -> onSuccess.run())
                    .addOnFailureListener(onError::accept);
        } else {
            // Pas en favori -> On l'ajoute
            db.collection("users").document(userId)
                    .update("favorites", FieldValue.arrayUnion(hotelId))
                    .addOnSuccessListener(aVoid -> onSuccess.run())
                    .addOnFailureListener(e -> {
                        // Si le champ 'favorites' n'existe pas du tout, on le crée
                        java.util.Map<String, Object> data = new java.util.HashMap<>();
                        data.put("favorites", java.util.Arrays.asList(hotelId));
                        db.collection("users").document(userId).set(data, com.google.firebase.firestore.SetOptions.merge())
                                .addOnSuccessListener(aVoid -> onSuccess.run())
                                .addOnFailureListener(onError::accept);
                    });
        }
    }

    /**
     * Enregistre une nouvelle réservation dans la collection 'bookings'.
     */
    public void performBooking(java.util.Map<String, Object> bookingData, Runnable onSuccess, java.util.function.Consumer<Exception> onError) {
        db.collection("bookings")
                .add(bookingData)
                .addOnSuccessListener(documentReference -> onSuccess.run())
                .addOnFailureListener(onError::accept);
    }

    // --- Méthodes d'Administration ---

    public void addHotel(Hotel hotel, Runnable onSuccess, java.util.function.Consumer<Exception> onError) {
        db.collection("hotels")
                .add(hotel)
                .addOnSuccessListener(documentReference -> onSuccess.run())
                .addOnFailureListener(onError::accept);
    }

    public void updateHotel(Hotel hotel, Runnable onSuccess, java.util.function.Consumer<Exception> onError) {
        if (hotel.getId() == null) {
            onError.accept(new Exception("Hotel ID is missing"));
            return;
        }
        db.collection("hotels").document(hotel.getId())
                .set(hotel)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onError::accept);
    }

    public void deleteHotel(String hotelId, Runnable onSuccess, java.util.function.Consumer<Exception> onError) {
        db.collection("hotels").document(hotelId)
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onError::accept);
    }
}
