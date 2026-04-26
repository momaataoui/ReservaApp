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

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public void updateMessages(List<ChatMessage> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? 1 : 0;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == 1) ? R.layout.item_chat_user : R.layout.item_chat_gemini;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.tvMessage.setText(message.getText());

        if (holder.btnViewHotel != null) {
            if (message.hasHotelAction()) {
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
        MaterialButton btnViewHotel;

        ChatViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            btnViewHotel = itemView.findViewById(R.id.btnViewHotel);
        }
    }
}