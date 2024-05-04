package com.example.kurs;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.example.kurs.databinding.ListItemBinding;

public class ListAdapter extends ArrayAdapter<ListData> {
    private Boolean isAdmin;
    final String LOG_TAG = "myLogs";

    String Admin ="";

    public ListAdapter(@NonNull Context context, ArrayList<ListData> dataArrayList) {
        super(context, R.layout.list_item, dataArrayList);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if(Admin.equals(currentUser.getUid())){
                isAdmin = true;
            }else{
                isAdmin = false;
            }
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        ListData listData = getItem(position);
        ListItemBinding binding;

        if (view == null) {
            binding = ListItemBinding.inflate(LayoutInflater.from(getContext()), parent, false);
            view = binding.getRoot();
            view.setTag(binding);
        } else {
            binding = (ListItemBinding) view.getTag();
        }

        if (listData != null) {
            binding.listName.setText(listData.getName());
            binding.listTime.setText(listData.getTime());
            binding.listStatus.setText(listData.getstatus());

            if (listData.getImageUrl() != null && !listData.getImageUrl().isEmpty()) {
                Glide.with(getContext())
                        .load(listData.getImageUrl())
                        .into(binding.listImage);
            } else {
                binding.listImage.setImageResource(R.drawable.plane);
            }

            if (isAdmin) {
                binding.changeButton.setVisibility(View.VISIBLE);
                binding.deleteButton.setVisibility(View.VISIBLE);
            } else {
                binding.changeButton.setVisibility(View.GONE);
                binding.deleteButton.setVisibility(View.GONE);
            }
        }

        binding.changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemId = getItem(position).getFlight();
                Intent intent = new Intent(getContext(), Change.class);
                intent.putExtra("itemId", itemId);
                getContext().startActivity(intent);
            }
        });

        binding.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("flights");

                String itemId = getItem(position).getFlight();
                databaseReference.child(itemId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        remove(listData);
                        notifyDataSetChanged();
                    }
                });
            }
        });

        return view;
    }
}
