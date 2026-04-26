package com.ensab.reservaapp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensab.reservaapp.R;
import com.ensab.reservaapp.data.NavigationHelper;
import com.ensab.reservaapp.view.adapter.HotelAdapter;
import com.ensab.reservaapp.viewmodel.HotelListViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;

public class HotelListActivity extends AppCompatActivity {

    private RecyclerView rvHotels;
    private HotelAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvHotelCount, tvWelcomeName, tvSearch;
    private HotelListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_list);

        rvHotels = findViewById(R.id.rvHotels);
        progressBar = findViewById(R.id.progressBar);
        tvHotelCount = findViewById(R.id.tvHotelCount);
        tvWelcomeName = findViewById(R.id.tvWelcomeName);
        tvSearch = findViewById(R.id.tvSearch);

        rvHotels.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HotelAdapter(new ArrayList<>(), this);
        rvHotels.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(HotelListViewModel.class);

        viewModel.userName.observe(this, name -> {
            if (!name.isEmpty()) tvWelcomeName.setText("Hey, " + name);
        });

        viewModel.filteredHotels.observe(this, hotels -> {
            adapter.updateHotels(hotels);
            tvHotelCount.setText(hotels.size() + " résultats");
        });

        viewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        setupSearch();
        setupFilters();
        setupNotifications();
        
        NavigationHelper.setSelectedItem(this, R.id.navDiscover);
        setupBottomNavLinks();
    }

    private void setupSearch() {
        findViewById(R.id.cvSearchBar).setOnClickListener(v -> showSearchBottomSheet());
    }

    private void showSearchBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.layout_search_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);

        EditText etSheetSearch = sheetView.findViewById(R.id.etSheetSearch);
        
        sheetView.findViewById(R.id.destCasablanca).setOnClickListener(v -> updateSearch("Casablanca", bottomSheetDialog));
        sheetView.findViewById(R.id.destMarrakesh).setOnClickListener(v -> updateSearch("Marrakech", bottomSheetDialog));
        sheetView.findViewById(R.id.destRabat).setOnClickListener(v -> updateSearch("Rabat", bottomSheetDialog));
        sheetView.findViewById(R.id.destTangier).setOnClickListener(v -> updateSearch("Tanger", bottomSheetDialog));

        etSheetSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.filterHotels(s.toString());
                tvSearch.setText(s.length() > 0 ? s.toString() : "Start your search");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        bottomSheetDialog.show();
    }

    private void updateSearch(String query, BottomSheetDialog dialog) {
        tvSearch.setText(query);
        viewModel.filterHotels(query);
        dialog.dismiss();
    }

    private void setupFilters() {
        findViewById(R.id.chipAll).setOnClickListener(v -> {
            tvSearch.setText("Start your search");
            viewModel.filterHotels("");
        });
        
        findViewById(R.id.chipLuxe).setOnClickListener(v -> viewModel.filterLuxe());
        findViewById(R.id.chipPrice).setOnClickListener(v -> viewModel.sortByPrice());
        findViewById(R.id.chipRating).setOnClickListener(v -> viewModel.sortByRating());
    }

    private void setupNotifications() {
        findViewById(R.id.btnNotifications).setOnClickListener(v -> {
            Toast.makeText(this, "Vous avez 3 nouvelles offres !", Toast.LENGTH_LONG).show();
            findViewById(R.id.tvNotificationBadge).setVisibility(View.GONE);
        });
    }

    private void setupBottomNavLinks() {
        findViewById(R.id.navSaved).setOnClickListener(v -> NavigationHelper.fastNavigate(this, WishlistActivity.class));
        findViewById(R.id.navProfile).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ProfileActivity.class));
        findViewById(R.id.navBookings).setOnClickListener(v -> NavigationHelper.fastNavigate(this, MyBookingsActivity.class));
        findViewById(R.id.navDiscover).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ChoiceActivity.class));
    }
}
