package com.ensab.reservaapp.data;

import android.util.Log;
import com.ensab.reservaapp.BuildConfig;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * GeminiService gère les interactions avec l'API Generative AI de Google.
 * Elle permet d'envoyer des prompts et de recevoir des réponses asynchrones.
 */
public class GeminiService {
    private final GenerativeModelFutures model;
    private final Executor executor;

    /**
     * Initialise le modèle avec la clé API et la configuration spécifiée.
     * Utilise le modèle gemini-flash-lite pour des réponses rapides et économiques.
     */
    public GeminiService() {
        // Configuration par défaut (plus robuste selon les versions du SDK)
        com.google.ai.client.generativeai.type.GenerationConfig config = 
            new com.google.ai.client.generativeai.type.GenerationConfig.Builder()
                .build();

        GenerativeModel gm = new GenerativeModel(
                "gemini-flash-lite-latest",
                BuildConfig.GEMINI_API_KEY
        );
        this.model = GenerativeModelFutures.from(gm);
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Interface de rappel pour gérer la réponse ou l'erreur de l'IA.
     */
    public interface ChatCallback {
        void onResponse(String response);
        void onError(Throwable throwable);
    }

    /**
     * Envoie une requête à l'IA de manière asynchrone.
     * @param prompt Le texte de la question ou l'instruction système.
     * @param callback Callback pour traiter le résultat.
     */
    public void askGemini(String prompt, ChatCallback callback) {
        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        try {
            // Appel asynchrone via GenerativeModelFutures
            ListenableFuture<GenerateContentResponse> responseFuture = model.generateContent(content);

            responseFuture.addListener(() -> {
                try {
                    GenerateContentResponse result = responseFuture.get();
                    String text = result.getText();
                    if (text != null && !text.isEmpty()) {
                        callback.onResponse(text);
                    } else {
                        callback.onResponse("Désolé, je n'ai pas pu générer de réponse. Réessayez.");
                    }
                } catch (Exception e) {
                    Log.e("GeminiService", "Erreur de réponse: " + e.getMessage());
                    callback.onError(e);
                }
            }, executor);
        } catch (Exception e) {
            Log.e("GeminiService", "Erreur d'appel: " + e.getMessage());
            callback.onError(e);
        }
    }
}