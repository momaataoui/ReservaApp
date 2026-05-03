package com.ensab.reservaapp.model;

/**
 * Modèle de données représentant un utilisateur du système.
 * Gère à la fois les profils Clients et Administrateurs via le champ 'role'.
 */
public class User {
    private String id;              // UID généré par Firebase Auth
    private String fullName;        // Nom complet de l'utilisateur
    private String email;           // Adresse email (identifiant de connexion)
    private String phone;           // Numéro de téléphone
    private String role;            // Rôle : "client" ou "admin"
    private String profileImage;    // URL de la photo de profil (Bitmap décodeur utilisé)
    private boolean disabled;       // État du compte (utilisé par l'admin pour bannir/désactiver)

    public User() {
        // Constructeur vide requis par Firebase Firestore
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }
}
