package com.example.kurs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.kurs.databinding.ActivityDetailedBinding;
import com.bumptech.glide.Glide;

public class DetailedActivity extends AppCompatActivity {

    ActivityDetailedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if (intent != null){
            String name = intent.getStringExtra("name");
            String time = intent.getStringExtra("time");
            String road = intent.getStringExtra("road");
            String inf = intent.getStringExtra("inf");
            String imageUrl = intent.getStringExtra("image");
            String status = intent.getStringExtra("status");

            // Set data to views
            binding.detailName.setText(name);
            binding.detailTime.setText(time);
            binding.detailIngredients.setText(road);
            binding.detailDesc.setText(inf);
            binding.detailStatus.setText(status);

            // Load image from URL using Glide
            Glide.with(this)
                    .load(imageUrl)
                    .into(binding.detailImage);
        }
    }
}

