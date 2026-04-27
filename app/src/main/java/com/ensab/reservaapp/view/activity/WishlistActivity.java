package com.ensab.reservaapp.view.activity;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.util.NavigationHelper;
import com.ensab.reservaapp.databinding.ActivityWishlistBinding;
import com.ensab.reservaapp.view.adapter.UnifiedHotelAdapter;
import com.ensab.reservaapp.viewmodel.HotelListViewModel;
import java.util.ArrayList;

public class WishlistActivity extends AppCompatActivity {
    private ActivityWishlistBinding binding;
    private UnifiedHotelAdapter adapter;
    private HotelListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWishlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Fix for navigation bar overlap
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.bottomNavContainer.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(HotelListViewModel.class);
        
        setupRecyclerView();
        observeViewModel();
        setupNavigation();
    }

    private void setupRecyclerView() {
        adapter = new UnifiedHotelAdapter(false, (hotel, position) -> viewModel.toggleFavorite(hotel));
        binding.rvWishlist.setLayoutManager(new LinearLayoutManager(this));
        binding.rvWishlist.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.favoriteIds.observe(this, ids -> {
            adapter.setFavoriteIds(ids);
            if (ids == null || ids.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.rvWishlist.setVisibility(View.GONE);
                adapter.submitList(new ArrayList<>());
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.rvWishlist.setVisibility(View.VISIBLE);
                viewModel.loadHotelsByIds(ids);
            }
        });

        viewModel.wishlistHotels.observe(this, hotels -> {
            adapter.submitList(hotels);
        });

        viewModel.isLoading.observe(this, isLoading -> {
            // Check if progressBar exists in layout, if not, skip
            if (binding.rvWishlist != null) {
                // If we had a progress bar, we'd toggle it here. 
                // For now, let's just make sure we don't crash.
            }
        });
    }

    private void setupNavigation() {
        NavigationHelper.setSelectedItem(this, R.id.navSaved);
        findViewById(R.id.navDiscover).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ChoiceActivity.class));
        findViewById(R.id.navBookings).setOnClickListener(v -> NavigationHelper.fastNavigate(this, MyBookingsActivity.class));
        findViewById(R.id.navProfile).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ProfileActivity.class));
    }
}
