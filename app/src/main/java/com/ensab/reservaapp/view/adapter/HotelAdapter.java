package com.ensab.reservaapp.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.model.Hotel;
import com.ensab.reservaapp.view.activity.HotelDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    private List<Hotel> hotels;
    private Context context;
    private List<String> favoriteHotelIds = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public HotelAdapter(List<Hotel> hotels, Context context) {
        this.hotels = hotels;
        this.context = context;
        loadFavorites();
    }

    private void loadFavorites() {
        if (mAuth.getCurrentUser() == null) return;
        
        db.collection("users").document(mAuth.getCurrentUser().getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> favorites = (List<String>) documentSnapshot.get("favorites");
                    if (favorites != null) {
                        favoriteHotelIds = favorites;
                        notifyDataSetChanged();
                    }
                }
            });
    }

    public void updateHotels(List<Hotel> newHotels) {
        this.hotels = newHotels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotels.get(position);
        holder.tvName.setText(hotel.getName());
        holder.tvCity.setText(hotel.getCity());
        holder.tvPrice.setText(String.valueOf((int)hotel.getPrice_per_night()));
        holder.tvRating.setText(String.valueOf(hotel.getRating()));

        if (hotel.getImageUrl() != null && !hotel.getImageUrl().isEmpty()) {
            Glide.with(context)
                .load(hotel.getImageUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.ivPhoto);
        }

        // Update heart icon based on favorite status
        boolean isFavorite = favoriteHotelIds.contains(hotel.getId());
        holder.btnFavorite.setImageResource(isFavorite ? R.drawable.ic_saved_filled : R.drawable.ic_favorite_border);

        holder.btnFavorite.setOnClickListener(v -> toggleFavorite(hotel, position));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HotelDetailActivity.class);
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_ID, hotel.getId());
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_NAME, hotel.getName());
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_LOCATION, hotel.getCity() + ", Maroc");
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_PRICE, (int) hotel.getPrice_per_night());
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_RATING, (float) hotel.getRating());
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_DESC, hotel.getDescription());
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_IMAGE_URL, hotel.getImageUrl());
            context.startActivity(intent);
        });
    }

    private void toggleFavorite(Hotel hotel, int position) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(context, "Connectez-vous pour ajouter aux favoris", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        boolean isCurrentlyFavorite = favoriteHotelIds.contains(hotel.getId());

        if (isCurrentlyFavorite) {
            // Remove from favorites
            db.collection("users").document(userId)
                .update("favorites", FieldValue.arrayRemove(hotel.getId()))
                .addOnSuccessListener(aVoid -> {
                    favoriteHotelIds.remove(hotel.getId());
                    notifyItemChanged(position);
                    Toast.makeText(context, "Retiré des favoris", Toast.LENGTH_SHORT).show();
                });
        } else {
            // Add to favorites
            db.collection("users").document(userId)
                .update("favorites", FieldValue.arrayUnion(hotel.getId()))
                .addOnSuccessListener(aVoid -> {
                    favoriteHotelIds.add(hotel.getId());
                    notifyItemChanged(position);
                    Toast.makeText(context, "Ajouté aux favoris ❤️", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Si le champ n'existe pas encore, on crée l'array
                    db.collection("users").document(userId)
                        .set(new java.util.HashMap<String, Object>() {{
                            put("favorites", java.util.Arrays.asList(hotel.getId()));
                        }}, com.google.firebase.firestore.SetOptions.merge());
                    favoriteHotelIds.add(hotel.getId());
                    notifyItemChanged(position);
                });
        }
    }

    @Override
    public int getItemCount() {
        return hotels.size();
    }

    public static class HotelViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvName, tvCity, tvRating, tvPrice;
        ImageButton btnFavorite;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivHotelPhoto);
            tvName = itemView.findViewById(R.id.tvHotelName);
            tvCity = itemView.findViewById(R.id.tvHotelCity);
            tvRating = itemView.findViewById(R.id.tvHotelRating);
            tvPrice = itemView.findViewById(R.id.tvHotelPrice);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
