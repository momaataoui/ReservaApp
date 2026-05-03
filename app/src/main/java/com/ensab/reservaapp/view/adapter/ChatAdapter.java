package com.ensab.reservaapp.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.model.ChatMessage;
import com.ensab.reservaapp.view.activity.HotelDetailActivity;
import java.util.List;

/**
 * ChatAdapter gère l'affichage des bulles de discussion dans l'assistant IA.
 * Il alterne entre deux layouts (utilisateur à droite, Gemini à gauche)
 * et permet d'afficher des boutons d'action contextuels (ex: voir un hôtel).
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    /**
     * Met à jour la liste des messages et rafraîchit la vue.
     */
    public void updateMessages(List<ChatMessage> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    /**
     * Détermine si le message provient de l'utilisateur (1) ou de l'IA (0).
     */
    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? 1 : 0;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sélectionne le layout approprié selon l'expéditeur
        int layout = (viewType == 1) ? R.layout.item_chat_user : R.layout.item_chat_gemini;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.tvMessage.setText(message.getText());

        // Logique spécifique aux réponses de l'IA contenant une recommandation d'hôtel
        if (holder.btnViewHotel != null) {
            if (message.hasHotelAction()) {
                // Affiche le bouton "Voir l'hôtel" si un ID d'hôtel est présent dans le message
                holder.btnViewHotel.setVisibility(View.VISIBLE);
                holder.btnViewHotel.setOnClickListener(v -> {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, HotelDetailActivity.class);
                    intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_ID, message.getHotelId());
                    context.startActivity(intent);
                });
            } else {
                holder.btnViewHotel.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        MaterialButton btnViewHotel; // Bouton d'action optionnel

        ChatViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            btnViewHotel = itemView.findViewById(R.id.btnViewHotel);
        }
    }
}