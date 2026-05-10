package com.ensab.reservaapp.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ensab.reservaapp.R;
import com.ensab.reservaapp.databinding.ActivityMyBookingsBinding;
import com.ensab.reservaapp.util.NavigationHelper;
import com.ensab.reservaapp.model.Booking;
import com.ensab.reservaapp.view.adapter.BookingAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * MyBookingsActivity permet à l'utilisateur de consulter l'historique de ses voyages
 * et l'état de ses réservations actuelles (En attente, Confirmée, Annulée).
 */
public class MyBookingsActivity extends AppCompatActivity {

    private ActivityMyBookingsBinding binding;
    private BookingAdapter adapter;
    private List<Booking> bookingList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private com.google.firebase.firestore.ListenerRegistration bookingsListener; // Pour écouter les changements en temps réel

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuration visuelle de la barre de statut
        Window window = getWindow();
        window.setStatusBarColor(Color.WHITE);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) controller.setAppearanceLightStatusBars(true);

        binding = ActivityMyBookingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Correction du chevauchement avec la barre de navigation Android
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.bottomNavContainer.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        bookingList = new ArrayList<>();

        // Configuration de la liste (RecyclerView)
        binding.rvBookings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(bookingList);
        
        // Gestion des actions (Annuler ou Supprimer une réservation)
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

        // Navigation vers l'accueil si la liste est vide (bouton Explore)
        binding.btnExplore.setOnClickListener(v -> {
            NavigationHelper.fastNavigate(this, ChoiceActivity.class);
        });

        NavigationHelper.setSelectedItem(this, R.id.navBookings);
        setupNavigation();
        loadBookings(); // Chargement dynamique des données
    }

    /**
     * Affiche une alerte avant d'annuler une réservation.
     */
    private void showCancelConfirmation(Booking booking) {
        new AlertDialog.Builder(this)
            .setTitle("Annuler la réservation")
            .setMessage("Voulez-vous vraiment annuler votre séjour à " + booking.getHotelName() + " ?")
            .setPositiveButton("Oui, Annuler", (dialog, which) -> cancelBooking(booking))
            .setNegativeButton("Non", null)
            .show();
    }

    /**
     * Affiche une alerte avant de supprimer une ligne de l'historique.
     */
    private void showDeleteConfirmation(Booking booking) {
        new AlertDialog.Builder(this)
            .setTitle("Supprimer du voyage")
            .setMessage("Voulez-vous retirer ce record de votre historique ?")
            .setPositiveButton("Supprimer", (dialog, which) -> deleteBooking(booking))
            .setNegativeButton("Annuler", null)
            .show();
    }

    /**
     * Met à jour le statut du voyage à 'Cancelled' dans Firestore.
     */
    private void cancelBooking(Booking booking) {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("bookings").document(booking.getId())
            .update("status", "Cancelled")
            .addOnSuccessListener(aVoid -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Réservation annulée", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Erreur lors de l'annulation", Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Supprime définitivement le document de réservation de Firestore.
     */
    private void deleteBooking(Booking booking) {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("bookings").document(booking.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Voyage supprimé", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Libération de l'écouteur Firestore pour éviter les fuites de mémoire
        if (bookingsListener != null) bookingsListener.remove();
    }

    /**
     * Charge les réservations de l'utilisateur connecté en temps réel.
     * Utilise un addSnapshotListener pour mettre à jour l'UI dès qu'un admin valide le séjour.
     */
    private void loadBookings() {
        if (mAuth.getCurrentUser() == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();

        if (bookingsListener != null) bookingsListener.remove();

        bookingsListener = db.collection("bookings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener((queryDocumentSnapshots, e) -> {
                binding.progressBar.setVisibility(View.GONE);
                
                if (e != null) {
                    Log.e("MyBookings", "Échec de l'écoute.", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    bookingList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            // Mapping manuel du document Firestore vers l'objet Java Booking
                            Booking booking = new Booking();
                            booking.setId(doc.getId());
                            booking.setHotelName(doc.getString("hotelName"));
                            booking.setHotelLocation(doc.getString("hotelLocation"));
                            booking.setHotelImageUrl(doc.getString("hotelImageUrl"));
                            booking.setStatus(doc.getString("status"));
                            
                            Long price = doc.getLong("totalPrice");
                            if (price != null) booking.setTotalPrice(price.intValue());

                            // Gestion des dates stockées en Long (timestamp)
                            Object ci = doc.get("checkIn");
                            if (ci instanceof Long) booking.setCheckIn((Long) ci);
                            
                            Object co = doc.get("checkOut");
                            if (co instanceof Long) booking.setCheckOut((Long) co);

                            bookingList.add(booking);
                        } catch (Exception ex) {
                            Log.e("MyBookings", "Erreur mapping document : " + doc.getId());
                        }
                    }

                    // Gestion de l'affichage "Vide"
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

    /**
     * Centralise la configuration de la barre de navigation du bas.
     */
    private void setupNavigation() {
        binding.bottomNavContainer.findViewById(R.id.navDiscover).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ChoiceActivity.class));
        binding.bottomNavContainer.findViewById(R.id.navSaved).setOnClickListener(v -> NavigationHelper.fastNavigate(this, WishlistActivity.class));
        binding.bottomNavContainer.findViewById(R.id.navProfile).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ProfileActivity.class));
    }
}
