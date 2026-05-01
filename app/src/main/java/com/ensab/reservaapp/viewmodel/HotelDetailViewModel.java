package com.ensab.reservaapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ensab.reservaapp.model.Hotel;
import com.ensab.reservaapp.repository.HotelRepository;

import java.util.List;
import java.util.Map;

public class HotelDetailViewModel extends ViewModel {
    private final HotelRepository repository = new HotelRepository();

    private final MutableLiveData<Hotel> _hotelDetails = new MutableLiveData<>();
    public LiveData<Hotel> hotelDetails = _hotelDetails;

    private final MutableLiveData<Boolean> _isFavorite = new MutableLiveData<>(false);
    public LiveData<Boolean> isFavorite = _isFavorite;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _bookingSuccess = new MutableLiveData<>();
    public LiveData<Boolean> bookingSuccess = _bookingSuccess;

    private String currentHotelId;

    public void initHotel(String hotelId) {
        if (hotelId == null || hotelId.isEmpty()) return;
        this.currentHotelId = hotelId;
        checkIfFavorite(hotelId);
        fetchHotelDetails(hotelId);
    }

    private void fetchHotelDetails(String hotelId) {
        _isLoading.setValue(true);
        repository.getHotelById(hotelId, hotel -> {
            _hotelDetails.setValue(hotel);
            _isLoading.setValue(false);
        }, exception -> {
            _errorMessage.setValue("Error loading hotel details: " + exception.getMessage());
            _isLoading.setValue(false);
        });
    }

    public void checkIfFavorite(String hotelId) {
        repository.observeFavorites(new HotelRepository.FavoritesCallback() {
            @Override
            public void onCallback(List<String> favoriteIds) {
                if (favoriteIds != null && hotelId != null) {
                    _isFavorite.setValue(favoriteIds.contains(hotelId));
                }
            }

            @Override
            public void onError(Exception e) {
                // Ignore or handle
            }
        });
    }

    public void toggleFavorite() {
        if (currentHotelId == null) {
            _errorMessage.setValue("Please log in to add to favorites");
            return;
        }

        boolean currentState = _isFavorite.getValue() != null ? _isFavorite.getValue() : false;
        
        repository.toggleFavorite(currentHotelId, currentState, () -> {
            // Note: We don't necessarily need to update _isFavorite manually here if observeFavorites is active, 
            // but we can to be safe if the listener delays. The observer will catch it anyway.
        }, exception -> {
            _errorMessage.setValue("Failed to update favorite status");
        });
    }

    public void performBooking(Map<String, Object> bookingData) {
        _isLoading.setValue(true);
        repository.performBooking(bookingData, () -> {
            _isLoading.setValue(false);
            _bookingSuccess.setValue(true);
        }, exception -> {
            _isLoading.setValue(false);
            _errorMessage.setValue("Error during booking: " + exception.getMessage());
            _bookingSuccess.setValue(false);
        });
    }
}
