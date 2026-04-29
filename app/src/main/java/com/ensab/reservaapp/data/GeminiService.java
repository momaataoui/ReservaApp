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

public class GeminiService {
    private final GenerativeModelFutures model;
    private final Executor executor;

    public GeminiService() {
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", BuildConfig.GEMINI_API_KEY);
        this.model = GenerativeModelFutures.from(gm);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public interface ChatCallback {
        void onResponse(String response);
        void onError(Throwable throwable);
    }

    public void askGemini(String prompt, ChatCallback callback) {
        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        try {
            ListenableFuture<GenerateContentResponse> responseFuture = model.generateContent(content);

            responseFuture.addListener(() -> {
                try {
                    GenerateContentResponse result = responseFuture.get();
                    String text = result.getText();
                    if (text != null && !text.isEmpty()) {
                        callback.onResponse(text);
                    } else {
                        callback.onResponse("I'm sorry, I couldn't generate a response. Please try again.");
                    }
                } catch (Exception e) {
                    Log.e("GeminiService", "Response error: " + e.getMessage());
                    callback.onError(e);
                }
            }, executor);
        } catch (Exception e) {
            Log.e("GeminiService", "Call error: " + e.getMessage());
            callback.onError(e);
        }
    }
}