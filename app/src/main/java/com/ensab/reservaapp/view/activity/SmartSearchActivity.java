package com.ensab.reservaapp.view.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ensab.reservaapp.R;
import com.ensab.reservaapp.databinding.ActivitySmartSearchBinding;
import com.ensab.reservaapp.util.NavigationHelper;
import com.ensab.reservaapp.view.adapter.ChatAdapter;
import com.ensab.reservaapp.viewmodel.ChatViewModel;

import java.util.ArrayList;

public class SmartSearchActivity extends AppCompatActivity {

    private ActivitySmartSearchBinding binding;
    private ChatAdapter adapter;
    private ChatViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySmartSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Fix for system bars (status bar and navigation bar) overlap
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvChat.setLayoutManager(layoutManager);

        adapter = new ChatAdapter(new ArrayList<>());
        binding.rvChat.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        viewModel.messages.observe(this, chatMessages -> {
            adapter.updateMessages(chatMessages);
            if (!chatMessages.isEmpty()) {
                binding.rvChat.smoothScrollToPosition(chatMessages.size() - 1);
            }
        });

        viewModel.isLoading.observe(this, isLoading ->
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE)
        );

        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                viewModel.sendMessage(text);
                binding.etMessage.setText("");
            }
        });

        setupSuggestions();
    }

    private void setupSuggestions() {
        if (binding.suggestionSearch != null) {
            binding.suggestionSearch.setOnClickListener(v ->
                    NavigationHelper.fastNavigate(this, HotelListActivity.class)
            );
        }

        if (binding.suggestionBookings != null) {
            binding.suggestionBookings.setOnClickListener(v ->
                    NavigationHelper.fastNavigate(this, MyBookingsActivity.class)
            );
        }

        if (binding.suggestionHelp != null) {
            binding.suggestionHelp.setOnClickListener(v ->
                    viewModel.sendMessage(getString(R.string.help_message))
            );
        }
    }
}