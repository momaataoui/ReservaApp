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

/**
 * ChatViewModel gère la logique de la messagerie intelligente.
 * Il récupère les données des hôtels, construit les prompts pour Gemini,
 * et traite les réponses pour extraire les recommandations d'hôtels.
 */
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

    /**
     * Ajoute le message de bienvenue initial de l'assistant.
     */
    private void addInitialMessage() {
        addMessage("Hello! I'm your Smart Reserva assistant. How can I help you today?", false);
    }

    /**
     * Envoie le message de l'utilisateur à l'IA après avoir enrichi le contexte avec les données Firestore.
     * @param query La question ou la requête de l'utilisateur.
     */
    public void sendMessage(String query) {
        if (query == null || query.trim().isEmpty()) return;

        addMessage(query, true);
        _isLoading.setValue(true);

        // Récupération de la base de données locale pour l'envoyer comme contexte à l'IA
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

                // Construction du prompt avec instructions strictes
                String prompt = "You are Smart Reserva AI, a helpful hotel booking assistant. " +
                        "Here is our hotel database:\n" + hotelData.toString() +
                        "\nUser Message: " + query +
                        "\nInstructions: " +
                        "1. If the user says hello, asks how you are, or says thanks, reply politely without recommending a hotel. " +
                        "2. If the user is looking for a hotel, recommend the best match from the database. " +
                        "3. Only if you recommend a hotel, end your message with [HOTEL_ID:id_here]. " +
                        "4. Be friendly and brief.";

                geminiService.askGemini(prompt, new GeminiService.ChatCallback() {
                    @Override
                    public void onResponse(String aiResponse) {
                        processAIResponse(aiResponse);
                        _isLoading.postValue(false);
                    }

                    @Override
                    public void onError(Throwable t) {
                        android.util.Log.e("ChatViewModel", "Gemini Error: " + t.getMessage(), t);
                        
                        String userMessage = "Sorry, I encountered a technical error. Please try again.";
                        
                        // Gestion des quotas et de la disponibilité Google
                        if (t.getMessage() != null && t.getMessage().contains("429")) {
                            userMessage = "Request limit reached (Google Quota). Please wait a minute.";
                        } else if (t.getMessage() != null && (t.getMessage().contains("503") || t.getMessage().contains("UNAVAILABLE"))) {
                            userMessage = "The AI server is currently busy due to high demand. Please try again in a few seconds.";
                        } else if (t.getMessage() != null && t.getMessage().contains("Safety")) {
                            userMessage = "Sorry, I cannot answer this question for safety reasons.";
                        }

                        addMessage(userMessage, false);
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

    /**
     * Analyse la réponse de l'IA pour séparer le texte du tag d'ID d'hôtel.
     * @param response La réponse brute de Gemini.
     */
    private void processAIResponse(String response) {
        String hotelId = null;
        // Recherche du tag [HOTEL_ID:...] via Expression Régulière
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

    /**
     * Met à jour la liste des messages affichés dans le RecyclerView.
     */
    private void addMessage(String text, boolean isUser, String hotelId) {
        List<ChatMessage> current = new ArrayList<>(_messages.getValue());
        current.add(new ChatMessage(text, isUser, hotelId));
        _messages.postValue(current);
    }
}
