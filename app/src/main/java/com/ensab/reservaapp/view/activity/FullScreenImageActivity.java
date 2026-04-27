package com.ensab.reservaapp.view.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.ensab.reservaapp.R;
import com.ensab.reservaapp.data.ImageLoader;
import com.ensab.reservaapp.databinding.ActivityFullScreenImageBinding;

public class FullScreenImageActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE_URL = "extra_image_url";
    private ActivityFullScreenImageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullScreenImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        if (imageUrl != null) {
            ImageLoader.getInstance().load(imageUrl, binding.ivFullScreen, R.drawable.mamounia);
        }

        binding.btnClose.setOnClickListener(v -> finish());
    }
}