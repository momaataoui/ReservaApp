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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensab.reservaapp.R;
import com.ensab.reservaapp.util.NavigationHelper;
import com.ensab.reservaapp.databinding.ActivityHotelListBinding;
import com.ensab.reservaapp.view.adapter.UnifiedHotelAdapter;
import com.ensab.reservaapp.viewmodel.HotelListViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;

public class HotelListActivity extends AppCompatActivity {

    private ActivityHotelListBinding binding;
    private UnifiedHotelAdapter adapter;
    private UnifiedHotelAdapter horizontalAdapter;
    private HotelListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHotelListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Fix for navigation bar overlap
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.bottomNavContainer.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Setup Adapters
        binding.rvHotels.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UnifiedHotelAdapter(false, (hotel, position) -> viewModel.toggleFavorite(hotel));
        binding.rvHotels.setAdapter(adapter);

        binding.rvHotelsHorizontal.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        horizontalAdapter = new UnifiedHotelAdapter(true, (hotel, position) -> viewModel.toggleFavorite(hotel));
        binding.rvHotelsHorizontal.setAdapter(horizontalAdapter);

        viewModel = new ViewModelProvider(this).get(HotelListViewModel.class);

        viewModel.userName.observe(this, name -> {
            if (!name.isEmpty()) binding.tvWelcomeName.setText("Hey, " + name);
        });

        viewModel.filteredHotels.observe(this, hotels -> {
            adapter.submitList(hotels);
            binding.tvHotelCount.setText(hotels.size() + " résultats");
        });

        viewModel.topRelevantHotels.observe(this, hotels -> {
            horizontalAdapter.submitList(hotels);
        });

        viewModel.favoriteIds.observe(this, ids -> {
            adapter.setFavoriteIds(ids);
            horizontalAdapter.setFavoriteIds(ids);
        });

        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        binding.tvViewAllHotels.setOnClickListener(v -> {
            if (binding.rvHotels.getVisibility() == View.GONE) {
                binding.rvHotels.setVisibility(View.VISIBLE);
                binding.rlTopHotelsHeader.setVisibility(View.VISIBLE);
                binding.tvViewAllHotels.setText("Show less");
            } else {
                binding.rvHotels.setVisibility(View.GONE);
                binding.rlTopHotelsHeader.setVisibility(View.GONE);
                binding.tvViewAllHotels.setText("View all >");
            }
        });

        setupSearch();
        setupFilters();
        setupNotifications();
        
        NavigationHelper.setSelectedItem(this, R.id.navDiscover);
        setupBottomNavLinks();
    }

    private void setupSearch() {
        binding.cvSearchBar.setOnClickListener(v -> showSearchBottomSheet());
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
                binding.tvSearch.setText(s.length() > 0 ? s.toString() : "Start your search");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        bottomSheetDialog.show();
    }

    private void updateSearch(String query, BottomSheetDialog dialog) {
        binding.tvSearch.setText(query);
        viewModel.filterHotels(query);
        dialog.dismiss();
    }

    private void setupFilters() {
        binding.chipAll.setOnClickListener(v -> {
            binding.tvSearch.setText("Start your search");
            viewModel.filterHotels("");
        });
        
        binding.chipLuxe.setOnClickListener(v -> viewModel.filterLuxe());
        binding.chipPrice.setOnClickListener(v -> viewModel.sortByPrice());
        binding.chipRating.setOnClickListener(v -> viewModel.sortByRating());
    }

    private void setupNotifications() {
        binding.btnNotifications.setOnClickListener(v -> {
            Toast.makeText(this, "Vous avez 3 nouvelles offres !", Toast.LENGTH_LONG).show();
            binding.tvNotificationBadge.setVisibility(View.GONE);
        });
    }

    private void setupBottomNavLinks() {
        findViewById(R.id.navSaved).setOnClickListener(v -> NavigationHelper.fastNavigate(this, WishlistActivity.class));
        findViewById(R.id.navProfile).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ProfileActivity.class));
        findViewById(R.id.navBookings).setOnClickListener(v -> NavigationHelper.fastNavigate(this, MyBookingsActivity.class));
        findViewById(R.id.navDiscover).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ChoiceActivity.class));
    }
}
