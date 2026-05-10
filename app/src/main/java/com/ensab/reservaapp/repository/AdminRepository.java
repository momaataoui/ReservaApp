package com.ensab.reservaapp.repository;

import com.ensab.reservaapp.model.Booking;
import com.ensab.reservaapp.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminRepository gère toutes les opérations de données pour le module d'administration.
 * Elle communique directement avec Firestore pour la gestion des utilisateurs,
 * des réservations et le calcul des statistiques.
 */
public class AdminRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Interface de callback générique pour les opérations asynchrones.
     */
    public interface AdminCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    // --- Gestion des Utilisateurs ---

    /**
     * Récupère la liste complète de tous les utilisateurs inscrits dans Firestore.
     */
    public void getAllUsers(AdminCallback<List<User>> callback) {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<User> users = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    User user = doc.toObject(User.class);
                    user.setId(doc.getId()); // On récupère l'ID du document Firestore
                    users.add(user);
                }
                callback.onSuccess(users);
            } else {
                callback.onError(task.getException());
            }
        });
    }

    /**
     * Met à jour le rôle d'un utilisateur (ex: basculer de 'client' à 'admin').
     */
    public void updateUserRole(String userId, String newRole, Runnable onSuccess, java.util.function.Consumer<Exception> onError) {
        db.collection("users").document(userId)
                .update("role", newRole)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onError::accept);
    }

    // --- Gestion des Réservations ---

    /**
     * Récupère toutes les réservations du système, triées par date d'arrivée décroissante.
     */
    public void getAllBookings(AdminCallback<List<Booking>> callback) {
        db.collection("bookings")
                .orderBy("checkIn", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Booking> bookings = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Booking booking = doc.toObject(Booking.class);
                            booking.setId(doc.getId());
                            bookings.add(booking);
                        }
                        callback.onSuccess(bookings);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    /**
     * Modifie l'état d'une réservation (ex: passer de 'pending' à 'confirmed').
     */
    public void updateBookingStatus(String bookingId, String newStatus, Runnable onSuccess, java.util.function.Consumer<Exception> onError) {
        db.collection("bookings").document(bookingId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onError::accept);
    }

    // --- Statistiques du Tableau de Bord ---

    /**
     * Calcule dynamiquement les statistiques globales pour le Dashboard Admin.
     * Calcule le nombre total de réservations, le revenu total (hors annulations),
     * et le nombre de réservations par hôtel.
     */
    public void getStats(AdminCallback<java.util.Map<String, Object>> callback) {
        db.collection("bookings").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                java.util.Map<String, Object> stats = new java.util.HashMap<>();
                int totalBookings = task.getResult().size();
                double totalRevenue = 0;
                java.util.Map<String, Integer> hotelCounts = new java.util.HashMap<>();

                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Booking b = doc.toObject(Booking.class);
                    // On ne compte le revenu que pour les réservations non annulées
                    if (!"cancelled".equalsIgnoreCase(b.getStatus())) {
                        totalRevenue += b.getTotalPrice();
                    }
                    String hotelName = b.getHotelName();
                    hotelCounts.put(hotelName, hotelCounts.getOrDefault(hotelName, 0) + 1);
                }

                stats.put("totalBookings", totalBookings);
                stats.put("totalRevenue", totalRevenue);
                stats.put("hotelCounts", hotelCounts);
                
                // On récupère aussi le nombre total d'utilisateurs inscrits
                db.collection("users").get().addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        stats.put("totalUsers", userTask.getResult().size());
                        callback.onSuccess(stats);
                    } else {
                        callback.onSuccess(stats); // On retourne les stats même si le compte user échoue
                    }
                });
            } else {
                callback.onError(task.getException());
            }
        });
    }
}
