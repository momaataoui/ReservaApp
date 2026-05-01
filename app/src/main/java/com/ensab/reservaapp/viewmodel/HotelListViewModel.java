package com.ensab.reservaapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ensab.reservaapp.model.Hotel;
import com.ensab.reservaapp.repository.HotelRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HotelListViewModel extends ViewModel {
    private final HotelRepository repository = new HotelRepository();
    
    private final MutableLiveData<List<Hotel>> _allHotels = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Hotel>> _filteredHotels = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Hotel>> filteredHotels = _filteredHotels;

    private final MutableLiveData<List<Hotel>> _topRelevantHotels = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Hotel>> topRelevantHotels = _topRelevantHotels;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _userName = new MutableLiveData<>("");
    public LiveData<String> userName = _userName;

    private final MutableLiveData<List<String>> _favoriteIds = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<String>> favoriteIds = _favoriteIds;


    public HotelListViewModel() {
        loadUserName();
        loadHotels();
        observeFavorites();
    }

    public void observeFavorites() {
        repository.observeFavorites(new HotelRepository.FavoritesCallback() {
            @Override
            public void onCallback(List<String> favoriteIds) {
                _favoriteIds.setValue(favoriteIds);
            }

            @Override
            public void onError(Exception e) {
                // Handle error
            }
        });
    }

    public void loadFavorites() {
        repository.getFavorites(new HotelRepository.FavoritesCallback() {
            @Override
            public void onCallback(List<String> favoriteIds) {
                _favoriteIds.setValue(favoriteIds);
            }

            @Override
            public void onError(Exception e) {
                // Handle error
            }
        });
    }

    public void toggleFavorite(Hotel hotel) {
        List<String> currentFavs = _favoriteIds.getValue();
        if (currentFavs == null) return;
        boolean isFavorite = currentFavs.contains(hotel.getId());

        repository.toggleFavorite(hotel.getId(), isFavorite, this::loadFavorites, e -> {});
    }

    private void loadUserName() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String fullName = documentSnapshot.getString("fullName");
                    if (fullName != null && !fullName.isEmpty()) {
                        _userName.setValue(fullName.split(" ")[0]);
                    } else {
                        String email = auth.getCurrentUser().getEmail();
                        if (email != null) {
                            String name = email.split("@")[0];
                            _userName.setValue(name.substring(0, 1).toUpperCase() + name.substring(1));
                        }
                    }
                });
        }
    }

    private final MutableLiveData<List<Hotel>> _wishlistHotels = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Hotel>> wishlistHotels = _wishlistHotels;

    public void loadHotelsByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            _wishlistHotels.setValue(new ArrayList<>());
            return;
        }
        
        _isLoading.setValue(true);
        repository.getHotelsByIds(ids, new HotelRepository.HotelCallback() {
            @Override
            public void onCallback(List<Hotel> hotels) {
                _wishlistHotels.setValue(hotels);
                _isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false);
            }
        });
    }

    public void loadHotels() {
        _isLoading.setValue(true);
        repository.getAllHotels(new HotelRepository.HotelCallback() {
            @Override
            public void onCallback(List<Hotel> hotels) {
                _allHotels.setValue(hotels);
                _filteredHotels.setValue(hotels);
                
                // Extraire les 4 mieux notés pour "Most Relevant"
                List<Hotel> top4 = new ArrayList<>(hotels);
                top4.sort((h1, h2) -> Double.compare(h2.getRating(), h1.getRating()));
                _topRelevantHotels.setValue(top4.stream().limit(4).collect(Collectors.toList()));

                _isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false);
            }
        });
    }

    public void filterHotels(String query) {
        String lowerQuery = query.toLowerCase().trim();
        List<Hotel> all = _allHotels.getValue();
        if (all == null) return;

        if (lowerQuery.isEmpty()) {
            _filteredHotels.setValue(all);
        } else {
            List<Hotel> filtered = all.stream()
                .filter(h -> h.getName().toLowerCase().contains(lowerQuery) || 
                            h.getCity().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
            _filteredHotels.setValue(filtered);
        }
    }

    public void filterLuxe() {
        List<Hotel> all = _allHotels.getValue();
        if (all != null) {
            _filteredHotels.setValue(all.stream()
                .filter(h -> h.getPrice_per_night() >= 1300)
                .collect(Collectors.toList()));
        }
    }

    private boolean isPriceAscending = true;
    private boolean isRatingDescending = true;

    public void sortByPrice() {
        List<Hotel> current = _filteredHotels.getValue();
        if (current == null) return;
        List<Hotel> sorted = new ArrayList<>(current);
        if (isPriceAscending) {
            sorted.sort((h1, h2) -> Double.compare(h2.getPrice_per_night(), h1.getPrice_per_night()));
        } else {
            sorted.sort((h1, h2) -> Double.compare(h1.getPrice_per_night(), h2.getPrice_per_night()));
        }
        isPriceAscending = !isPriceAscending;
        _filteredHotels.setValue(sorted);
    }

    public void sortByRating() {
        List<Hotel> current = _filteredHotels.getValue();
        if (current == null) return;
        List<Hotel> sorted = new ArrayList<>(current);
        if (isRatingDescending) {
            sorted.sort((h1, h2) -> Double.compare(h1.getRating(), h2.getRating()));
        } else {
            sorted.sort((h1, h2) -> Double.compare(h2.getRating(), h1.getRating()));
        }
        isRatingDescending = !isRatingDescending;
        _filteredHotels.setValue(sorted);
    }


}