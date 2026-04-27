package com.ensab.reservaapp.model;

import java.util.ArrayList;
import java.util.List;

public class Hotel {
    private String id;
    private String name;
    private String city;
    private String description;
    private double price_per_night;
    private double rating;
    private String imageUrl; // Image principale
    private List<String> images; // Liste des images additionnelles

    public Hotel() {
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