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
        addMessage("Bonjour ! Je suis votre assistant Smart Reserva. Comment puis-je vous aider aujourd'hui ?", false);
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
                    hotelData.append("- Nom: ").append(hotel.getName())
                            .append(", Ville: ").append(hotel.getCity())
                            .append(", Prix: ").append(hotel.getPrice_per_night())
                            .append(", ID: ").append(hotel.getId()).append("\n");
                }

                // Construction du prompt avec instructions strictes
                String prompt = "Tu es Smart Reserva AI, un assistant hôtelier utile. " +
                        "Voici notre base de données d'hôtels :\n" + hotelData.toString() +
                        "\nMessage de l'utilisateur : " + query +
                        "\nInstructions : " +
                        "1. Si l'utilisateur dit bonjour, demande comment vous allez ou dit merci, réponds poliment sans recommander d'hôtel. " +
                        "2. Si l'utilisateur cherche un hôtel, recommande le meilleur match de la base de données. " +
                        "3. Uniquement si tu recommandes un hôtel, termine ton message par [HOTEL_ID:id_ici]. " +
                        "4. Sois amical et bref.";

                geminiService.askGemini(prompt, new GeminiService.ChatCallback() {
                    @Override
                    public void onResponse(String aiResponse) {
                        processAIResponse(aiResponse);
                        _isLoading.postValue(false);
                    }

                    @Override
                    public void onError(Throwable t) {
                        android.util.Log.e("ChatViewModel", "Erreur Gemini: " + t.getMessage(), t);
                        
                        String userMessage = "Désolé, j'ai rencontré une erreur technique. Réessayez.";
                        
                        // Gestion des quotas et de la sécurité Google
                        if (t.getMessage() != null && t.getMessage().contains("429")) {
                            userMessage = "Limite de requêtes atteinte (Quota Google). Veuillez patienter une minute.";
                        } else if (t.getMessage() != null && t.getMessage().contains("Safety")) {
                            userMessage = "Désolé, je ne peux pas répondre à cette question pour des raisons de sécurité.";
                        }

                        addMessage(userMessage, false);
                        _isLoading.postValue(false);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                addMessage("Erreur de base de données. Réessayez.", false);
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
