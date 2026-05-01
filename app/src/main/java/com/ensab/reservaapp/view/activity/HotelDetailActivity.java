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

public class HotelDetailActivity extends AppCompatActivity {

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

    private long checkInTimestamp = MaterialDatePicker.todayInUtcMilliseconds();
    private long checkOutTimestamp = checkInTimestamp + (24 * 60 * 60 * 1000);

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat ctrl = ViewCompat.getWindowInsetsController(window.getDecorView());
        if (ctrl != null) ctrl.setAppearanceLightStatusBars(false);

        binding = ActivityHotelDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Fix for navigation bar overlap
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        viewModel = new ViewModelProvider(this).get(HotelDetailViewModel.class);
        setupObservers();

        loadData();
        setupSpinners();
        setupListeners();
        updateDisplay();
    }

    private void setupObservers() {
        viewModel.hotelDetails.observe(this, hotel -> {
            if (hotel != null) {
                binding.tvHotelName.setText(hotel.getName());
                binding.tvHotelLocationDetail.setText(getString(R.string.location_morocco, hotel.getCity()));
                pricePerNight = (int) hotel.getPrice_per_night();
                binding.tvRating.setText(String.valueOf(hotel.getRating()));
                if (hotel.getDescription() != null) binding.tvDescription.setText(hotel.getDescription());

                setupImages(hotel.getImageUrl(), hotel.getImages());
                updateDisplay();
            }
        });

        viewModel.isFavorite.observe(this, isFav -> {
            if (isFav) {
                binding.btnFavorite.setIconResource(R.drawable.ic_favorite_filled);
                binding.btnFavorite.setIconTintResource(R.color.black);
            } else {
                binding.btnFavorite.setIconResource(R.drawable.ic_favorite_border);
                binding.btnFavorite.setIconTintResource(R.color.black);
            }
        });

        viewModel.errorMessage.observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

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

    private void loadData() {
        Intent i = getIntent();
        hotelId = i.getStringExtra(EXTRA_HOTEL_ID);
        String name = i.getStringExtra(EXTRA_HOTEL_NAME);
        
        if (name == null && hotelId != null) {
            viewModel.initHotel(hotelId);
        } else {
            displayHotelData(i);
            if (hotelId != null) viewModel.initHotel(hotelId);
        }
    }

    private void displayHotelData(Intent i) {
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

    private void setupImages(String mainImage, List<String> others) {
        List<String> hotelImages = new ArrayList<>();
        if (mainImage != null) {
            hotelImageUrl = mainImage;
            hotelImages.add(mainImage);
        }
        if (others != null) {
            hotelImages.addAll(others);
        }

        if (hotelImages.isEmpty()) {
            hotelImageUrl = "https://images.unsplash.com/photo-1587061949733-5d6932e1574e";
            hotelImages.add(hotelImageUrl);
        } else if (hotelImageUrl == null) {
            hotelImageUrl = hotelImages.get(0);
        }

        HotelImageAdapter adapter = new HotelImageAdapter(hotelImages, this);
        binding.viewPagerImages.setAdapter(adapter);
        new TabLayoutMediator(binding.tabLayoutIndicators, binding.viewPagerImages, (tab, pos) -> {}).attach();

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

        binding.cardMap.setOnClickListener(v -> {
            Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(binding.tvHotelName.getText() + " " + binding.tvHotelLocationDetail.getText()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, mapUri));
            }
        });

        binding.btnBookNow.setOnClickListener(v -> showBookingSummary());
    }

    private void showBookingSummary() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to book", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottomsheet_booking_confirm, null);
        dialog.setContentView(view);

        TextView bsHotelName = view.findViewById(R.id.bsHotelName);
        TextView bsCheckIn = view.findViewById(R.id.bsCheckIn);
        TextView bsCheckOut = view.findViewById(R.id.bsCheckOut);
        TextView bsNights = view.findViewById(R.id.bsNights);
        TextView bsGuests = view.findViewById(R.id.bsGuests);
        TextView bsTotalPrice = view.findViewById(R.id.bsTotalPrice);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirmBooking);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancelBooking);

        bsHotelName.setText(binding.tvHotelName.getText());
        bsCheckIn.setText(binding.tvCheckInDate.getText());
        bsCheckOut.setText(binding.tvCheckOutDate.getText());
        
        long diff = checkOutTimestamp - checkInTimestamp;
        int nights = (int) TimeUnit.MILLISECONDS.toDays(diff);
        if (nights <= 0) nights = 1;
        bsNights.setText(nights > 1 ? getString(R.string.night_count_multiple, nights) : getString(R.string.night_count_single));

        String guestsText = binding.spinnerAdults.getSelectedItem() + ", " + binding.spinnerKids.getSelectedItem();
        bsGuests.setText(guestsText);
        bsTotalPrice.setText(binding.tvTotalPriceBottom.getText());

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            performBooking();
        });

        dialog.show();
    }

    private void toggleFavorite() {
        if (FirebaseAuth.getInstance().getUid() == null) {
            Toast.makeText(this, "Please log in to add to favorites", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.toggleFavorite();
        setResult(RESULT_OK); // To notify previous activity if needed
    }

    private void showDatePicker(boolean isCheckIn) {
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointForward.now());

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isCheckIn ? "Select Check-in Date" : "Select Check-out Date")
                .setSelection(isCheckIn ? checkInTimestamp : checkOutTimestamp)
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (isCheckIn) {
                checkInTimestamp = selection;
                if (checkOutTimestamp <= checkInTimestamp) {
                    checkOutTimestamp = checkInTimestamp + TimeUnit.DAYS.toMillis(1);
                }
            } else {
                if (selection <= checkInTimestamp) {
                    Toast.makeText(this, "Check-out must be after check-in", Toast.LENGTH_SHORT).show();
                    return;
                }
                checkOutTimestamp = selection;
            }
            updateDisplay();
        });

        picker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void updateDisplay() {
        binding.tvCheckInDate.setText(dateFormat.format(new Date(checkInTimestamp)));
        binding.tvCheckOutDate.setText(dateFormat.format(new Date(checkOutTimestamp)));

        long diff = checkOutTimestamp - checkInTimestamp;
        int nights = (int) TimeUnit.MILLISECONDS.toDays(diff);
        if (nights <= 0) nights = 1;

        String nightsText = nights > 1 ? getString(R.string.night_count_multiple, nights) : getString(R.string.night_count_single);
        binding.tvNightsCount.setText(nightsText + " selected");
        binding.tvPricePerNightValue.setText(getString(R.string.dh_currency, pricePerNight));
        binding.tvNightsSubtotalLabel.setText(nightsText);
        
        int subtotal = pricePerNight * nights;
        binding.tvNightsSubtotalValue.setText(getString(R.string.dh_currency, subtotal));
        
        int serviceFee = 150;
        int taxFee = 50;
        binding.tvServiceFee.setText(getString(R.string.dh_currency, serviceFee));
        binding.tvTaxFee.setText(getString(R.string.dh_currency, taxFee));
        
        currentTotalPrice = subtotal + serviceFee + taxFee;
        binding.tvPriceDetailTotal.setText(getString(R.string.dh_currency, currentTotalPrice));
        binding.tvTotalPriceBottom.setText(getString(R.string.dh_currency, currentTotalPrice));
    }

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
        booking.put("status", "Confirmed");
        
        int adults = 1;
        try { adults = Integer.parseInt(binding.spinnerAdults.getSelectedItem().toString().split(" ")[0]); } catch (Exception e) {}
        int kids = 0;
        try { kids = Integer.parseInt(binding.spinnerKids.getSelectedItem().toString().split(" ")[0]); } catch (Exception e) {}
        
        booking.put("adults", adults);
        booking.put("children", kids);

        viewModel.performBooking(booking);
    }

    private void showSuccessDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_booking_success, null);
        dialog.setContentView(view);
        dialog.setCancelable(false);

        MaterialButton btnViewBookings = view.findViewById(R.id.btnViewBookings);
        MaterialButton btnHome = view.findViewById(R.id.btnBackToHome);

        btnViewBookings.setOnClickListener(v -> {
            dialog.dismiss();
            NavigationHelper.fastNavigate(this, MyBookingsActivity.class);
            finish();
        });

        btnHome.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
}
