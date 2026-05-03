package com.ensab.reservaapp.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.data.ImageLoader;
import com.ensab.reservaapp.model.Hotel;
import com.ensab.reservaapp.view.activity.HotelDetailActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * UnifiedHotelAdapter est un adaptateur polyvalent utilisé pour afficher les listes d'hôtels.
 * Il gère deux types de mise en page : horizontale (recommandations) et verticale (résultats de recherche).
 * Utilise 'ListAdapter' pour des mises à jour fluides via DiffUtil.
 */
public class UnifiedHotelAdapter extends ListAdapter<Hotel, UnifiedHotelAdapter.HotelViewHolder> {

    private final boolean isHorizontal; // Vrai pour le scroll horizontal sur l'accueil
    private final OnFavoriteClickListener favoriteClickListener;
    private List<String> favoriteIds = new ArrayList<>();

    /**
     * Interface pour intercepter le clic sur le bouton "Favoris".
     */
    public interface OnFavoriteClickListener {
        void onFavoriteClick(Hotel hotel, int position);
    }

    public UnifiedHotelAdapter(boolean isHorizontal, OnFavoriteClickListener favoriteClickListener) {
        super(new HotelDiffCallback());
        this.isHorizontal = isHorizontal;
        this.favoriteClickListener = favoriteClickListener;
    }

    /**
     * Met à jour la liste des favoris de l'utilisateur pour rafraîchir l'icône coeur.
     */
    public void setFavoriteIds(List<String> favoriteIds) {
        this.favoriteIds = favoriteIds != null ? favoriteIds : new ArrayList<>();
        notifyItemRangeChanged(0, getItemCount());
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sélection dynamique du layout selon le mode d'affichage
        int layout = isHorizontal ? R.layout.item_hotel_horizontal : R.layout.item_hotel;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = getItem(position);
        Context context = holder.itemView.getContext();

        // Remplissage des données textuelles
        holder.tvName.setText(hotel.getName());
        holder.tvCity.setText(hotel.getCity());
        holder.tvRating.setText(String.valueOf(hotel.getRating()));
        
        // Formatage du prix
        String priceText = String.format(Locale.FRANCE, "%,d", (int)hotel.getPrice_per_night());
        if (isHorizontal) {
            holder.tvPrice.setText(context.getString(R.string.mad_currency, priceText));
        } else {
            holder.tvPrice.setText(priceText);
        }

        // Chargement de l'image via l'ImageLoader personnalisé
        ImageLoader.getInstance().load(hotel.getImageUrl(), holder.ivPhoto, R.drawable.ic_launcher_background);

        // Gestion de l'état "Favori" (coeur rempli ou vide)
        boolean isFav = favoriteIds.contains(hotel.getId());
        holder.btnFavorite.setImageResource(isFav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        holder.btnFavorite.setColorFilter(ContextCompat.getColor(context, R.color.white));

        // Clic sur le favori
        holder.btnFavorite.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(getItem(pos), pos);
            }
        });

        // Clic sur l'item pour ouvrir les détails de l'hôtel
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HotelDetailActivity.class);
            // Passage des données via Intent pour un affichage immédiat sans attendre Firebase
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_ID, hotel.getId());
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_NAME, hotel.getName());
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_LOCATION, hotel.getCity() + ", Morocco");
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_PRICE, (int) hotel.getPrice_per_night());
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_RATING, (float) hotel.getRating());
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_DESC, hotel.getDescription());
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_IMAGE_URL, hotel.getImageUrl());
            
            if (hotel.getImages() != null && !hotel.getImages().isEmpty()) {
                intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_IMAGES, hotel.getImages().toArray(new String[0]));
            }

            context.startActivity(intent);
        });
    }

    /**
     * Conteneur des vues pour chaque item de la liste.
     */
    public static class HotelViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvName, tvCity, tvRating, tvPrice;
        ImageButton btnFavorite;

        HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivHotelPhoto);
            tvName = itemView.findViewById(R.id.tvHotelName);
            tvCity = itemView.findViewById(R.id.tvHotelCity);
            tvRating = itemView.findViewById(R.id.tvHotelRating);
            tvPrice = itemView.findViewById(R.id.tvHotelPrice);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }

    /**
     * Classe utilitaire pour optimiser les mises à jour de la liste.
     * Ne recharge que ce qui a changé.
     */
    static class HotelDiffCallback extends DiffUtil.ItemCallback<Hotel> {
        @Override
        public boolean areItemsTheSame(@NonNull Hotel oldItem, @NonNull Hotel newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Hotel oldItem, @NonNull Hotel newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getCity().equals(newItem.getCity()) &&
                   oldItem.getPrice_per_night() == newItem.getPrice_per_night() &&
                   oldItem.getRating() == newItem.getRating() &&
                   String.valueOf(oldItem.getImageUrl()).equals(String.valueOf(newItem.getImageUrl()));
        }
    }
}