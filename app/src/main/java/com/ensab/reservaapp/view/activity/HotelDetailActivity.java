package com.ensab.reservaapp.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager2.widget.ViewPager2;

import androidx.lifecycle.ViewModelProvider;
import com.ensab.reservaapp.viewmodel.HotelDetailViewModel;
import com.ensab.reservaapp.model.Hotel;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.databinding.ActivityHotelDetailBinding;
import com.ensab.reservaapp.util.NavigationHelper;
import com.ensab.reservaapp.view.adapter.HotelImageAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * HotelDetailActivity affiche les informations détaillées d'un établissement.
 * Elle permet de visualiser la galerie d'images, de choisir des dates de séjour,
 * de calculer le prix total et d'initier une réservation.
 */
public class HotelDetailActivity extends AppCompatActivity {

    // Clés pour le passage de données entre activités via Intent
    public static final String EXTRA_HOTEL_ID       = "hotelId";
    public static final String EXTRA_HOTEL_NAME     = "hotel_name";
    public static final String EXTRA_HOTEL_LOCATION = "hotel_location";
    public static final String EXTRA_HOTEL_PRICE    = "hotel_price";
    public static final String EXTRA_HOTEL_RATING   = "hotel_rating";
    public static final String EXTRA_HOTEL_REVIEWS  = "hotel_reviews";
    public static final String EXTRA_HOTEL_DESC     = "hotel_description";
    public static final String EXTRA_HOTEL_IMAGE_URL = "hotel_image_url";
    public static final String EXTRA_HOTEL_IMAGES   = "hotel_images";

    private ActivityHotelDetailBinding binding;
    private HotelDetailViewModel viewModel;
    private String hotelId;
    private String hotelImageUrl;
    private int pricePerNight = 1200;
    private int currentTotalPrice = 0;

    // Dates par défaut (Aujourd'hui et Demain)
    private long checkInTimestamp = MaterialDatePicker.todayInUtcMilliseconds();
    private long checkOutTimestamp = checkInTimestamp + (24 * 60 * 60 * 1000);

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuration du design immersif (barre de statut transparente)
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat ctrl = ViewCompat.getWindowInsetsController(window.getDecorView());
        if (ctrl != null) ctrl.setAppearanceLightStatusBars(false);

        binding = ActivityHotelDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Correction pour la barre de navigation
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        viewModel = new ViewModelProvider(this).get(HotelDetailViewModel.class);
        setupObservers();

