package com.example.kurs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kurs.databinding.ChangeLayoutBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;
import java.util.regex.Pattern;

public class Change extends AppCompatActivity {

    ChangeLayoutBinding binding;
    DatabaseReference databaseReference;
    long count;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private static final Pattern TIME_PATTERN = Pattern.compile("^\\d{2}:\\d{2} -- \\d{2}:\\d{2}$");

    String ITEMID = "";
    String currentImageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChangeLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseReference = FirebaseDatabase.getInstance().getReference().child("flights");
        final String itemId = getIntent().getStringExtra("itemId");
        ITEMID = itemId;
        databaseReference.child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ListData data = dataSnapshot.getValue(ListData.class);
                    if (data != null) {
                        binding.nameEditText.setText(data.getName());
                        binding.timeEditText.setText(data.getTime());
                        binding.roadEditText.setText(data.getRoad());
                        binding.infoEditText.setText(data.getInfo());
                        binding.statusEditText.setText(data.getstatus());
                        currentImageUrl = data.getImageUrl();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        binding.imageUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        binding.changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.nameEditText.getText().toString().trim();
                String time = binding.timeEditText.getText().toString().trim();
                String road = binding.roadEditText.getText().toString().trim();
                String info = binding.infoEditText.getText().toString().trim();
                String status = binding.statusEditText.getText().toString().trim();

                if (!name.isEmpty() && !time.isEmpty() && !road.isEmpty() && !info.isEmpty() && !status.isEmpty()) {
                    if (TIME_PATTERN.matcher(time).matches()) {
                        uploadImageToFirebase(name, time, road, info, status);
                    } else {
                        Toast.makeText(Change.this, "Введите время в формате XX:XX -- XX:XX", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Change.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
        }
    }

    private void uploadImageToFirebase(final String name, final String time, final String road, final String info, final String status) {
        if (imageUri != null) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("images/" + UUID.randomUUID().toString());

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    updateFlightInDatabase(name, time, road, info, imageUrl, status);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Change.this, "Ошибка при загрузке изображения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // No image selected, retain the current image URL
            updateFlightInDatabase(name, time, road, info, currentImageUrl, status);
        }
    }

    private void updateFlightInDatabase(String name, String time, String road, String info, String imageUrl, String status) {
        ListData newData = new ListData(name, time, road, info, imageUrl, status);
        databaseReference.child(ITEMID).setValue(newData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(Change.this, "Изменено", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(Change.this, "Ошибка при изменении данных", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
