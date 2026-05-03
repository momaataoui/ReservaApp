package com.ensab.reservaapp.model;

import com.google.firebase.firestore.Exclude;

/**
 * Modèle de données représentant une réservation d'hôtel.
 * Cette classe est utilisée pour mapper les documents de la collection 'bookings' dans Firestore.
 */
public class Booking {
    private String id;              // ID unique du document Firestore (exclu de la sérialisation via @Exclude)
    private String userId;          // ID de l'utilisateur qui a effectué la réservation
    private String hotelId;          // ID de l'hôtel réservé
    private String hotelName;        // Nom de l'hôtel (stocké pour éviter des jointures complexes)
    private String hotelLocation;    // Localisation de l'hôtel
    private String hotelImageUrl;    // Image de l'hôtel pour l'affichage dans la liste des réservations
    private long checkIn;           // Date d'arrivée (Timestamp en millisecondes)
    private long checkOut;          // Date de départ (Timestamp en millisecondes)
    private int adults;             // Nombre d'adultes
    private int children;           // Nombre d'enfants
    private int totalPrice;         // Prix total de la réservation
    private String status;          // État : "Pending" (attente), "Confirmed" (validé), "Cancelled" (annulé)

    public Booking() {
        // Constructeur vide requis par Firebase Firestore pour la désérialisation
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