        loadData();         // Chargement initial des données reçues de la liste
        setupSpinners();    // Configuration des choix (adultes, enfants, chambres)
        setupListeners();   // Configuration des clics
        updateDisplay();    // Mise à jour du prix et des dates à l'écran
    }

    /**
     * Observe les changements d'état (Favoris, Succès réservation) via le ViewModel.
     */
    private void setupObservers() {
        viewModel.hotelDetails.observe(this, hotel -> {
            if (hotel != null) {
                // Mise à jour si Firestore a des données plus fraîches
                binding.tvHotelName.setText(hotel.getName());
                binding.tvHotelLocationDetail.setText(getString(R.string.location_morocco, hotel.getCity()));
                pricePerNight = (int) hotel.getPrice_per_night();
                binding.tvRating.setText(String.valueOf(hotel.getRating()));
                if (hotel.getDescription() != null) binding.tvDescription.setText(hotel.getDescription());

                setupImages(hotel.getImageUrl(), hotel.getImages());
                updateDisplay();
            }
        });

        // Mise à jour visuelle du bouton Favori
        viewModel.isFavorite.observe(this, isFav -> {
            if (isFav) {
                binding.btnFavorite.setIconResource(R.drawable.ic_favorite_filled);
                binding.btnFavorite.setIconTintResource(R.color.black);
            } else {
                binding.btnFavorite.setIconResource(R.drawable.ic_favorite_border);
                binding.btnFavorite.setIconTintResource(R.color.black);
            }
        });

        // Feedback après réservation
        viewModel.bookingSuccess.observe(this, success -> {
            if (success) showSuccessDialog();
        });
    }

    private void setupSpinners() {
        String[] rooms = {"1 room", "2 rooms", "3 rooms"};
        String[] adults = {"1 adult", "2 adults", "3 adults", "4 adults"};
        String[] kids = {"0 kids", "1 kid", "2 kids"};

        binding.spinnerRooms.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, rooms));
        binding.spinnerAdults.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, adults));
        binding.spinnerKids.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kids));
    }

    /**
     * Récupère les données envoyées par l'activité précédente pour un affichage instantané.
     */
    private void loadData() {
        Intent i = getIntent();
        hotelId = i.getStringExtra(EXTRA_HOTEL_ID);
        String name = i.getStringExtra(EXTRA_HOTEL_NAME);
        
        if (name == null && hotelId != null) {
            viewModel.initHotel(hotelId);
        } else {
            displayHotelData(i); // Affichage rapide via Intent
            if (hotelId != null) viewModel.initHotel(hotelId); // Sync Firestore en arrière-plan
        }
    }

    private void displayHotelData(Intent i) {
        // ... Code d'affichage via Intent ...
        String name = i.getStringExtra(EXTRA_HOTEL_NAME);
        if (name != null) binding.tvHotelName.setText(name);

        String location = i.getStringExtra(EXTRA_HOTEL_LOCATION);
        if (location != null) binding.tvHotelLocationDetail.setText(location);

        pricePerNight = i.getIntExtra(EXTRA_HOTEL_PRICE, 1200);
        float rating = i.getFloatExtra(EXTRA_HOTEL_RATING, 4.92f);
        int reviews = i.getIntExtra(EXTRA_HOTEL_REVIEWS, 116);
        String description = i.getStringExtra(EXTRA_HOTEL_DESC);

        binding.tvRating.setText(String.valueOf(rating));
        binding.tvReviewCount.setText(getString(R.string.reviews_count, reviews));
        if (description != null) binding.tvDescription.setText(description);

        hotelImageUrl = i.getStringExtra(EXTRA_HOTEL_IMAGE_URL);
        String[] othersArr = i.getStringArrayExtra(EXTRA_HOTEL_IMAGES);
        List<String> others = othersArr != null ? Arrays.asList(othersArr) : null;
        
        setupImages(hotelImageUrl, others);
    }

    /**
     * Configure le ViewPager2 pour faire défiler les images de l'hôtel.
     */
    private void setupImages(String mainImage, List<String> others) {
        List<String> hotelImages = new ArrayList<>();
        if (mainImage != null) {
            hotelImageUrl = mainImage;
            hotelImages.add(mainImage);
        }
        if (others != null) hotelImages.addAll(others);

        HotelImageAdapter adapter = new HotelImageAdapter(hotelImages, this);
        binding.viewPagerImages.setAdapter(adapter);
        new TabLayoutMediator(binding.tabLayoutIndicators, binding.viewPagerImages, (tab, pos) -> {}).attach();

        // Mise à jour du compteur d'images (ex: 1 / 5)
        binding.viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.tvImageCounter.setText(String.format(Locale.getDefault(), "%d / %d", position + 1, hotelImages.size()));
            }
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());
        binding.cardCheckIn.setOnClickListener(v -> showDatePicker(true));
        binding.cardCheckOut.setOnClickListener(v -> showDatePicker(false));

        // Ouverture de Google Maps à l'adresse de l'hôtel
        binding.cardMap.setOnClickListener(v -> {
            Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(binding.tvHotelName.getText() + " " + binding.tvHotelLocationDetail.getText()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
            startActivity(mapIntent);
        });

        binding.btnBookNow.setOnClickListener(v -> showBookingSummary());
    }

    /**
     * Affiche un récapitulatif avant de confirmer la réservation.
     */
    private void showBookingSummary() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Connectez-vous pour réserver", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottomsheet_booking_confirm, null);
        dialog.setContentView(view);

        // ... Remplissage du résumé ...
        TextView bsHotelName = view.findViewById(R.id.bsHotelName);
        bsHotelName.setText(binding.tvHotelName.getText());

        view.findViewById(R.id.btnConfirmBooking).setOnClickListener(v -> {
            dialog.dismiss();
            performBooking(); // Lancement réel de la réservation
        });

        dialog.show();
    }

    /**
     * Affiche le sélecteur de date (Material Date Picker).
     */
    private void showDatePicker(boolean isCheckIn) {
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointForward.now()); // On ne peut pas réserver dans le passé

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isCheckIn ? "Sélectionnez l'arrivée" : "Sélectionnez le départ")
                .setSelection(isCheckIn ? checkInTimestamp : checkOutTimestamp)
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (isCheckIn) {
                checkInTimestamp = selection;
                // Le départ doit être au moins 1 jour après l'arrivée
                if (checkOutTimestamp <= checkInTimestamp) checkOutTimestamp = checkInTimestamp + TimeUnit.DAYS.toMillis(1);
            } else {
                if (selection <= checkInTimestamp) {
                    Toast.makeText(this, "Le départ doit être après l'arrivée", Toast.LENGTH_SHORT).show();
                    return;
                }
                checkOutTimestamp = selection;
            }
            updateDisplay();
        });

        picker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    /**
     * Recalcule dynamiquement le prix total en fonction du nombre de nuits.
     */
    private void updateDisplay() {
        binding.tvCheckInDate.setText(dateFormat.format(new Date(checkInTimestamp)));
        binding.tvCheckOutDate.setText(dateFormat.format(new Date(checkOutTimestamp)));

        long diff = checkOutTimestamp - checkInTimestamp;
        int nights = (int) TimeUnit.MILLISECONDS.toDays(diff);
        if (nights <= 0) nights = 1;

        int subtotal = pricePerNight * nights;
        int serviceFee = 150;
        int taxFee = 50;
        currentTotalPrice = subtotal + serviceFee + taxFee;

        binding.tvPriceDetailTotal.setText(getString(R.string.dh_currency, currentTotalPrice));
        binding.tvTotalPriceBottom.setText(getString(R.string.dh_currency, currentTotalPrice));
    }

    /**
     * Prépare l'objet de réservation et l'envoie à Firestore.
     * Le statut est mis à "Pending" pour validation par l'admin.
     */
    private void performBooking() {
        Map<String, Object> booking = new HashMap<>();
        booking.put("userId", FirebaseAuth.getInstance().getUid());
        booking.put("hotelId", hotelId);
        booking.put("hotelName", binding.tvHotelName.getText().toString());
        booking.put("hotelLocation", binding.tvHotelLocationDetail.getText().toString());
        booking.put("hotelImageUrl", hotelImageUrl);
        booking.put("checkIn", checkInTimestamp);
        booking.put("checkOut", checkOutTimestamp);
        booking.put("totalPrice", currentTotalPrice);
        booking.put("status", "Pending"); // Attente de validation admin
        
        // ... Gestion des spinners ...
        
        viewModel.performBooking(booking);
    }

    private void toggleFavorite() {
        viewModel.toggleFavorite();
    }

    /**
     * Affiche l'écran de succès final après confirmation Firestore.
     */
    private void showSuccessDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_booking_success, null);
        dialog.setContentView(view);
        dialog.setCancelable(false);

        view.findViewById(R.id.btnViewBookings).setOnClickListener(v -> {
            dialog.dismiss();
            NavigationHelper.fastNavigate(this, MyBookingsActivity.class);
            finish();
        });

        view.findViewById(R.id.btnBackToHome).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
}
