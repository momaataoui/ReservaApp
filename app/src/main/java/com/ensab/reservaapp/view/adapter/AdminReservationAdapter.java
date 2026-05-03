package com.ensab.reservaapp.view.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.databinding.ItemAdminReservationBinding;
import com.ensab.reservaapp.model.Booking;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * AdminReservationAdapter gère l'affichage d'une ligne de réservation dans le module Admin.
 * Il permet d'afficher les détails (hôtel, prix, dates) et d'appliquer des styles dynamiques
 * selon le statut (Confirmé, Annulé, En attente).
 */
public class AdminReservationAdapter extends RecyclerView.Adapter<AdminReservationAdapter.ViewHolder> {

    private final List<Booking> bookings;
    private final OnReservationActionListener listener; // Interface pour les clics sur Confirmer/Annuler
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

    /**
     * Interface pour déléguer le traitement métier à l'Activité.
     */
    public interface OnReservationActionListener {
        void onConfirm(Booking booking);
        void onCancel(Booking booking);
    }

    public AdminReservationAdapter(List<Booking> bookings, OnReservationActionListener listener) {
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Liaison avec le layout XML item_admin_reservation
        ItemAdminReservationBinding binding = ItemAdminReservationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        
        // Remplissage des données textuelles
        holder.binding.tvHotelName.setText(booking.getHotelName());
        holder.binding.tvUserEmail.setText(booking.getUserId()); 
        holder.binding.tvTotalPrice.setText(String.format("%d DH", booking.getTotalPrice()));
        
        // Formatage des dates de séjour
        String dateStr = dateFormat.format(new Date(booking.getCheckIn())) + " - " + dateFormat.format(new Date(booking.getCheckOut()));
        holder.binding.tvDates.setText(dateStr);

        // Gestion dynamique de l'UI selon le statut
        String status = booking.getStatus() != null ? booking.getStatus().toUpperCase() : "PENDING";
        holder.binding.tvStatus.setText(status);

        switch (status) {
            case "CONFIRMED":
                // Vert pour les réservations validées
                holder.binding.tvStatus.setBackgroundResource(R.drawable.status_confirmed_bg);
                holder.binding.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                holder.binding.btnConfirm.setVisibility(View.GONE);
                holder.binding.btnCancel.setVisibility(View.VISIBLE);
                break;
            case "CANCELLED":
                // Rouge pour les annulations
                holder.binding.tvStatus.setBackgroundResource(R.drawable.status_cancelled_bg);
                holder.binding.tvStatus.setTextColor(Color.parseColor("#C62828"));
                holder.binding.btnConfirm.setVisibility(View.VISIBLE);
                holder.binding.btnCancel.setVisibility(View.GONE);
                break;
            default: // PENDING
                // Orange pour les demandes en attente
                holder.binding.tvStatus.setBackgroundResource(R.drawable.status_pending_bg);
                holder.binding.tvStatus.setTextColor(Color.parseColor("#EF6C00"));
                holder.binding.btnConfirm.setVisibility(View.VISIBLE);
                holder.binding.btnCancel.setVisibility(View.VISIBLE);
                break;
        }

        // Branchement des listeners sur les boutons d'action
        holder.binding.btnConfirm.setOnClickListener(v -> listener.onConfirm(booking));
        holder.binding.btnCancel.setOnClickListener(v -> listener.onCancel(booking));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    /**
     * ViewHolder interne utilisant ViewBinding pour la performance.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminReservationBinding binding;

        public ViewHolder(ItemAdminReservationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
