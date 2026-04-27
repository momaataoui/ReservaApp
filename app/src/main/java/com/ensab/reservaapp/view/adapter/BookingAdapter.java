package com.ensab.reservaapp.view.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.model.Booking;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookings;
    private OnBookingActionListener listener;
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);

    public interface OnBookingActionListener {
        void onCancel(Booking booking);
        void onDelete(Booking booking);
    }

    public BookingAdapter(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public void setOnBookingActionListener(OnBookingActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        
        holder.tvHotelName.setText(booking.getHotelName());
        holder.tvHotelLocation.setText(booking.getHotelLocation());
        holder.tvTotalPrice.setText(String.format(Locale.getDefault(), "%d.00 DH", booking.getTotalPrice()));
        
        holder.tvCheckInDate.setText(DATE_FMT.format(new Date(booking.getCheckIn())));
        holder.tvCheckOutDate.setText(DATE_FMT.format(new Date(booking.getCheckOut())));

        // Calculate nights
        long diff = booking.getCheckOut() - booking.getCheckIn();
        long nights = TimeUnit.MILLISECONDS.toDays(diff);
        holder.tvNightsCount.setText(nights + " nights");
        
        holder.tvGuestsCount.setText(booking.getAdults() + " adults");
        // Assuming 1 room for now as it's not in the model explicitly, or using a default
        holder.tvRoomType.setText("1 room");

        // Status styling and Button Logic
        String status = booking.getStatus() != null ? booking.getStatus().toLowerCase() : "pending";
        
        if (status.equals("confirmed")) {
            holder.tvStatus.setText("Confirmed");
            holder.ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
            holder.ivStatusIcon.setImageTintList(ColorStateList.valueOf(0xFF4CAF50));
            holder.layoutStatusBadge.setBackgroundTintList(ColorStateList.valueOf(0xFFE8F5E9));
            
            holder.btnCancel.setEnabled(true);
            holder.btnCancel.setAlpha(1.0f);
            holder.btnDelete.setEnabled(false);
            holder.btnDelete.setAlpha(0.5f);
        } else if (status.equals("cancelled")) {
            holder.tvStatus.setText("Cancelled");
            holder.ivStatusIcon.setImageResource(R.drawable.ic_close);
            holder.ivStatusIcon.setImageTintList(ColorStateList.valueOf(0xFFF44336));
            holder.layoutStatusBadge.setBackgroundTintList(ColorStateList.valueOf(0xFFFFEBEE));
            
            holder.btnCancel.setEnabled(false);
            holder.btnCancel.setAlpha(0.5f);
            holder.btnDelete.setEnabled(true);
            holder.btnDelete.setAlpha(1.0f);
        } else {
            holder.tvStatus.setText("Pending");
            holder.ivStatusIcon.setImageResource(R.drawable.ic_lock); // or a pending icon
            holder.ivStatusIcon.setImageTintList(ColorStateList.valueOf(0xFFFFC107));
            holder.layoutStatusBadge.setBackgroundTintList(ColorStateList.valueOf(0xFFFFF8E1));

            holder.btnCancel.setEnabled(true);
            holder.btnCancel.setAlpha(1.0f);
            holder.btnDelete.setEnabled(false);
            holder.btnDelete.setAlpha(0.5f);
        }

        holder.btnCancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancel(booking);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(booking);
        });

        if (booking.getHotelImageUrl() != null && !booking.getHotelImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(booking.getHotelImageUrl())
                .centerCrop()
                .placeholder(R.drawable.mamounia)
                .into(holder.ivHotelImage);
        } else {
            holder.ivHotelImage.setImageResource(R.drawable.mamounia);
        }
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public void updateBookings(List<Booking> newBookings) {
        this.bookings = newBookings;
        notifyDataSetChanged();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHotelImage, ivStatusIcon;
        TextView tvHotelName, tvHotelLocation, tvCheckInDate, tvCheckOutDate, tvTotalPrice, tvStatus;
        TextView tvNightsCount, tvGuestsCount, tvRoomType;
        View layoutStatusBadge;
        MaterialButton btnCancel, btnDelete;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvHotelLocation = itemView.findViewById(R.id.tvHotelLocation);
            tvCheckInDate = itemView.findViewById(R.id.tvCheckInDate);
            tvCheckOutDate = itemView.findViewById(R.id.tvCheckOutDate);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvNightsCount = itemView.findViewById(R.id.tvNightsCount);
            tvGuestsCount = itemView.findViewById(R.id.tvGuestsCount);
            tvRoomType = itemView.findViewById(R.id.tvRoomType);
            layoutStatusBadge = itemView.findViewById(R.id.layoutStatusBadge);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
