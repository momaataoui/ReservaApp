package com.ensab.reservaapp.data;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe d'aide pour interagir avec les services Firebase, y compris Firestore.
 * Cette classe centralise les opérations Firebase pour rendre d'autres parties de l'application plus propres.
 */
public class FirebaseHelper {
    // TAG pour les messages de journalisation de cette classe
    private static final String TAG = "FirebaseHelper";
    // Instance de la base de données Firestore
    private final FirebaseFirestore db;

    /**
     * Interface de rappel générique pour les opérations asynchrones.
     * @param <T> Le type du résultat en cas de succès.
     */
    public interface Callback<T> {
        /**
         * Appelée lorsque l'opération asynchrone se termine avec succès.
         * @param result Le résultat de l'opération.
         */
        void onSuccess(T result);

        /**
         * Appelée lorsque l'opération asynchrone échoue.
         * @param error Une chaîne de caractères décrivant l'erreur.
         */
        void onFailure(String error);
    }

    /**
     * Constructeur pour FirebaseHelper.
     * Initialise l'instance de Firebase Firestore.
     */
    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Vérifie si la collection "hotels" est vide et, si c'est le cas, insère des données d'hôtel d'exemple.
     * Ceci est utile pour la configuration initiale ou les environnements de développement.
     * @param callback Un Callback<Void> pour signaler l'achèvement de l'opération (succès ou échec).
     */
    public void insertSampleDataIfEmpty(Callback<Void> callback) {
        Log.d(TAG, "Vérification si des données d'exemple doivent être insérées...");
        // Limiter à 1 document pour vérifier efficacement si la collection contient des données
        db.collection("hotels").limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Si aucun document n'est trouvé, la collection est vide
                if (task.getResult().isEmpty()) {
                    Log.d(TAG, "La collection d'hôtels est vide. Insertion des données d'exemple...");
                    
                    // Insérer des données d'hôtel d'exemple prédéfinies
                    insertHotel("La Mamounia Marrakech", "Marrakech", "Palais légendaire au cœur de la médina.", 4500, 4.9, "https://images.unsplash.com/photo-1590073844006-33379778ae09?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Sofitel Agadir Thalassa", "Agadir", "Resort 5 étoiles face à l'océan.", 2200, 4.6, "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Royal Mansour Casablanca", "Casablanca", "Architecture art-déco majestueuse.", 3500, 4.8, "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80");
                    insertHotel("The View Agadir", "Agadir", "Hôtel moderne avec vue imprenable.", 1800, 4.5, "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Riad Fès", "Fès", "Riad authentique du XIVe siècle.", 2800, 4.9, "https://images.unsplash.com/photo-1544124499-58912cbddaad?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Four Seasons Resort Marrakech", "Marrakech", "Luxe contemporain dans un jardin mauresque.", 5200, 4.9, "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Mazagan Beach Resort", "El Jadida", "Destination de rêve avec golf et casino.", 2600, 4.7, "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Michlifen Resort & Golf", "Ifrane", "Chalet de luxe dans le Petit Suisse marocain.", 3100, 4.8, "https://images.unsplash.com/photo-1518733057094-95b53143d2a7?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Hilton Tangier Al Houara", "Tanger", "Resort balnéaire d'exception.", 2100, 4.6, "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Banyan Tree Tamouda Bay", "Tétouan", "Villas avec piscine privée au bord de la Méditerranée.", 6500, 4.9, "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?auto=format&fit=crop&w=800&q=80");

                    // Nouvelles Régions
                    insertHotel("Hôtel Rabat Marriott", "Rabat", "Luxe moderne au centre de la capitale.", 2900, 4.7, "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Heure Bleue Palais", "Essaouira", "Relais & Châteaux historique.", 2400, 4.8, "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Lina Ryad & Spa", "Chefchaouen", "Havre de paix dans la ville bleue.", 1500, 4.6, "https://images.unsplash.com/photo-1548013146-72479768bbaa?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Berber Palace", "Ouarzazate", "Le repaire des stars de cinéma.", 1900, 4.5, "https://images.unsplash.com/photo-1596436889106-be35e843f974?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Dakhla Club Hotel & Spa", "Dakhla", "Kitesurf et détente entre désert et océan.", 2700, 4.7, "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Hyatt Regency Taghazout", "Agadir", "Surfez dans le luxe absolu.", 3200, 4.8, "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=800&q=80");

                    // Notifier le succès après avoir tenté d'insérer tous les hôtels.
                    // Remarque : Les échecs d'insertion d'hôtels individuels sont journalisés en interne.
                    callback.onSuccess(null);
                } else {
                    Log.d(TAG, "La collection d'hôtels contient déjà des données. Aucune donnée d'exemple insérée.");
                    callback.onSuccess(null); // La collection n'est pas vide, c'est toujours une vérification réussie
                }
            } else {
                // Journaliser et signaler toute erreur rencontrée lors de la vérification de la collection
                Log.e(TAG, "Erreur lors de la vérification de la collection : " + task.getException().getMessage());
                callback.onFailure(task.getException().getMessage());
            }
        });
    }

    /**
     * Insère un document d'hôtel unique dans la collection "hotels" de Firestore.
     * Journalise le succès ou l'échec de l'insertion.
     * @param name Le nom de l'hôtel.
     * @param city La ville où l'hôtel est situé.
     * @param desc Une description de l'hôtel.
     * @param price Le prix par nuit pour l'hôtel.
     * @param rating La note de l'hôtel (par exemple, sur 5).
     * @param imageUrl L'URL d'une image pour l'hôtel.
     */
    private void insertHotel(String name, String city, String desc, double price, double rating, String imageUrl) {
        // Créer une HashMap pour stocker les données de l'hôtel
        Map<String, Object> hotel = new HashMap<>();
        hotel.put("name", name);
        hotel.put("city", city);
        hotel.put("description", desc);
        hotel.put("price_per_night", price);
        hotel.put("rating", rating);
        hotel.put("imageUrl", imageUrl);
        
        // Ajouter la map de l'hôtel comme nouveau document à la collection "hotels"
        db.collection("hotels").add(hotel)
            .addOnSuccessListener(documentReference -> Log.d(TAG, "Hôtel inséré avec succès : " + name + " avec l'ID : " + documentReference.getId()))
            .addOnFailureListener(e -> Log.e(TAG, "Erreur lors de l'insertion de l'hôtel " + name + " : " + e.getMessage()));
    }
}
