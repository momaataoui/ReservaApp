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
import com.google.android.material.imageview.ShapeableImageView;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvDisplayFullName, tvInfoName, tvInfoEmail, tvInfoPhone;
    private ShapeableImageView ivProfileLarge;
    private Button btnEdit, btnLogout;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "user_session";
    private static final String KEY_USER_EMAIL = "user_email";

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

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString(KEY_USER_EMAIL, "");

        ivProfileLarge = findViewById(R.id.ivProfileLarge);
        tvDisplayFullName = findViewById(R.id.tvDisplayFullName);
        tvInfoName = findViewById(R.id.tvInfoName);
        tvInfoEmail = findViewById(R.id.tvInfoEmail);
        tvInfoPhone = findViewById(R.id.tvInfoPhone);
        btnEdit = findViewById(R.id.btnEdit);
        btnLogout = findViewById(R.id.btnLogout);

        loadUserData(userEmail);
        initPhotoPickers(userEmail);

        findViewById(R.id.btnChangePhoto).setOnClickListener(v -> showPhotoOptionsDialog());
        btnEdit.setOnClickListener(v -> showEditDialog(userEmail));
        btnLogout.setOnClickListener(v -> logoutUser());
        
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_scale_out);
        });

        setupBottomNav();
    }

    private void initPhotoPickers(String email) {
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            saveProfileImage(email, imageUri.toString());
                        } catch (Exception e) {
                            saveProfileImage(email, imageUri.toString());
                        }
                    }
                }
            }
        );

        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && cameraImageUri != null) {
                    saveProfileImage(email, cameraImageUri.toString());
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

    private void saveProfileImage(String email, String uriString) {
        if (dbHelper.updateProfileImage(email, uriString)) {
            ivProfileLarge.setImageURI(null); // Force refresh
            ivProfileLarge.setImageURI(Uri.parse(uriString));
            Toast.makeText(this, "Photo de profil mise à jour", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNav() {
        View navProfile = findViewById(R.id.navProfile);
        navProfile.setBackgroundResource(R.drawable.nav_item_bg_selected);
        TextView tvProfileNav = findViewById(R.id.tvProfileNav);
        ImageView ivProfileNav = findViewById(R.id.ivProfileNav);
        if (tvProfileNav != null) { tvProfileNav.setTextColor(Color.BLACK); tvProfileNav.setAlpha(1.0f); }
        if (ivProfileNav != null) { ivProfileNav.setAlpha(1.0f); }
        View navDiscover = findViewById(R.id.navDiscover);
        navDiscover.setBackground(null);
        ImageView ivDiscover = findViewById(R.id.ivDiscover);
        TextView tvDiscover = findViewById(R.id.tvDiscover);
        if (ivDiscover != null) ivDiscover.setAlpha(0.5f);
        if (tvDiscover != null) { tvDiscover.setAlpha(0.5f); tvDiscover.setTextColor(getResources().getColor(R.color.secondary_text)); }
        navDiscover.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChoiceActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_scale_out);
            finish();
        });
    }

    private void loadUserData(String email) {
        if (!email.isEmpty()) {
            String fullName = dbHelper.getUserFullName(email);
            String phone = dbHelper.getUserPhone(email);
            String imageUri = dbHelper.getUserProfileImage(email);

            tvDisplayFullName.setText(fullName);
            tvInfoName.setText(fullName);
            tvInfoEmail.setText(email);
            tvInfoPhone.setText(phone != null && !phone.isEmpty() ? phone : "Non renseigné");
            
            if (imageUri != null && !imageUri.isEmpty()) {
                ivProfileLarge.setImageURI(Uri.parse(imageUri));
            }
        }
    }

    private void showEditDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);
        EditText etEditName = dialogView.findViewById(R.id.etEditName);
        EditText etEditPhone = dialogView.findViewById(R.id.etEditPhone);
        etEditName.setText(dbHelper.getUserFullName(email));
        etEditPhone.setText(dbHelper.getUserPhone(email));
        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            String newName = etEditName.getText().toString().trim();
            String newPhone = etEditPhone.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Le nom ne peut pas être vide", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.updateFullName(email, newName);
                dbHelper.updatePhone(email, newPhone);
                loadUserData(email);
                Toast.makeText(this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.create().show();
    }

    private void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
