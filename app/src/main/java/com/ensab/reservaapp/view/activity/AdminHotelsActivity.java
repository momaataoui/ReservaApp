package com.ensab.reservaapp.view.activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.ensab.reservaapp.databinding.ActivityAdminHotelsBinding;
import com.ensab.reservaapp.model.Hotel;
import com.ensab.reservaapp.repository.HotelRepository;
import com.ensab.reservaapp.view.adapter.AdminHotelAdapter;
import com.ensab.reservaapp.view.adapter.AdminHotelAdapter;
import java.util.ArrayList;
import java.util.List;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

/**
 * AdminHotelsActivity permet à l'administrateur de voir la liste des hôtels
 * et d'accéder aux fonctionnalités d'ajout, de modification ou de suppression.
 */
public class AdminHotelsActivity extends AppCompatActivity {

    private ActivityAdminHotelsBinding binding;
    private HotelRepository hotelRepository;
    private AdminHotelAdapter adapter;
    private List<Hotel> hotelList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminHotelsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Gestion des insets pour la barre de statut
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        hotelRepository = new HotelRepository();

        setupRecyclerView();
        setupToolbar();
        
        // Bouton flottant pour ajouter un nouvel hôtel
        binding.fabAddHotel.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AdminAddEditHotelActivity.class);
            startActivity(intent);
        });

        loadHotels(); // Chargement initial
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Configure la liste des hôtels avec les actions Modifier/Supprimer.
     */
    private void setupRecyclerView() {
        adapter = new AdminHotelAdapter(hotelList, new AdminHotelAdapter.OnHotelActionListener() {
            @Override
            public void onEdit(Hotel hotel) {
                // Ouverture de l'écran d'édition avec l'ID de l'hôtel
                android.content.Intent intent = new android.content.Intent(AdminHotelsActivity.this, AdminAddEditHotelActivity.class);
                intent.putExtra("hotel_id", hotel.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Hotel hotel) {
                // Boîte de dialogue ou suppression directe
                deleteHotel(hotel);
            }
        });
        binding.rvAdminHotels.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminHotels.setAdapter(adapter);
    }

    /**
     * Récupère la liste des hôtels depuis Firestore.
     */
    private void loadHotels() {
        hotelRepository.getAllHotels(new HotelRepository.HotelCallback() {
            @Override
            public void onCallback(List<Hotel> hotels) {
                hotelList.clear();
                hotelList.addAll(hotels);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminHotelsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Supprime un hôtel de Firestore.
     */
    private void deleteHotel(Hotel hotel) {
        hotelRepository.deleteHotel(hotel.getId(), () -> {
            Toast.makeText(this, "Hotel deleted", Toast.LENGTH_SHORT).show();
            loadHotels(); // Recharger la liste après suppression
        }, e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
