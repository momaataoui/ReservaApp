package com.ensab.reservaapp.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.ensab.reservaapp.R;
import com.ensab.reservaapp.data.NavigationHelper;
import com.ensab.reservaapp.view.adapter.HotelImageAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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

    private TextView tvHotelName, tvRating, tvReviewCount, tvDescription, tvHotelLocationDetail;
    private TextView tvCheckInDate, tvCheckOutDate, tvNightsCount, tvImageCounter;
    private TextView tvPricePerNightValue, tvNightsSubtotalLabel, tvNightsSubtotalValue;
    private TextView tvServiceFee, tvTaxFee, tvPriceDetailTotal, tvTotalPriceBottom;
    private Spinner  spinnerRooms, spinnerAdults, spinnerKids;
    private MaterialButton btnBack, btnBookNow, btnFavorite;
    private View cardCheckIn, cardCheckOut, cardMapPlaceholder;
    private ViewPager2 viewPagerImages;
    private TabLayout tabLayoutIndicators;

    private String hotelId;
    private String hotelImageUrl;
    private int pricePerNight = 1200;
    private int currentTotalPrice = 0;

    private long checkInTimestamp = MaterialDatePicker.todayInUtcMilliseconds();
    private long checkOutTimestamp = checkInTimestamp + (24 * 60 * 60 * 1000);

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat ctrl = ViewCompat.getWindowInsetsController(window.getDecorView());
        if (ctrl != null) ctrl.setAppearanceLightStatusBars(false);

        setContentView(R.layout.activity_hotel_detail);

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        initViews();
        loadData();
        setupSpinners();
        setupListeners();
        updateDisplay();
    }

    private void initViews() {
        tvHotelName = findViewById(R.id.tvHotelName);
        tvRating = findViewById(R.id.tvRating);
        tvReviewCount = findViewById(R.id.tvReviewCount);
        tvDescription = findViewById(R.id.tvDescription);
        tvHotelLocationDetail = findViewById(R.id.tvHotelLocationDetail);
        tvCheckInDate = findViewById(R.id.tvCheckInDate);
        tvCheckOutDate = findViewById(R.id.tvCheckOutDate);
        tvNightsCount = findViewById(R.id.tvNightsCount);
        tvImageCounter = findViewById(R.id.tvImageCounter);
        
        tvPricePerNightValue = findViewById(R.id.tvPricePerNightValue);
        tvNightsSubtotalLabel = findViewById(R.id.tvNightsSubtotalLabel);
        tvNightsSubtotalValue = findViewById(R.id.tvNightsSubtotalValue);
        tvServiceFee = findViewById(R.id.tvServiceFee);
        tvTaxFee = findViewById(R.id.tvTaxFee);
        tvPriceDetailTotal = findViewById(R.id.tvPriceDetailTotal);
        tvTotalPriceBottom = findViewById(R.id.tvTotalPriceBottom);

        spinnerRooms = findViewById(R.id.spinnerRooms);
        spinnerAdults = findViewById(R.id.spinnerAdults);
        spinnerKids = findViewById(R.id.spinnerKids);

        cardCheckIn = findViewById(R.id.cardCheckIn);
        cardCheckOut = findViewById(R.id.cardCheckOut);
        cardMapPlaceholder = findViewById(R.id.cardMapPlaceholder);
        
        viewPagerImages = findViewById(R.id.viewPagerImages);
        tabLayoutIndicators = findViewById(R.id.tabLayoutIndicators);
        
        btnBack = findViewById(R.id.btnBack);
        btnBookNow = findViewById(R.id.btnBookNow);
        btnFavorite = findViewById(R.id.btnFavorite);
    }

    private void setupSpinners() {
        String[] rooms = {"1 room", "2 rooms", "3 rooms"};
        String[] adults = {"1 adult", "2 adults", "3 adults", "4 adults"};
        String[] kids = {"0 kids", "1 kid", "2 kids"};

        spinnerRooms.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, rooms));
        spinnerAdults.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, adults));
        spinnerKids.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kids));
    }

    private void loadData() {
        Intent i = getIntent();
        hotelId = i.getStringExtra(EXTRA_HOTEL_ID);
        String name = i.getStringExtra(EXTRA_HOTEL_NAME);
        if (name != null) tvHotelName.setText(name);
        
        String location = i.getStringExtra(EXTRA_HOTEL_LOCATION);
        if (location != null) tvHotelLocationDetail.setText(location);
        
        pricePerNight = i.getIntExtra(EXTRA_HOTEL_PRICE, 1200);
        float rating = i.getFloatExtra(EXTRA_HOTEL_RATING, 4.92f);
        int reviews = i.getIntExtra(EXTRA_HOTEL_REVIEWS, 116);
        String description = i.getStringExtra(EXTRA_HOTEL_DESC);
        
        tvRating.setText(String.valueOf(rating));
        tvReviewCount.setText("(" + reviews + " reviews)");
        if (description != null) tvDescription.setText(description);
        
        List<String> hotelImages = new ArrayList<>();
        hotelImageUrl = i.getStringExtra(EXTRA_HOTEL_IMAGE_URL);
        if (hotelImageUrl != null) hotelImages.add(hotelImageUrl);
        String[] others = i.getStringArrayExtra(EXTRA_HOTEL_IMAGES);
        if (others != null) hotelImages.addAll(Arrays.asList(others));
        
        if (hotelImages.isEmpty()) {
            hotelImageUrl = "https://images.unsplash.com/photo-1587061949733-5d6932e1574e";
            hotelImages.add(hotelImageUrl);
        } else if (hotelImageUrl == null) {
            hotelImageUrl = hotelImages.get(0);
        }

        HotelImageAdapter adapter = new HotelImageAdapter(hotelImages, this);
        viewPagerImages.setAdapter(adapter);
        new TabLayoutMediator(tabLayoutIndicators, viewPagerImages, (tab, pos) -> {}).attach();

        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (tvImageCounter != null) {
                    tvImageCounter.setText((position + 1) + " / " + hotelImages.size());
                }
            }
        });

        checkIfFavorite();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        cardCheckIn.setOnClickListener(v -> showDatePicker(true));
        cardCheckOut.setOnClickListener(v -> showDatePicker(false));

        cardMapPlaceholder.setOnClickListener(v -> {
            Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(tvHotelName.getText() + " " + tvHotelLocationDetail.getText()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, mapUri));
            }
        });

        btnBookNow.setOnClickListener(v -> showBookingSummary());
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

        bsHotelName.setText(tvHotelName.getText());
        bsCheckIn.setText(tvCheckInDate.getText());
        bsCheckOut.setText(tvCheckOutDate.getText());
        
        long diff = checkOutTimestamp - checkInTimestamp;
        int nights = (int) (diff / (24 * 60 * 60 * 1000));
        if (nights <= 0) nights = 1;
        bsNights.setText(nights + (nights > 1 ? " nights" : " night"));

        String guestsText = spinnerAdults.getSelectedItem() + ", " + spinnerKids.getSelectedItem();
        bsGuests.setText(guestsText);
        bsTotalPrice.setText(tvTotalPriceBottom.getText());

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            performBooking();
        });

        dialog.show();
    }

    private void checkIfFavorite() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null || hotelId == null) return;

        FirebaseFirestore.getInstance().collection("wishlists")
                .whereEqualTo("userId", userId)
                .whereEqualTo("hotelId", hotelId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        btnFavorite.setIconResource(R.drawable.ic_favorite_filled);
                        btnFavorite.setIconTintResource(R.color.black);
                    }
                });
    }

    private void toggleFavorite() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(this, "Please log in to add to favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("wishlists")
                .whereEqualTo("userId", userId)
                .whereEqualTo("hotelId", hotelId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Map<String, Object> fav = new HashMap<>();
                        fav.put("userId", userId);
                        fav.put("hotelId", hotelId);
                        fav.put("hotelName", tvHotelName.getText().toString());
                        fav.put("hotelLocation", tvHotelLocationDetail.getText().toString());
                        fav.put("hotelPrice", pricePerNight);

                        db.collection("wishlists").add(fav).addOnSuccessListener(doc -> {
                            btnFavorite.setIconResource(R.drawable.ic_favorite_filled);
                            btnFavorite.setIconTintResource(R.color.black);
                            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("wishlists").document(docId).delete().addOnSuccessListener(aVoid -> {
                            btnFavorite.setIconResource(R.drawable.ic_favorite_border);
                            btnFavorite.setIconTintResource(R.color.black);
                            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void performBooking() {
        Map<String, Object> booking = new HashMap<>();
        booking.put("userId", FirebaseAuth.getInstance().getUid());
        booking.put("hotelId", hotelId);
        booking.put("hotelName", tvHotelName.getText().toString());
        booking.put("hotelLocation", tvHotelLocationDetail.getText().toString());
        booking.put("hotelImageUrl", hotelImageUrl);
        booking.put("checkIn", checkInTimestamp); // Save as long (milliseconds)
        booking.put("checkOut", checkOutTimestamp); // Save as long (milliseconds)
        booking.put("totalPrice", currentTotalPrice); // Save as int
        booking.put("status", "Confirmed");
        
        // Extract count from spinner selection (e.g., "2 adults" -> 2)
        int adults = 1;
        try { adults = Integer.parseInt(spinnerAdults.getSelectedItem().toString().split(" ")[0]); } catch (Exception e) {}
        int kids = 0;
        try { kids = Integer.parseInt(spinnerKids.getSelectedItem().toString().split(" ")[0]); } catch (Exception e) {}
        
        booking.put("adults", adults);
        booking.put("children", kids);

        FirebaseFirestore.getInstance().collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    showSuccessDialog();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error during booking", Toast.LENGTH_SHORT).show());
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
                    checkOutTimestamp = checkInTimestamp + (24 * 60 * 60 * 1000);
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
        tvCheckInDate.setText(dateFormat.format(new Date(checkInTimestamp)));
        tvCheckOutDate.setText(dateFormat.format(new Date(checkOutTimestamp)));

        long diff = checkOutTimestamp - checkInTimestamp;
        int nights = (int) (diff / (24 * 60 * 60 * 1000));
        if (nights <= 0) nights = 1;

        tvNightsCount.setText(nights + (nights > 1 ? " nights" : " night"));
        tvPricePerNightValue.setText(pricePerNight + " DH");
        tvNightsSubtotalLabel.setText(nights + (nights > 1 ? " nights" : " night"));
        
        int subtotal = pricePerNight * nights;
        tvNightsSubtotalValue.setText(subtotal + " DH");
        
        int serviceFee = 150;
        int taxFee = 50;
        tvServiceFee.setText(serviceFee + " DH");
        tvTaxFee.setText(taxFee + " DH");
        
        currentTotalPrice = subtotal + serviceFee + taxFee;
        tvPriceDetailTotal.setText(currentTotalPrice + " DH");
        tvTotalPriceBottom.setText(currentTotalPrice + " DH");
    }
}
