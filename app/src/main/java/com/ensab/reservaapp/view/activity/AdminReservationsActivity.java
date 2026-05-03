package com.ensab.reservaapp.view.activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.ensab.reservaapp.databinding.ActivityAdminReservationsBinding;
import com.ensab.reservaapp.model.Booking;
import com.ensab.reservaapp.repository.AdminRepository;
import com.ensab.reservaapp.view.adapter.AdminReservationAdapter;
import com.ensab.reservaapp.view.adapter.AdminReservationAdapter;
import java.util.ArrayList;
import java.util.List;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

/**
 * AdminReservationsActivity permet à l'administrateur de gérer les demandes de réservations.
 * C'est ici que l'admin valide (Confirme) ou rejette (Annule) les réservations en attente.
 */
public class AdminReservationsActivity extends AppCompatActivity {

    private ActivityAdminReservationsBinding binding;
    private AdminRepository adminRepository;
    private AdminReservationAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminReservationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Gestion du padding système
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        adminRepository = new AdminRepository();

        setupRecyclerView();
        setupToolbar();
        loadBookings(); // Récupération des réservations
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Configure la liste des réservations avec les actions métier.
     */
    private void setupRecyclerView() {
        adapter = new AdminReservationAdapter(bookingList, new AdminReservationAdapter.OnReservationActionListener() {
            @Override
            public void onConfirm(Booking booking) {
                // Passage du statut à "Confirmed"
                updateStatus(booking, "Confirmed");
            }

            @Override
            public void onCancel(Booking booking) {
                // Passage du statut à "Cancelled"
                updateStatus(booking, "Cancelled");
            }
        });
        binding.rvAdminReservations.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminReservations.setAdapter(adapter);
    }

    /**
     * Charge toutes les réservations triées par date.
     */
    private void loadBookings() {
        adminRepository.getAllBookings(new AdminRepository.AdminCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                bookingList.clear();
                bookingList.addAll(bookings);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminReservationsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Met à jour le statut dans Firestore et rafraîchit l'affichage.
     */
    private void updateStatus(Booking booking, String status) {
        adminRepository.updateBookingStatus(booking.getId(), status, () -> {
            Toast.makeText(this, "Status updated: " + status, Toast.LENGTH_SHORT).show();
            loadBookings(); // Rafraîchissement
        }, e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
