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

public class UnifiedHotelAdapter extends ListAdapter<Hotel, UnifiedHotelAdapter.HotelViewHolder> {

    private final boolean isHorizontal;
    private final OnFavoriteClickListener favoriteClickListener;
    private List<String> favoriteIds = new ArrayList<>();

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Hotel hotel, int position);
    }

    public UnifiedHotelAdapter(boolean isHorizontal, OnFavoriteClickListener favoriteClickListener) {
        super(new HotelDiffCallback());
        this.isHorizontal = isHorizontal;
        this.favoriteClickListener = favoriteClickListener;
    }

    public void setFavoriteIds(List<String> favoriteIds) {
        this.favoriteIds = favoriteIds != null ? favoriteIds : new ArrayList<>();
        // Note: Using submitList again with the same list reference won't trigger DiffUtil.
        // For favorites, since they are managed externally to the Hotel model,
        // we use notifyDataSetChanged or ideally incorporate 'isFavorite' into the UI model.
        notifyItemRangeChanged(0, getItemCount());
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = isHorizontal ? R.layout.item_hotel_horizontal : R.layout.item_hotel;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = getItem(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(hotel.getName());
        holder.tvCity.setText(hotel.getCity());
        holder.tvRating.setText(String.valueOf(hotel.getRating()));
        
        String priceText = String.format(Locale.FRANCE, "%,d", (int)hotel.getPrice_per_night());
        if (isHorizontal) {
            holder.tvPrice.setText(context.getString(R.string.mad_currency, priceText));
        } else {
            holder.tvPrice.setText(priceText);
        }

        ImageLoader.getInstance().load(hotel.getImageUrl(), holder.ivPhoto, R.drawable.ic_launcher_background);

        boolean isFav = favoriteIds.contains(hotel.getId());
        holder.btnFavorite.setImageResource(isFav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        holder.btnFavorite.setColorFilter(ContextCompat.getColor(context, R.color.white));

        holder.btnFavorite.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(getItem(pos), pos);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HotelDetailActivity.class);
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