package com.example.kurs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.kurs.databinding.ActivityDetailedBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DetailedActivity extends AppCompatActivity {

    ActivityDetailedBinding binding;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();

        Intent intent = getIntent();
        if (intent != null){
            String name = intent.getStringExtra("name");
            String time = intent.getStringExtra("time");
            String road = intent.getStringExtra("road");
            String inf = intent.getStringExtra("inf");
            String imageUrl = intent.getStringExtra("image");
            String status = intent.getStringExtra("status");

            binding.detailName.setText("Рейс №" + name);
            binding.detailTime.setText(time);
            binding.detailIngredients.setText(road);
            binding.detailDesc.setText(inf);
            binding.detailStatus.setText(status);

            if (imageUrl != null) {
                if (imageUrl.startsWith("gs://")) {
                    // Handle gs:// URL using Firebase Storage
                    StorageReference storageReference = storage.getReferenceFromUrl(imageUrl);
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        Glide.with(this)
                                .load(uri)
                                .into(binding.detailImage);
                    }).addOnFailureListener(e -> {
                    });
                } else {
                    Glide.with(this)
                            .load(imageUrl)
                            .into(binding.detailImage);
                }
            }
        }
    }
}
