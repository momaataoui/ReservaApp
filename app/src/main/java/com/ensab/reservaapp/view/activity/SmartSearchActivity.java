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

/**
 * SmartSearchActivity est l'interface de messagerie avec l'intelligence artificielle (Gemini).
 * Elle permet aux clients de trouver des hôtels en utilisant le langage naturel.
 */
public class SmartSearchActivity extends AppCompatActivity {

    private ActivitySmartSearchBinding binding;
    private ChatAdapter adapter;       // Adaptateur pour l'affichage des bulles de message
    private ChatViewModel viewModel;    // Gère la communication avec le SDK Generative AI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySmartSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ajustement pour éviter que le clavier ou les barres système ne cachent le chat
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configuration du RecyclerView pour le chat
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Fait défiler le chat vers le bas par défaut
        binding.rvChat.setLayoutManager(layoutManager);

        adapter = new ChatAdapter(new ArrayList<>());
        binding.rvChat.setAdapter(adapter);

        // Liaison avec le ViewModel
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // Observation des messages envoyés/reçus
        viewModel.messages.observe(this, chatMessages -> {
            adapter.updateMessages(chatMessages);
            if (!chatMessages.isEmpty()) {
                // Défilement automatique vers le dernier message
                binding.rvChat.smoothScrollToPosition(chatMessages.size() - 1);
            }
        });

        // Indicateur de chargement pendant que l'IA "réfléchit"
        viewModel.isLoading.observe(this, isLoading ->
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE)
        );

        // Envoi d'un message au clic sur l'icône
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                viewModel.sendMessage(text);
                binding.etMessage.setText(""); // Effacer le champ après envoi
            }
        });

        setupSuggestions(); // Configuration des boutons d'aide rapide
    }

    /**
     * Configure les actions rapides (suggestions) affichées au-dessus du champ de texte.
     */
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