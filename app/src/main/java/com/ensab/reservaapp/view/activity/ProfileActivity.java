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
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.data.NavigationHelper;
import com.ensab.reservaapp.viewmodel.ProfileViewModel;
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

    private EditText etEditName, etEditPhone, etEditEmail;
    private ShapeableImageView ivEditProfilePic;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "user_session";

    private ProfileViewModel viewModel;

    private ActivityResultLauncher<PickVisualMediaRequest> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private Uri cameraImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat windowInsetsController = 
                WindowCompat.getInsetsController(window, window.getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(false);

        setContentView(R.layout.dialog_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        // Initialisation du ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        etEditName = findViewById(R.id.etEditName);
        etEditPhone = findViewById(R.id.etEditPhone);
        etEditEmail = findViewById(R.id.etEditEmail);
        ivEditProfilePic = findViewById(R.id.ivEditProfilePic);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            goToLogin();
            return;
        }

        // Si les données sont déjà dans le ViewModel (après rotation), on les restaure
        if (viewModel.isDataLoaded()) {
            restoreDataFromViewModel();
        } else {
            loadUserData();
        }

        setupTextWatchers();
        initPhotoPickers();

        findViewById(R.id.btnChangePic).setOnClickListener(v -> showPhotoOptionsDialog());
        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> saveProfileChanges());
        findViewById(R.id.btnLogoutDialog).setOnClickListener(v -> logoutUser());

        NavigationHelper.setSelectedItem(this, R.id.navProfile);
        setupNavigation();
    }

    private void setupTextWatchers() {
        etEditName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.name.setValue(s.toString());
            }
        });

        etEditPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.phone.setValue(s.toString());
            }
        });
    }

    private void restoreDataFromViewModel() {
        etEditName.setText(viewModel.name.getValue());
        etEditEmail.setText(viewModel.email.getValue());
        etEditPhone.setText(viewModel.phone.getValue());
        
        String imageUrl = viewModel.profileImageUrl.getValue();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(ivEditProfilePic);
        }
    }

    private void setupNavigation() {
        findViewById(R.id.navDiscover).setOnClickListener(v -> NavigationHelper.fastNavigate(this, ChoiceActivity.class));
        findViewById(R.id.navSaved).setOnClickListener(v -> NavigationHelper.fastNavigate(this, WishlistActivity.class));
        findViewById(R.id.navBookings).setOnClickListener(v -> {
            Toast.makeText(this, "Trips coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveProfileChanges() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        
        String userId = user.getUid();
        String newName = etEditName.getText().toString().trim();
        String newPhone = etEditPhone.getText().toString().trim();
        
        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
            .update("fullName", newName, "phone", newPhone)
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initPhotoPickers() {
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
                if (uri != null) {
                    uploadProfileImage(uri);
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
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void showPhotoOptionsDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
            .setTitle("Change Profile Picture")
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
            Toast.makeText(this, "File creation error", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void openGallery() {
        galleryLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
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
                        viewModel.profileImageUrl.setValue(downloadUrl);
                        Glide.with(this).load(downloadUrl).into(ivEditProfilePic);
                        Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                    });
            }))
            .addOnFailureListener(e -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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

                    viewModel.name.setValue(fullName);
                    viewModel.email.setValue(email);
                    viewModel.phone.setValue(phone != null ? phone : "");
                    viewModel.profileImageUrl.setValue(imageUrl != null ? imageUrl : "");
                    viewModel.setDataLoaded(true);

                    restoreDataFromViewModel();
                }
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show());
    }

    private void logoutUser() {
        mAuth.signOut();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        goToLogin();
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
