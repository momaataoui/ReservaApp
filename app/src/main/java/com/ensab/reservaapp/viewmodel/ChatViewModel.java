package com.ensab.reservaapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ensab.reservaapp.data.GeminiService;
import com.ensab.reservaapp.model.ChatMessage;
import com.ensab.reservaapp.model.Hotel;
import com.ensab.reservaapp.repository.HotelRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatViewModel extends ViewModel {
    private final MutableLiveData<List<ChatMessage>> _messages = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<ChatMessage>> messages = _messages;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final HotelRepository hotelRepository = new HotelRepository();
    private final GeminiService geminiService = new GeminiService();

    public ChatViewModel() {
        addInitialMessage();
    }

    private void addInitialMessage() {
        addMessage("Hello! I'm your Smart Reserva assistant. How can I help you today?", false);
    }

    public void sendMessage(String query) {
        if (query == null || query.trim().isEmpty()) return;

        addMessage(query, true);
        _isLoading.setValue(true);

        hotelRepository.getAllHotels(new HotelRepository.HotelCallback() {
            @Override
            public void onCallback(List<Hotel> hotels) {
                StringBuilder hotelData = new StringBuilder();
                for (Hotel hotel : hotels) {
                    hotelData.append("- Name: ").append(hotel.getName())
                            .append(", City: ").append(hotel.getCity())
                            .append(", Price: ").append(hotel.getPrice_per_night())
                            .append(", ID: ").append(hotel.getId()).append("\n");
                }

                String prompt = "You are Smart Reserva AI. Database:\n" + hotelData.toString() +
                        "\nUser Question: " + query +
                        "\nRules: Recommend best hotel. End with [HOTEL_ID:id]. Be brief.";

                geminiService.askGemini(prompt, new GeminiService.ChatCallback() {
                    @Override
                    public void onResponse(String aiResponse) {
                        processAIResponse(aiResponse);
                        _isLoading.postValue(false);
                    }

                    @Override
                    public void onError(Throwable t) {
                        addMessage("Sorry, I reached my limit. Please try again later.", false);
                        _isLoading.postValue(false);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                addMessage("Database error. Please try again.", false);
                _isLoading.postValue(false);
            }
        });
    }

    private void processAIResponse(String response) {
        String hotelId = null;
        Pattern p = Pattern.compile("\\[HOTEL_ID:([^\\]]+)\\]");
        Matcher m = p.matcher(response);
        String cleanText = response;
        if (m.find()) {
            hotelId = m.group(1).trim();
            cleanText = response.replace(m.group(0), "").trim();
        }
        addMessage(cleanText, false, hotelId);
    }

    private void addMessage(String text, boolean isUser) {
        addMessage(text, isUser, null);
    }

    private void addMessage(String text, boolean isUser, String hotelId) {
        List<ChatMessage> current = new ArrayList<>(_messages.getValue());
        current.add(new ChatMessage(text, isUser, hotelId));
        _messages.postValue(current);
    }
}
