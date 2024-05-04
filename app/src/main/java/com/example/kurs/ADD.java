package com.example.kurs;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kurs.databinding.ActivityAddBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.util.Log;

public class ADD extends AppCompatActivity {

    final String LOG_TAG = "myLogs";
    ActivityAddBinding binding;
    DatabaseReference databaseReference;
    long count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseReference = FirebaseDatabase.getInstance().getReference().child("flights");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                count = dataSnapshot.getChildrenCount();
                Log.d(LOG_TAG, "Количество потомков flights: " + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        binding.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.nameEditText.getText().toString().trim();
                String time = binding.timeEditText.getText().toString().trim();
                String road = binding.roadEditText.getText().toString().trim();
                String info = binding.infoEditText.getText().toString().trim();
                String imageUrl = binding.imageUrlEditText.getText().toString().trim();
                String status = binding.statusEditText.getText().toString().trim();

                ListData newData = new ListData(name, time, road, info, imageUrl,status);
                count += 1;
                String flight = "flight" + count;
                databaseReference.child(flight).setValue(newData);
                Toast.makeText(ADD.this, "Добавлено", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
