package com.ensab.reservaapp.data;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public interface Callback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public void getHotels(OnCompleteListener<QuerySnapshot> callback) {
        db.collection("hotels").get().addOnCompleteListener(callback);
    }

    public void insertSampleDataIfEmpty(Callback<Void> callback) {
        Log.d(TAG, "Checking if sample data needs to be inserted...");
        db.collection("hotels").limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    Log.d(TAG, "Hotels collection is empty. Inserting sample data...");
                    
                    insertHotel("La Mamounia Marrakech", "Marrakech", "Palais légendaire au cœur de la médina.", 4500, 4.9, "https://images.unsplash.com/photo-1590073844006-33379778ae09?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Sofitel Agadir Thalassa", "Agadir", "Resort 5 étoiles face à l'océan.", 2200, 4.6, "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Royal Mansour Casablanca", "Casablanca", "Architecture art-déco majestueuse.", 3500, 4.8, "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80");
                    insertHotel("The View Agadir", "Agadir", "Hôtel moderne avec vue imprenable.", 1800, 4.5, "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Riad Fès", "Fès", "Riad authentique du XIVe siècle.", 2800, 4.9, "https://images.unsplash.com/photo-1544124499-58912cbddaad?auto=format&fit=crop&w=800&q=80");
                    
                    // Nouveaux hôtels
                    insertHotel("Four Seasons Resort Marrakech", "Marrakech", "Luxe contemporain dans un jardin mauresque.", 5200, 4.9, "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Mazagan Beach Resort", "El Jadida", "Destination de rêve avec golf et casino.", 2600, 4.7, "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Michlifen Resort & Golf", "Ifrane", "Chalet de luxe dans le Petit Suisse marocain.", 3100, 4.8, "https://images.unsplash.com/photo-1518733057094-95b53143d2a7?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Hilton Tangier Al Houara", "Tanger", "Resort balnéaire d'exception.", 2100, 4.6, "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80");
                    insertHotel("Banyan Tree Tamouda Bay", "Tétouan", "Villas avec piscine privée au bord de la Méditerranée.", 6500, 4.9, "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?auto=format&fit=crop&w=800&q=80");

                    callback.onSuccess(null);
                } else {
                    Log.d(TAG, "Hotels collection already contains data.");
                    callback.onSuccess(null);
                }
            } else {
                Log.e(TAG, "Error checking collection: " + task.getException().getMessage());
                callback.onFailure(task.getException().getMessage());
            }
        });
    }

    private void insertHotel(String name, String city, String desc, double price, double rating, String imageUrl) {
        Map<String, Object> hotel = new HashMap<>();
        hotel.put("name", name);
        hotel.put("city", city);
        hotel.put("description", desc);
        hotel.put("price_per_night", price);
        hotel.put("rating", rating);
        hotel.put("imageUrl", imageUrl);
        
        db.collection("hotels").add(hotel)
            .addOnSuccessListener(documentReference -> Log.d(TAG, "Hotel inserted: " + name))
            .addOnFailureListener(e -> Log.e(TAG, "Error inserting hotel " + name + ": " + e.getMessage()));
    }
}
