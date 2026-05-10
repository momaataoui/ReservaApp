package com.ensab.reservaapp.model;

/**
 * Modèle représentant un message dans la messagerie IA (Gemini).
 * Permet de distinguer les messages envoyés par l'utilisateur de ceux de l'IA.
 */
public class ChatMessage {
    private final String text;      // Contenu textuel du message
    private final boolean isUser;    // Vrai si le message vient du client, Faux si c'est Gemini
    private String hotelId;         // Optionnel : ID d'un hôtel recommandé par l'IA

    /**
     * Constructeur utilisé quand l'IA recommande un hôtel spécifique.
     */
    public ChatMessage(String text, boolean isUser, String hotelId) {
        this.text = text;
        this.isUser = isUser;
        this.hotelId = hotelId;
    }

    public String getText() { return text; }
    public boolean isUser() { return isUser; }
    public String getHotelId() { return hotelId; }
    
    /**
     * Vérifie si ce message contient une recommandation d'hôtel cliquable.
     */
    public boolean hasHotelAction() { return hotelId != null && !hotelId.isEmpty(); }
}