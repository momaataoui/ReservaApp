package com.ensab.reservaapp.view.adapter;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookings;
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd MMM", Locale.FRENCH);

    public BookingAdapter(List<Booking> bookings) {
        this.bookings = bookings;
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
        holder.tvTotalPrice.setText(fmtPrice(booking.getTotalPrice()) + " MAD");
        
        String dateRange = DATE_FMT.format(new Date(booking.getCheckIn())) + " - " + DATE_FMT.format(new Date(booking.getCheckOut()));
        holder.tvBookingDates.setText(dateRange);

        // Status styling
        String status = booking.getStatus() != null ? booking.getStatus().toUpperCase() : "PENDING";
        holder.tvStatus.setText(status);
        
        if (status.equals("CONFIRMED")) {
            holder.tvStatus.setTextColor(0xFF4CAF50); // Green
        } else if (status.equals("CANCELLED")) {
            holder.tvStatus.setTextColor(0xFFF44336); // Red
        } else {
            holder.tvStatus.setTextColor(0xFFFFC107); // Yellow/Amber
        }

        if (booking.getHotelImageUrl() != null && !booking.getHotelImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(booking.getHotelImageUrl())
                .centerCrop()
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

    private String fmtPrice(int p) {
        if (p < 1000) return String.valueOf(p);
        return (p / 1000) + " " + String.format(Locale.getDefault(), "%03d", p % 1000);
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHotelImage;
        TextView tvHotelName, tvHotelLocation, tvBookingDates, tvTotalPrice, tvStatus;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvHotelLocation = itemView.findViewById(R.id.tvHotelLocation);
            tvBookingDates = itemView.findViewById(R.id.tvBookingDates);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
