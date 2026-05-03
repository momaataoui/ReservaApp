package com.ensab.reservaapp.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.data.ImageLoader;
import com.ensab.reservaapp.databinding.ItemAdminHotelBinding;
import com.ensab.reservaapp.model.Hotel;
import java.util.List;

public class AdminHotelAdapter extends RecyclerView.Adapter<AdminHotelAdapter.ViewHolder> {

    private final List<Hotel> hotels;
    private final OnHotelActionListener listener;

    public interface OnHotelActionListener {
        void onEdit(Hotel hotel);
        void onDelete(Hotel hotel);
    }

    public AdminHotelAdapter(List<Hotel> hotels, OnHotelActionListener listener) {
        this.hotels = hotels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminHotelBinding binding = ItemAdminHotelBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hotel hotel = hotels.get(position);
        holder.binding.tvHotelName.setText(hotel.getName());
        holder.binding.tvHotelCity.setText(hotel.getCity());
        holder.binding.tvHotelPrice.setText(String.format("%.0f DH / Night", hotel.getPrice_per_night()));

        ImageLoader.getInstance().load(hotel.getImageUrl(), holder.binding.ivHotelImage, R.drawable.hotel_image_bg);

        holder.binding.btnEdit.setOnClickListener(v -> listener.onEdit(hotel));
        holder.binding.btnDelete.setOnClickListener(v -> listener.onDelete(hotel));
    }

    @Override
    public int getItemCount() {
        return hotels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminHotelBinding binding;

        public ViewHolder(ItemAdminHotelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
