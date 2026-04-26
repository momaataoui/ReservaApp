package com.ensab.reservaapp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.data.NavigationHelper;
import com.ensab.reservaapp.view.adapter.ChatAdapter;
import com.ensab.reservaapp.viewmodel.ChatViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class SmartSearchActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private ChatAdapter adapter;
    private EditText etMessage;
    private FloatingActionButton btnSend;
    private ProgressBar progressBar;
    private ChatViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_search);

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        progressBar = findViewById(R.id.progressBar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);
        
        adapter = new ChatAdapter(new ArrayList<>());
        rvChat.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        viewModel.messages.observe(this, chatMessages -> {
            adapter.updateMessages(chatMessages);
            if (!chatMessages.isEmpty()) {
                rvChat.smoothScrollToPosition(chatMessages.size() - 1);
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                viewModel.sendMessage(text);
                etMessage.setText("");
            }
        });

        setupSuggestions();
    }

    private void setupSuggestions() {
        View suggestionSearch = findViewById(R.id.suggestionSearch);
        View suggestionBookings = findViewById(R.id.suggestionBookings);
        View suggestionHelp = findViewById(R.id.suggestionHelp);

        if (suggestionSearch != null) {
            suggestionSearch.setOnClickListener(v -> {
                NavigationHelper.fastNavigate(this, HotelListActivity.class);
            });
        }

        if (suggestionBookings != null) {
            suggestionBookings.setOnClickListener(v -> {
                Toast.makeText(this, "Redirection vers mes réservations...", Toast.LENGTH_SHORT).show();
                // Vous pouvez ajouter l'Intent vers votre activité de réservations ici
            });
        }

        if (suggestionHelp != null) {
            suggestionHelp.setOnClickListener(v -> {
                viewModel.sendMessage("J'ai besoin d'aide pour utiliser l'application.");
            });
        }
    }
}
