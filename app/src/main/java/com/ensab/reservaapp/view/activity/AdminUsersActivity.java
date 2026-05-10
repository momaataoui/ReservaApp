package com.ensab.reservaapp.view.activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.ensab.reservaapp.databinding.ActivityAdminUsersBinding;
import com.ensab.reservaapp.model.User;
import com.ensab.reservaapp.repository.AdminRepository;
import com.ensab.reservaapp.view.adapter.AdminUserAdapter;
import java.util.ArrayList;
import java.util.List;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

/**
 * AdminUsersActivity permet à l'administrateur de visualiser tous les utilisateurs inscrits.
 * Elle permet également de changer le rôle d'un utilisateur (ex: promouvoir un client en admin).
 */
public class AdminUsersActivity extends AppCompatActivity {

    private ActivityAdminUsersBinding binding;
    private AdminRepository adminRepository;
    private AdminUserAdapter adapter;
    private List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Correction pour la barre de statut
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        adminRepository = new AdminRepository();

        setupRecyclerView();
        setupToolbar();
        loadUsers(); // Récupération de la liste des utilisateurs
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Configure la liste avec l'action de changement de rôle.
     */
    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(userList, (user, newRole) -> {
            updateUserRole(user, newRole);
        });
        binding.rvAdminUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminUsers.setAdapter(adapter);
    }

    /**
     * Charge tous les utilisateurs depuis la collection 'users' de Firestore.
     */
    private void loadUsers() {
        adminRepository.getAllUsers(new AdminRepository.AdminCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                userList.clear();
                userList.addAll(users);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminUsersActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Modifie le rôle de l'utilisateur sélectionné.
     */
    private void updateUserRole(User user, String newRole) {
        adminRepository.updateUserRole(user.getId(), newRole, () -> {
            Toast.makeText(this, "Role updated: " + newRole, Toast.LENGTH_SHORT).show();
            loadUsers(); // Rafraîchissement après modification
        }, e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
