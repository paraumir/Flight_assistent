package com.example.kurs;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.bumptech.glide.Glide;
import com.example.kurs.databinding.ListItemBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ListAdapter extends ArrayAdapter<ListData> {

    private Boolean isAdmin;
    final String LOG_TAG = "myLogs";
    private static final String CHANNEL_ID = "flight_status_channel";
    private static final int NOTIFICATION_ID = 1;

    private String Admin = "qQlmqNGbpEgTrpH05YDtPCcRHSJ3";
    private Handler handler;
    private Runnable statusUpdater;
    public ArrayList<ListData> originalList;
    public ArrayList<ListData> filteredList;

    public ListAdapter(@NonNull Context context, ArrayList<ListData> dataArrayList) {
        super(context, R.layout.list_item, dataArrayList);
        this.originalList = new ArrayList<>(dataArrayList);
        this.filteredList = dataArrayList;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            isAdmin = Admin.equals(currentUser.getUid());
        }

        handler = new Handler();
        statusUpdater = new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
                handler.postDelayed(this, 5000); // 5 секунд
            }
        };
        handler.post(statusUpdater);
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
            binding.listName.setText("Рейс №" + listData.getName());
            binding.listTime.setText(listData.getTime());
            updateStatus(binding, listData);

            if (listData.getImageUrl() != null && !listData.getImageUrl().isEmpty()) {
                if (listData.getImageUrl().startsWith("gs://")) {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(listData.getImageUrl());
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        Glide.with(getContext())
                                .load(uri)
                                .into(binding.listImage);
                    }).addOnFailureListener(exception -> {
                        binding.listImage.setImageResource(R.drawable.plane);
                    });
                } else {
                    Glide.with(getContext())
                            .load(listData.getImageUrl())
                            .into(binding.listImage);
                }
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
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("flights").child(getItem(position).getFlight());
                    databaseReference.removeValue();
                    String imageUrl = getItem(position).getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                        photoRef.delete();
                    }
                    remove(getItem(position));
                    notifyDataSetChanged();
                }
            });
        }

        return view;
    }

    private void updateStatus(ListItemBinding binding, ListData listData) {
        String currentTime = getCurrentTime();
        String flightTime = listData.getTime();

        // Extract hours and minutes from both times
        int currentHours = Integer.parseInt(currentTime.substring(0, 2));
        int currentMinutes = Integer.parseInt(currentTime.substring(3, 5));
        int flightHours = Integer.parseInt(flightTime.substring(0, 2));
        int flightMinutes = Integer.parseInt(flightTime.substring(3, 5));

        int currentTotalMinutes = currentHours * 60 + currentMinutes;
        int flightTotalMinutes = flightHours * 60 + flightMinutes;

        String newStatus;
        int diffMinutes = flightTotalMinutes - currentTotalMinutes;

        if (diffMinutes > 15) {
            newStatus = "Ожидание";
            binding.listStatus.setTextColor(Color.rgb(41, 102, 13));
        } else if (diffMinutes > 0) {
            newStatus = "Подготовка к вылету";
            binding.listStatus.setTextColor(Color.rgb(191, 179, 6));
        } else {
            newStatus = "Вылетел";
            binding.listStatus.setTextColor(Color.rgb(178, 34, 34));
        }

        if (!newStatus.equals(listData.getstatus())) {
            listData.setstatus(newStatus);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("flights");
            String itemId = listData.getFlight();
            databaseReference.child(itemId).child("status").setValue(newStatus).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(LOG_TAG, "Статус успешно обновлён");
                    sendNotification(listData.getName(), newStatus);
                }
            });
        }

        binding.listStatus.setText(newStatus);
    }
    private void sendNotification(String flightName, String newStatus) {
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Flight Status", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setContentTitle("Статус рейса: " + flightName)
                .setContentText("Новый статус: " + newStatus)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private String getCurrentTime() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return formatter.format(date);
    }

    public void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            for (ListData item : originalList) {
                if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Nullable
    @Override
    public ListData getItem(int position) {
        return filteredList.get(position);
    }
}
