package com.ensab.reservaapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Modèle de données représentant un établissement hôtelier.
 * Utilisé pour la collection 'hotels' dans Firestore.
 */
public class Hotel {
    private String id;              // ID unique généré par Firestore
    private String name;            // Nom de l'hôtel
    private String city;            // Ville où se situe l'hôtel (utilisée pour le filtrage)
    private String description;      // Description détaillée
    private double price_per_night; // Prix par nuit
    private double rating;          // Note moyenne (ex: 4.5)
    private String imageUrl;        // URL de l'image principale (couverture)
    private List<String> images;    // Galerie d'images additionnelles

    public Hotel() {
        // Initialisation de la liste pour éviter les NullPointerException
        this.images = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice_per_night() { return price_per_night; }
    public void setPrice_per_night(double price_per_night) { this.price_per_night = price_per_night; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}