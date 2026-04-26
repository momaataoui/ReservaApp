package com.ensab.reservaapp.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.data.NavigationHelper;
import com.ensab.reservaapp.model.Booking;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HotelDetailActivity extends AppCompatActivity {

    public static final String EXTRA_HOTEL_ID       = "hotelId";
    public static final String EXTRA_HOTEL_NAME     = "hotel_name";
    public static final String EXTRA_HOTEL_LOCATION = "hotel_location";
    public static final String EXTRA_HOTEL_PRICE    = "hotel_price";
    public static final String EXTRA_HOTEL_RATING   = "hotel_rating";
    public static final String EXTRA_HOTEL_REVIEWS  = "hotel_reviews";
    public static final String EXTRA_HOTEL_DESC     = "hotel_description";
    public static final String EXTRA_HOTEL_IMAGE_URL = "hotel_image_url";

    private TextView         tvHotelName, tvLocation, tvPriceSummaryTotal, tvLocationHeader;
    private TextView         tvRating, tvReviewCount, tvDescription;
    private TextView         tvCheckInDate, tvCheckOutDate, tvGuests;
    private TextView         tvTotalPrice;
    private TextView         tvPriceSummaryAmount, tvPriceSummaryTaxes, tvPriceSummaryLabel;
    private TextView         tvBedsCount, tvDoubleBedsCount;
    private ImageView        ivHeroImage, btnFavorite;
    private MaterialButton   btnBack, btnCheckAvailability;
    private MaterialButton   btnBedsMinus, btnBedsPlus;
    private MaterialButton   btnDoubleBedsMinus, btnDoubleBedsPlus;
    private MaterialCardView cvCheckIn, cvCheckOut, cvGuests;
    private View             llReadMore;

    private boolean isFavorite     = false;
    private boolean isExpanded     = false;
    private long    checkInMillis;
    private long    checkOutMillis;
    private int     adults         = 2;
    private int     children       = 1;
    private int     pricePerNight;
    private int     simpleBeds     = 1;
    private int     doubleBeds     = 0;
    
    private String  hotelId;
    private String  hotelName;
    private String  hotelLocation;
    private String  hotelImageUrl;

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("dd MMM yyyy", Locale.FRENCH);

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.WHITE);
        WindowInsetsControllerCompat ctrl =
                ViewCompat.getWindowInsetsController(window.getDecorView());
        if (ctrl != null) ctrl.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_hotel_detail);

        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                    root, (v, insets) -> {
                        Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                        v.setPadding(bars.left, bars.top, bars.right, 0);
                        return insets;
                    });
        }

        initViews();
        loadData();
        setupListeners();
        checkIfFavorite();
    }

    private void initViews() {
        tvHotelName          = findViewById(R.id.tvHotelName);
        tvLocationHeader     = findViewById(R.id.tvLocationHeader);
        tvRating             = findViewById(R.id.tvRating);
        tvReviewCount        = findViewById(R.id.tvReviewCount);
        tvDescription        = findViewById(R.id.tvDescription);
        tvCheckInDate        = findViewById(R.id.tvCheckInDate);
        tvCheckOutDate       = findViewById(R.id.tvCheckOutDate);
        tvGuests             = findViewById(R.id.tvGuests);
        tvTotalPrice         = findViewById(R.id.tvTotalPrice);
        tvPriceSummaryTotal  = findViewById(R.id.tvPriceSummaryTotal);
        tvPriceSummaryAmount = findViewById(R.id.tvPriceSummaryAmount);
        tvPriceSummaryTaxes  = findViewById(R.id.tvPriceSummaryTaxes);
        tvPriceSummaryLabel  = findViewById(R.id.tvPriceSummaryLabel);
        
        tvBedsCount          = findViewById(R.id.tvBedsCount);
        tvDoubleBedsCount    = findViewById(R.id.tvDoubleBedsCount);
        
        ivHeroImage          = findViewById(R.id.ivHeroImage);
        llReadMore           = findViewById(R.id.llReadMore);
        btnBack              = findViewById(R.id.btnBack);
        btnFavorite          = findViewById(R.id.btnFavorite);
        btnCheckAvailability = findViewById(R.id.btnCheckAvailability);
        
        btnBedsMinus         = findViewById(R.id.btnBedsMinus);
        btnBedsPlus          = findViewById(R.id.btnBedsPlus);
        btnDoubleBedsMinus   = findViewById(R.id.btnDoubleBedsMinus);
        btnDoubleBedsPlus    = findViewById(R.id.btnDoubleBedsPlus);
        
        cvCheckIn            = findViewById(R.id.cvCheckIn);
        cvCheckOut           = findViewById(R.id.cvCheckOut);
        cvGuests             = findViewById(R.id.cvGuests);
    }

    private void loadData() {
        Intent i = getIntent();
        hotelId       = i.getStringExtra(EXTRA_HOTEL_ID);
        hotelName     = getExtra(i, EXTRA_HOTEL_NAME,     "Hôtel des Lumières");
        hotelLocation = getExtra(i, EXTRA_HOTEL_LOCATION, "Le Marais, Paris");
        pricePerNight = i.getIntExtra(EXTRA_HOTEL_PRICE, 189);
        float  rating = i.getFloatExtra(EXTRA_HOTEL_RATING, 4.7f);
        int    reviews= i.getIntExtra(EXTRA_HOTEL_REVIEWS, 1289);
        String desc   = getExtra(i, EXTRA_HOTEL_DESC,
                "Charmant hôtel 4 étoiles au cœur du Marais. Chambres élégantes, service attentionné et emplacement idéal pour découvrir Paris.");
        
        hotelImageUrl = i.getStringExtra(EXTRA_HOTEL_IMAGE_URL);

        checkInMillis  = System.currentTimeMillis();
        checkOutMillis = checkInMillis + 1L * 86_400_000; // 1 night default as per UI

        if (tvHotelName != null) tvHotelName.setText(hotelName);
        if (tvLocationHeader != null) tvLocationHeader.setText(hotelLocation + " • 1,2 km du centre");
        if (tvRating != null) tvRating.setText(String.format(Locale.getDefault(), "%.1f", rating).replace(".", ","));
        if (tvReviewCount != null) tvReviewCount.setText(String.format(Locale.getDefault(), "(%d avis)", reviews));
        if (tvDescription != null) tvDescription.setText(desc);
        
        if (ivHeroImage != null) {
            if (hotelImageUrl != null && !hotelImageUrl.isEmpty()) {
                Glide.with(this).load(hotelImageUrl).centerCrop().into(ivHeroImage);
            } else {
                ivHeroImage.setImageResource(R.drawable.mamounia);
            }
        }
        
        updateBedsUI();
        refreshDatesAndTotal();
    }

    private void checkIfFavorite() {
        if (mAuth.getCurrentUser() == null || hotelId == null) return;

        db.collection("users").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> favorites = (List<String>) documentSnapshot.get("favorites");
                        if (favorites != null && favorites.contains(hotelId)) {
                            isFavorite = true;
                            btnFavorite.setImageResource(R.drawable.ic_saved_filled);
                        }
                    }
                });
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> toggleFavorite());
        }

        if (llReadMore != null) {
            llReadMore.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                TextView label = llReadMore.findViewById(android.R.id.text1);
                if (label == null && llReadMore instanceof android.view.ViewGroup) {
                    label = (TextView) ((android.view.ViewGroup) llReadMore).getChildAt(0);
                }
                if (isExpanded) {
                    tvDescription.setMaxLines(Integer.MAX_VALUE);
                    if (label != null) label.setText("Voir moins");
                } else {
                    tvDescription.setMaxLines(3);
                    if (label != null) label.setText("Voir plus");
                }
            });
        }

        if (cvCheckIn != null) cvCheckIn.setOnClickListener(v  -> openDatePicker(true));
        if (cvCheckOut != null) cvCheckOut.setOnClickListener(v -> openDatePicker(false));
        if (cvGuests != null) cvGuests.setOnClickListener(v -> showGuestDialog());
        
        // Simple Beds
        if (btnBedsMinus != null) {
            btnBedsMinus.setOnClickListener(v -> {
                if (simpleBeds > 0 && (simpleBeds + doubleBeds) > 1) {
                    simpleBeds--;
                    updateBedsUI();
                    refreshDatesAndTotal();
                }
            });
        }
        if (btnBedsPlus != null) {
            btnBedsPlus.setOnClickListener(v -> {
                if (simpleBeds + doubleBeds < 5) {
                    simpleBeds++;
                    updateBedsUI();
                    refreshDatesAndTotal();
                } else {
                    Toast.makeText(this, "Maximum 5 lits au total", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Double Beds
        if (btnDoubleBedsMinus != null) {
            btnDoubleBedsMinus.setOnClickListener(v -> {
                if (doubleBeds > 0 && (simpleBeds + doubleBeds) > 1) {
                    doubleBeds--;
                    updateBedsUI();
                    refreshDatesAndTotal();
                }
            });
        }
        if (btnDoubleBedsPlus != null) {
            btnDoubleBedsPlus.setOnClickListener(v -> {
                if (simpleBeds + doubleBeds < 5) {
                    doubleBeds++;
                    updateBedsUI();
                    refreshDatesAndTotal();
                } else {
                    Toast.makeText(this, "Maximum 5 lits au total", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnCheckAvailability != null) {
            btnCheckAvailability.setOnClickListener(v -> {
                long nights = Math.max(1, (checkOutMillis - checkInMillis) / 86_400_000L);
                int roomTotal = (pricePerNight + (doubleBeds * 200)) * (int) nights;
                int taxes = (int) (roomTotal * 0.1);
                saveBookingToFirestore(roomTotal + taxes);
            });
        }
    }

    private void toggleFavorite() {
        if (mAuth.getCurrentUser() == null || hotelId == null) {
            Toast.makeText(this, "Connectez-vous pour ajouter aux favoris", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        if (isFavorite) {
            db.collection("users").document(userId)
                    .update("favorites", FieldValue.arrayRemove(hotelId))
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = false;
                        btnFavorite.setImageResource(R.drawable.ic_saved);
                        Toast.makeText(this, "Retiré des favoris", Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("users").document(userId)
                    .update("favorites", FieldValue.arrayUnion(hotelId))
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = true;
                        btnFavorite.setImageResource(R.drawable.ic_saved_filled);
                        Toast.makeText(this, "Ajouté aux favoris ❤️", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        db.collection("users").document(userId)
                                .set(new java.util.HashMap<String, Object>() {{
                                    put("favorites", java.util.Arrays.asList(hotelId));
                                }}, com.google.firebase.firestore.SetOptions.merge());
                        isFavorite = true;
                        btnFavorite.setImageResource(R.drawable.ic_saved_filled);
                    });
        }
    }

    private void updateBedsUI() {
        if (tvBedsCount != null) tvBedsCount.setText(String.valueOf(simpleBeds));
        if (tvDoubleBedsCount != null) tvDoubleBedsCount.setText(String.valueOf(doubleBeds));
    }

    private void openDatePicker(boolean isCheckIn) {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isCheckIn ? "Date d'arrivée" : "Date de départ")
                .setSelection(isCheckIn ? checkInMillis : checkOutMillis)
                .setCalendarConstraints(constraints)
                .build();

        picker.addOnPositiveButtonClickListener(ms -> {
            if (isCheckIn) {
                checkInMillis = ms;
                if (checkOutMillis <= checkInMillis)
                    checkOutMillis = checkInMillis + 86_400_000L;
            } else {
                if (ms > checkInMillis) {
                    checkOutMillis = ms;
                } else {
                    Toast.makeText(this, "La date de départ doit être après l'arrivée", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            refreshDatesAndTotal();
        });

        picker.show(getSupportFragmentManager(), "dp_" + isCheckIn);
    }

    private void showGuestDialog() {
        final int[] tmpAdults   = {adults};
        final int[] tmpChildren = {children};
        View dv = LayoutInflater.from(this).inflate(R.layout.dialog_guest_picker, null);
        TextView tvA = dv.findViewById(R.id.tvAdultsCount);
        TextView tvC = dv.findViewById(R.id.tvChildrenCount);
        MaterialButton bAm = dv.findViewById(R.id.btnAdultMinus);
        MaterialButton bAp = dv.findViewById(R.id.btnAdultPlus);
        MaterialButton bCm = dv.findViewById(R.id.btnChildMinus);
        MaterialButton bCp = dv.findViewById(R.id.btnChildPlus);

        if (tvA != null) tvA.setText(String.valueOf(tmpAdults[0]));
        if (tvC != null) tvC.setText(String.valueOf(tmpChildren[0]));

        if (bAm != null) bAm.setOnClickListener(v -> { if (tmpAdults[0] > 1) { tmpAdults[0]--; tvA.setText(String.valueOf(tmpAdults[0])); } });
        if (bAp != null) bAp.setOnClickListener(v -> { if (tmpAdults[0] < 10) { tmpAdults[0]++; tvA.setText(String.valueOf(tmpAdults[0])); } });
        if (bCm != null) bCm.setOnClickListener(v -> { if (tmpChildren[0] > 0) { tmpChildren[0]--; tvC.setText(String.valueOf(tmpChildren[0])); } });
        if (bCp != null) bCp.setOnClickListener(v -> { if (tmpChildren[0] < 6) { tmpChildren[0]++; tvC.setText(String.valueOf(tmpChildren[0])); } });

        new AlertDialog.Builder(this)
                .setTitle("Voyageurs")
                .setView(dv)
                .setPositiveButton("Confirmer", (d, w) -> {
                    adults   = tmpAdults[0];
                    children = tmpChildren[0];
                    if (tvGuests != null) tvGuests.setText(guestLabel());
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void saveBookingToFirestore(int total) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(this, "Veuillez vous connecter pour réserver", Toast.LENGTH_SHORT).show();
            return;
        }

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setHotelId(hotelId);
        booking.setHotelName(hotelName);
        booking.setHotelLocation(hotelLocation);
        booking.setHotelImageUrl(hotelImageUrl);
        booking.setCheckIn(checkInMillis);
        booking.setCheckOut(checkOutMillis);
        booking.setAdults(adults);
        booking.setChildren(children);
        booking.setTotalPrice(total);
        booking.setStatus("pending");

        FirebaseFirestore.getInstance().collection("bookings")
            .add(booking)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "✅ Réservation confirmée !", Toast.LENGTH_LONG).show();
                NavigationHelper.fastNavigate(this, MyBookingsActivity.class);
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void refreshDatesAndTotal() {
        if (tvCheckInDate != null) tvCheckInDate.setText(DATE_FMT.format(new Date(checkInMillis)));
        if (tvCheckOutDate != null) tvCheckOutDate.setText(DATE_FMT.format(new Date(checkOutMillis)));

        long nights = Math.max(1, (checkOutMillis - checkInMillis) / 86_400_000L);
        // Base calculation: use pricePerNight from Intent, double beds cost +200 DH per bed
        int roomTotal = (pricePerNight + (doubleBeds * 200)) * (int) nights;
        int taxes = (int) (roomTotal * 0.1);
        int total = roomTotal + taxes;

        if (tvPriceSummaryLabel != null) tvPriceSummaryLabel.setText("Séjour (" + nights + " nuit" + (nights > 1 ? "s" : "") + ")");
        if (tvPriceSummaryAmount != null) tvPriceSummaryAmount.setText(roomTotal + " DH");
        if (tvPriceSummaryTaxes != null) tvPriceSummaryTaxes.setText(taxes + " DH");
        if (tvPriceSummaryTotal != null) tvPriceSummaryTotal.setText(total + " DH");
        if (tvTotalPrice != null) tvTotalPrice.setText(total + " DH");
        
        if (tvGuests != null) tvGuests.setText(guestLabel());
    }

    private String guestLabel() {
        return adults + " adlt, " + children + " enf";
    }

    private static String getExtra(Intent i, String key, String def) {
        String v = i.getStringExtra(key); return v != null ? v : def;
    }
}
