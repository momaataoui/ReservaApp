package com.ensab.reservaapp.view.activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ensab.reservaapp.databinding.ActivityAdminAddEditHotelBinding;
import com.ensab.reservaapp.model.Hotel;
import com.ensab.reservaapp.repository.HotelRepository;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

/**
 * AdminAddEditHotelActivity est un écran polyvalent permettant soit de créer un nouvel hôtel,
 * soit de modifier un établissement existant dans Firestore.
 */
public class AdminAddEditHotelActivity extends AppCompatActivity {

    private ActivityAdminAddEditHotelBinding binding;
    private HotelRepository hotelRepository;
    private String hotelId;         // Stocke l'ID si on est en mode modification
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddEditHotelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ajustement pour la barre système
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        hotelRepository = new HotelRepository();
        
        // On vérifie si un ID a été passé dans l'Intent (signifie qu'on modifie un hôtel)
        hotelId = getIntent().getStringExtra("hotel_id");

        setupToolbar();

        if (hotelId != null) {
            isEditing = true;
            binding.toolbar.setTitle("Edit Hotel");
            loadHotelData(); // Pré-remplissage du formulaire
        } else {
            binding.toolbar.setTitle("Add Hotel");
        }

        binding.btnSave.setOnClickListener(v -> saveHotel());
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Charge les données actuelles de l'hôtel depuis Firestore pour modification.
     */
    private void loadHotelData() {
        hotelRepository.getHotelById(hotelId, hotel -> {
            binding.etHotelName.setText(hotel.getName());
            binding.etCity.setText(hotel.getCity());
            binding.etDescription.setText(hotel.getDescription());
            binding.etPrice.setText(String.valueOf(hotel.getPrice_per_night()));
            binding.etRating.setText(String.valueOf(hotel.getRating()));
            binding.etImageUrl.setText(hotel.getImageUrl());
        }, e -> Toast.makeText(this, "Loading error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Valide et enregistre les données saisies.
     */
    private void saveHotel() {
        String name = binding.etHotelName.getText().toString().trim();
        String city = binding.etCity.getText().toString().trim();
        String desc = binding.etDescription.getText().toString().trim();
        String priceStr = binding.etPrice.getText().toString().trim();
        String ratingStr = binding.etRating.getText().toString().trim();
        String imageUrl = binding.etImageUrl.getText().toString().trim();

        // Validation minimale
        if (name.isEmpty() || city.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Création de l'objet Hotel
        Hotel hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName(name);
        hotel.setCity(city);
        hotel.setDescription(desc);
        hotel.setPrice_per_night(Double.parseDouble(priceStr));
        hotel.setRating(ratingStr.isEmpty() ? 0 : Double.parseDouble(ratingStr));
        hotel.setImageUrl(imageUrl);

        // Envoi vers le Repository (Update ou Add)
        if (isEditing) {
            hotelRepository.updateHotel(hotel, () -> {
                Toast.makeText(this, "Hotel updated", Toast.LENGTH_SHORT).show();
                finish();
            }, e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            hotelRepository.addHotel(hotel, () -> {
                Toast.makeText(this, "Hotel added successfully", Toast.LENGTH_SHORT).show();
                finish();
            }, e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
