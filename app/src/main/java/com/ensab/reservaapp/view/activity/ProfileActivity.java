package com.ensab.reservaapp.view.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.ensab.reservaapp.R;
import com.ensab.reservaapp.data.ImageLoader;
import com.ensab.reservaapp.databinding.ActivityProfileBinding;
import com.ensab.reservaapp.util.NavigationHelper;
import com.ensab.reservaapp.viewmodel.ProfileViewModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ProfileActivity gère les informations personnelles de l'utilisateur.
 * Elle permet de modifier son nom, son numéro de téléphone, sa photo de profil
 * et donne accès au panneau d'administration si l'utilisateur possède le rôle 'admin'.
 */
public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "user_session";

    private ProfileViewModel viewModel; // Utilisation d'un ViewModel pour survivre aux rotations d'écran

    // Launchers pour les actions multimédia (Photos)
    private ActivityResultLauncher<PickVisualMediaRequest> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private Uri cameraImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuration de la barre de statut transparente
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(false);
        }

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Correction du padding pour la barre de navigation Android
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.bottomNavContainer.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        // Initialisation du ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            goToLogin(); // Redirection si non connecté
            return;
        }

        // Restauration ou chargement des données
        if (viewModel.isDataLoaded()) {
            restoreDataFromViewModel();
        } else {
            loadUserData();
        }

        setupTextWatchers();
        initPhotoPickers();

        // Branchement des clics
        binding.btnChangePic.setOnClickListener(v -> showPhotoOptionsDialog());
        binding.btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
        binding.btnLogoutDialog.setOnClickListener(v -> logoutUser());
        binding.btnAdminPanel.setOnClickListener(v -> 
            NavigationHelper.fastNavigate(this, AdminDashboardActivity.class));

        NavigationHelper.setSelectedItem(this, R.id.navProfile);
        setupNavigation();
    }

    /**
     * Observe les changements de saisie pour mettre à jour le ViewModel en temps réel.
     */
    private void setupTextWatchers() {
        binding.etEditName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.name.setValue(s.toString());
            }
        });

        binding.etEditPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.phone.setValue(s.toString());
            }
        });
    }

    /**
     * Remplit les champs UI à partir des données stockées dans le ViewModel.
     */
    private void restoreDataFromViewModel() {
        binding.etEditName.setText(viewModel.name.getValue());
        binding.etEditEmail.setText(viewModel.email.getValue());
        binding.etEditPhone.setText(viewModel.phone.getValue());

        String imageUrl = viewModel.profileImageUrl.getValue();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ImageLoader.getInstance().load(imageUrl, binding.ivEditProfilePic, R.drawable.profile);
        }
    }

    private void setupNavigation() {
        binding.bottomNavContainer.findViewById(R.id.navDiscover).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ChoiceActivity.class));
        binding.bottomNavContainer.findViewById(R.id.navSaved).setOnClickListener(v -> NavigationHelper.fastNavigate(this, WishlistActivity.class));
        binding.bottomNavContainer.findViewById(R.id.navBookings).setOnClickListener(v -> NavigationHelper.fastNavigate(this, MyBookingsActivity.class));
    }

    /**
     * Sauvegarde les modifications (Nom, Téléphone) dans Firestore.
     */
    private void saveProfileChanges() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        String newName = binding.etEditName.getText().toString().trim();
        String newPhone = binding.etEditPhone.getText().toString().trim();
        
        if (newName.isEmpty()) {
            Toast.makeText(this, "Le nom ne peut pas être vide", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
            .update("fullName", newName, "phone", newPhone)
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profil mis à jour", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(this, "Échec de la mise à jour", Toast.LENGTH_SHORT).show());
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Configure les sélecteurs de photo (Appareil photo / Galerie).
     */
    private void initPhotoPickers() {
        // Galerie (Android Photo Picker)
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> { if (uri != null) uploadProfileImage(uri); }
        );

        // Appareil photo
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> { if (success && cameraImageUri != null) uploadProfileImage(cameraImageUri); }
        );

        // Permissions
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> { if (isGranted) openCamera(); else Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show(); }
        );
    }

    // --- Méthodes utilitaires pour la photo ---
    private void showPhotoOptionsDialog() {
        String[] options = {"Prendre une photo", "Choisir dans la galerie"};
        new AlertDialog.Builder(this)
            .setTitle("Changer la photo de profil")
            .setItems(options, (dialog, which) -> {
                if (which == 0) checkCameraPermission(); else openGallery();
            }).show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            Toast.makeText(this, "Erreur lors de la création du fichier", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }

    private void openGallery() {
        galleryLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
    }

    private void uploadProfileImage(Uri imageUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        db.collection("users").document(user.getUid())
            .update("profileImage", imageUri.toString())
            .addOnSuccessListener(aVoid -> {
                binding.ivEditProfilePic.setImageURI(imageUri);
                Toast.makeText(this, "Photo mise à jour", Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Charge les données de l'utilisateur et gère la visibilité du panneau Admin.
     */
    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        
        db.collection("users").document(user.getUid()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String fullName = documentSnapshot.getString("fullName");
                    String email = documentSnapshot.getString("email");
                    String phone = documentSnapshot.getString("phone");
                    String imageUrl = documentSnapshot.getString("profileImage");
                    String role = documentSnapshot.getString("role");

                    // Mise à jour du ViewModel
                    viewModel.name.setValue(fullName);
                    viewModel.email.setValue(email);
                    viewModel.phone.setValue(phone != null ? phone : "");
                    viewModel.profileImageUrl.setValue(imageUrl != null ? imageUrl : "");
                    viewModel.setDataLoaded(true);

                    // Affichage conditionnel du bouton Admin
                    binding.btnAdminPanel.setVisibility("admin".equals(role) ? android.view.View.VISIBLE : android.view.View.GONE);

                    restoreDataFromViewModel();
                }
            });
    }

    /**
     * Déconnecte l'utilisateur et efface la session locale.
     */
    private void logoutUser() {
        mAuth.signOut();
        sharedPreferences.edit().clear().apply();
        goToLogin();
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
