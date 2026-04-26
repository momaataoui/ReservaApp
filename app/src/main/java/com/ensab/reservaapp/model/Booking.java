package com.ensab.reservaapp.model;

import com.google.firebase.firestore.Exclude;

public class Booking {
    private String id;
    private String userId;
    private String hotelId;
    private String hotelName;
    private String hotelLocation;
    private String hotelImageUrl;
    private long checkIn;
    private long checkOut;
    private int adults;
    private int children;
    private int totalPrice;
    private String status; // "pending", "confirmed", "cancelled"

    public Booking() {
        // Required for Firestore
    }

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public String getHotelLocation() { return hotelLocation; }
    public void setHotelLocation(String hotelLocation) { this.hotelLocation = hotelLocation; }

    public String getHotelImageUrl() { return hotelImageUrl; }
    public void setHotelImageUrl(String hotelImageUrl) { this.hotelImageUrl = hotelImageUrl; }

    public long getCheckIn() { return checkIn; }
    public void setCheckIn(long checkIn) { this.checkIn = checkIn; }

    public long getCheckOut() { return checkOut; }
    public void setCheckOut(long checkOut) { this.checkOut = checkOut; }

    public int getAdults() { return adults; }
    public void setAdults(int adults) { this.adults = adults; }

    public int getChildren() { return children; }
    public void setChildren(int children) { this.children = children; }

    public int getTotalPrice() { return totalPrice; }
    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
