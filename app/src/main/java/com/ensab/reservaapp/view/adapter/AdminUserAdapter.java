package com.ensab.reservaapp.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.data.ImageLoader;
import com.ensab.reservaapp.databinding.ItemAdminUserBinding;
import com.ensab.reservaapp.model.User;
import java.util.List;

/**
 * AdminUserAdapter gère l'affichage d'un utilisateur dans la liste d'administration.
 * Il permet de visualiser le profil et de modifier le rôle (Admin/Client) via un Switch.
 */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private final List<User> users;
    private final OnUserActionListener listener;

    /**
     * Interface pour notifier le changement de rôle à l'activité.
     */
    public interface OnUserActionListener {
        void onRoleChanged(User user, String newRole);
    }

    public AdminUserAdapter(List<User> users, OnUserActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Utilisation du ViewBinding pour l'item_admin_user
        ItemAdminUserBinding binding = ItemAdminUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        
        // Affichage des informations de base
        holder.binding.tvFullName.setText(user.getFullName());
        holder.binding.tvEmail.setText(user.getEmail());
        holder.binding.tvRole.setText(user.getRole() != null ? user.getRole().toUpperCase() : "CLIENT");

        // Chargement de l'image de profil via notre ImageLoader personnalisé
        ImageLoader.getInstance().load(user.getProfileImage(), holder.binding.ivUserImage, R.drawable.ic_profile);

        // IMPORTANT : Désactiver temporairement le listener avant de changer l'état du Switch
        // Cela évite de déclencher un appel Firestore involontaire lors du défilement (Recycling)
        holder.binding.switchRole.setOnCheckedChangeListener(null);
        holder.binding.switchRole.setChecked("admin".equalsIgnoreCase(user.getRole()));
        
        // Réactivation du listener pour capturer l'action de l'admin
        holder.binding.switchRole.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newRole = isChecked ? "admin" : "client";
            listener.onRoleChanged(user, newRole);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminUserBinding binding;

        public ViewHolder(ItemAdminUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
