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
import com.google.android.material.datepicker.MaterialDatePicker;
import java.util.ArrayList;

/**
 * HotelListActivity est l'écran principal de recherche d'hôtels pour le client.
 * Elle affiche des recommandations (horizontal) et tous les hôtels (vertical) avec filtrage.
 */
public class HotelListActivity extends AppCompatActivity {

    private ActivityHotelListBinding binding;
    private UnifiedHotelAdapter adapter;             // Adaptateur vertical pour tous les résultats
    private UnifiedHotelAdapter horizontalAdapter;   // Adaptateur horizontal pour les recommandations
    private HotelListViewModel viewModel;            // ViewModel pour la logique métier

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHotelListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ajustement pour la barre de navigation Android
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.bottomNavContainer.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Configuration des listes RecyclerView
        binding.rvHotels.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UnifiedHotelAdapter(false, (hotel, position) -> viewModel.toggleFavorite(hotel));
        binding.rvHotels.setAdapter(adapter);

        binding.rvHotelsHorizontal.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        horizontalAdapter = new UnifiedHotelAdapter(true, (hotel, position) -> viewModel.toggleFavorite(hotel));
        binding.rvHotelsHorizontal.setAdapter(horizontalAdapter);

        // Liaison avec le ViewModel
        viewModel = new ViewModelProvider(this).get(HotelListViewModel.class);

        // Observation des données (Live Data)
        viewModel.userName.observe(this, name -> {
            if (!name.isEmpty()) binding.tvWelcomeName.setText("Hey, " + name);
        });

        // Mise à jour de la liste principale quand les filtres changent
        viewModel.filteredHotels.observe(this, hotels -> {
            adapter.submitList(hotels);
            binding.tvHotelCount.setText(hotels.size() + " résultats");
        });

        // Mise à jour des recommandations "Top Hotels"
        viewModel.topRelevantHotels.observe(this, hotels -> {
            horizontalAdapter.submitList(hotels);
        });

        // Mise à jour de l'état des favoris (coeurs)
        viewModel.favoriteIds.observe(this, ids -> {
            adapter.setFavoriteIds(ids);
            horizontalAdapter.setFavoriteIds(ids);
        });

        // Affichage de l'indicateur de chargement
        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Bouton "Afficher tout" / "Réduire"
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

        setupSearch();      // Configuration de la barre de recherche
        setupFilters();     // Configuration des puces (Chips) de filtrage
        setupNotifications();
        
        NavigationHelper.setSelectedItem(this, R.id.navDiscover);
        setupBottomNavLinks();
    }

    /**
     * Configure l'ouverture de la recherche avancée via un BottomSheet.
     */
    private void setupSearch() {
        binding.cvSearchBar.setOnClickListener(v -> showSearchBottomSheet());
    }

    /**
     * Affiche un menu flottant pour la recherche par ville.
     */
    private void showSearchBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.layout_search_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);

        EditText etSheetSearch = sheetView.findViewById(R.id.etSheetSearch);
        
        // Raccourcis pour les villes populaires
        sheetView.findViewById(R.id.destCasablanca).setOnClickListener(v -> updateSearch("Casablanca", bottomSheetDialog));
        sheetView.findViewById(R.id.destMarrakesh).setOnClickListener(v -> updateSearch("Marrakech", bottomSheetDialog));
        sheetView.findViewById(R.id.destRabat).setOnClickListener(v -> updateSearch("Rabat", bottomSheetDialog));
        sheetView.findViewById(R.id.destTangier).setOnClickListener(v -> updateSearch("Tanger", bottomSheetDialog));

        // Recherche textuelle dynamique
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

    /**
     * Configure les actions sur les Chips de filtrage (Prix, Note, Luxe).
     */
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

    /**
     * Gère la navigation entre les différents onglets de l'application.
     */
    private void setupBottomNavLinks() {
        findViewById(R.id.navSaved).setOnClickListener(v -> NavigationHelper.fastNavigate(this, WishlistActivity.class));
        findViewById(R.id.navProfile).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ProfileActivity.class));
        findViewById(R.id.navBookings).setOnClickListener(v -> NavigationHelper.fastNavigate(this, MyBookingsActivity.class));
        findViewById(R.id.navDiscover).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ChoiceActivity.class));
    }
}
