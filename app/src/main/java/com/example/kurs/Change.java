package com.example.kurs;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kurs.databinding.ChangeLayoutBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Change extends AppCompatActivity {

    final String LOG_TAG = "myLogs";
    ChangeLayoutBinding binding;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChangeLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseReference = FirebaseDatabase.getInstance().getReference().child("flights");

        final String itemId = getIntent().getStringExtra("itemId");

        databaseReference.child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ListData data = dataSnapshot.getValue(ListData.class);
                    if (data != null) {
                        binding.nameEditText.setText(data.getName());
                        binding.timeEditText.setText(data.getTime());
                        binding.roadEditText.setText(data.getRoad());
                        binding.infoEditText.setText(data.getInfo());
                        binding.imageUrlEditText.setText(data.getImageUrl());
                        binding.statusEditText.setText(data.getstatus());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Change.this, "Ошибка при чтении данных", Toast.LENGTH_SHORT).show();
            }
        });

        binding.changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Получение текста из полей макета
                String name = binding.nameEditText.getText().toString().trim();
                String time = binding.timeEditText.getText().toString().trim();
                String road = binding.roadEditText.getText().toString().trim();
                String info = binding.infoEditText.getText().toString().trim();
                String imageUrl = binding.imageUrlEditText.getText().toString().trim();
                String status = binding.statusEditText.getText().toString().trim();

                ListData newData = new ListData(name, time, road, info, imageUrl, status);
                databaseReference.child(itemId).setValue(newData);
                Toast.makeText(Change.this, "Данные изменены", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}

