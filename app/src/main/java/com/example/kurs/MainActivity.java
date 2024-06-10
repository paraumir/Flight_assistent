package com.example.kurs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.kurs.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ListAdapter listAdapter;
    private DatabaseReference databaseReference;
    private final String LOG_TAG = "myLogs";
    private String admin = "qQlmqNGbpEgTrpH05YDtPCcRHSJ3";
    private Boolean isAdmin;
    private ArrayList<ListData> dataArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseReference = FirebaseDatabase.getInstance().getReference().child("flights");

        binding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ADD.class));
            }
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            isAdmin = admin.equals(currentUser.getUid());
        }

        if (isAdmin) {
            binding.addButton.setVisibility(View.VISIBLE);
        } else {
            binding.addButton.setVisibility(View.GONE);
        }

        binding.infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new InfoFragment());
            }
        });

        binding.creatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new CreatorFragment());
            }
        });

        binding.instructionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new instructionFragment());
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataArrayList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ListData listData = snapshot.getValue(ListData.class);
                    if (listData != null) {
                        listData.setFlight(snapshot.getKey());
                        dataArrayList.add(listData);
                    }
                }
                listAdapter = new ListAdapter(MainActivity.this, dataArrayList);
                binding.listview.setAdapter(listAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });

        binding.listview.setClickable(true);

        binding.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ListData selectedFlight = (ListData) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(MainActivity.this, DetailedActivity.class);
                intent.putExtra("name", selectedFlight.getName());
                intent.putExtra("time", selectedFlight.getTime());
                intent.putExtra("road", selectedFlight.getRoad());
                intent.putExtra("inf", selectedFlight.getInfo());
                intent.putExtra("image", selectedFlight.getImageUrl());
                intent.putExtra("status", selectedFlight.getstatus());
                startActivity(intent);
            }
        });

        // Implement search functionality
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (listAdapter != null) {
                    listAdapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(binding.fragmentContainer.getId(), fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
