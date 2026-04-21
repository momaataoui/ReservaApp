package com.ensab.reservaapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvDisplayFullName, tvInfoName, tvInfoEmail, tvInfoPhone;
    private ShapeableImageView ivProfileLarge;
    private Button btnEdit, btnLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "user_session";

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private Uri cameraImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Window window = getWindow();
        window.setStatusBarColor(Color.WHITE);
        WindowInsetsControllerCompat windowInsetsController = 
                ViewCompat.getWindowInsetsController(window.getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }

        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        ivProfileLarge = findViewById(R.id.ivProfileLarge);
        tvDisplayFullName = findViewById(R.id.tvDisplayFullName);
        tvInfoName = findViewById(R.id.tvInfoName);
        tvInfoEmail = findViewById(R.id.tvInfoEmail);
        tvInfoPhone = findViewById(R.id.tvInfoPhone);
        btnEdit = findViewById(R.id.btnEdit);
        btnLogout = findViewById(R.id.btnLogout);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            goToLogin();
            return;
        }

        loadUserData();
        initPhotoPickers();
        NavigationHelper.setSelectedItem(this, R.id.navProfile);

        findViewById(R.id.btnChangePhoto).setOnClickListener(v -> showPhotoOptionsDialog());
        btnEdit.setOnClickListener(v -> showEditDialog());
        btnLogout.setOnClickListener(v -> logoutUser());
        
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_scale_out);
        });

        setupNavigation();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initPhotoPickers() {
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadProfileImage(imageUri);
                    }
                }
            }
        );

        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && cameraImageUri != null) {
                    uploadProfileImage(cameraImageUri);
                }
            }
        );

        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void showPhotoOptionsDialog() {
        String[] options = {"Prendre une photo", "Choisir de la galerie"};
        new AlertDialog.Builder(this)
            .setTitle("Changer la photo de profil")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    checkCameraPermission();
                } else {
                    openGallery();
                }
            })
            .show();
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
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        
        String userId = user.getUid();
        StorageReference profileRef = storage.getReference().child("profile_images/" + userId + ".jpg");

        profileRef.putFile(imageUri)
            .addOnSuccessListener(taskSnapshot -> profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                db.collection("users").document(userId)
                    .update("profileImage", downloadUrl)
                    .addOnSuccessListener(aVoid -> {
                        Glide.with(this).load(downloadUrl).into(ivProfileLarge);
                        Toast.makeText(this, "Photo de profil mise à jour", Toast.LENGTH_SHORT).show();
                    });
            }))
            .addOnFailureListener(e -> Toast.makeText(this, "Échec de l'upload: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupNavigation() {
        findViewById(R.id.navDiscover).setOnClickListener(v -> {
            Intent intent = new Intent(this, ChoiceActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_scale_out);
            finish();
        });
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        
        String userId = user.getUid();
        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String fullName = documentSnapshot.getString("fullName");
                    String email = documentSnapshot.getString("email");
                    String phone = documentSnapshot.getString("phone");
                    String imageUrl = documentSnapshot.getString("profileImage");

                    tvDisplayFullName.setText(fullName);
                    tvInfoName.setText(fullName);
                    tvInfoEmail.setText(email);
                    tvInfoPhone.setText(phone != null && !phone.isEmpty() ? phone : "Non renseigné");
                    
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this).load(imageUrl).into(ivProfileLarge);
                    }
                }
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show());
    }

    private void showEditDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        
        String userId = user.getUid();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);
        
        EditText etEditName = dialogView.findViewById(R.id.etEditName);
        EditText etEditPhone = dialogView.findViewById(R.id.etEditPhone);
        
        etEditName.setText(tvInfoName.getText());
        etEditPhone.setText(tvInfoPhone.getText().equals("Non renseigné") ? "" : tvInfoPhone.getText());

        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            String newName = etEditName.getText().toString().trim();
            String newPhone = etEditPhone.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Le nom ne peut pas être vide", Toast.LENGTH_SHORT).show();
            } else {
                db.collection("users").document(userId)
                    .update("fullName", newName, "phone", newPhone)
                    .addOnSuccessListener(aVoid -> {
                        loadUserData();
                        Toast.makeText(this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Échec de la mise à jour", Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.create().show();
    }

    private void logoutUser() {
        mAuth.signOut();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        goToLogin();
    }
}
