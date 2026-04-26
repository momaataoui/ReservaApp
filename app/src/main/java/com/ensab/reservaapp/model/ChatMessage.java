package com.ensab.reservaapp.model;

public class ChatMessage {
    private final String text;
    private final boolean isUser;
    private String hotelId;

    public ChatMessage(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }

    public ChatMessage(String text, boolean isUser, String hotelId) {
        this.text = text;
        this.isUser = isUser;
        this.hotelId = hotelId;
    }

    public String getText() { return text; }
    public boolean isUser() { return isUser; }
    public String getHotelId() { return hotelId; }
    public boolean hasHotelAction() { return hotelId != null && !hotelId.isEmpty(); }
}