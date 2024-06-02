package com.example.tripwise.Activity.view.itinerary;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.widget.TextClock;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tripwise.Activity.view.itinerary.item.Itinerary;
import com.example.tripwise.R;
import com.example.tripwise.databinding.ActivityEditItineraryTrackingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class EditItineraryTrackingActivity extends AppCompatActivity {

    private ActivityEditItineraryTrackingBinding binding;
    private DatabaseReference itineraryRef;
    private FirebaseUser currentUser;
    private Calendar startCalendar, endCalendar;
    private boolean startTimeSelected, endTimeSelected;
    private String date;
    private long dateMilis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditItineraryTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Itinerary Tracking");

        Intent intent = getIntent();
        long startTime = intent.getLongExtra("startTime", 0);
        long endTime = intent.getLongExtra("endTime", 0);
        date = intent.getStringExtra("date");
        dateMilis = intent.getLongExtra("dateMilis", 0);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            itineraryRef = FirebaseDatabase.getInstance().getReference("itinerary").child(userId);
        }

        binding.tcStart.setOnClickListener(v -> showTimePickerDialog(startTime, binding.tcStart));
        binding.tcEnd.setOnClickListener(v -> showTimePickerDialog(endTime, binding.tcEnd));
        binding.btnUpdate.setOnClickListener(v -> checkAndSaveItinerary());

        getItineraryData(startTime);
    }
    private void getItineraryData(long startTime) {
        Query query = itineraryRef.orderByChild("startTime").equalTo(startTime);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (dataSnapshot != null) {
                            String name = dataSnapshot.child("name").getValue(String.class);
                            String location = dataSnapshot.child("location").getValue(String.class);
                            long snapshotStartTime = dataSnapshot.child("startTime").getValue(Long.class);
                            long snapshotEndTime = dataSnapshot.child("endTime").getValue(Long.class);
                            binding.etName.setText(name);
                            binding.etLocation.setText(location);

                            setTimeToTextClock(binding.tcStart, snapshotStartTime);
                            setTimeToTextClock(binding.tcEnd, snapshotEndTime);

                            return;
                        }
                    }
                } else {
                    Toast.makeText(EditItineraryTrackingActivity.this, "Data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditItineraryTrackingActivity.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setTimeToTextClock(TextClock textClock, long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        String time = String.format("%02d:%02d", hour, minute);

        textClock.setFormat24Hour(time);
    }


    private void showTimePickerDialog(long initialTime, TextClock textClock) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(initialTime);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfDay) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minuteOfDay);
                    textClock.setText(DateFormat.format("HH:mm", calendar));
                    if (textClock.getId() == R.id.tcStart) {
                        startCalendar = calendar;
                        startTimeSelected = true;
                    } else if (textClock.getId() == R.id.tcEnd) {
                        endCalendar = calendar;
                        endTimeSelected = true;
                    }
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void checkAndSaveItinerary() {
        String name = binding.etName.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();

        if (!startTimeSelected && !endTimeSelected) {
            updateNameAndLocation(name, location);
        } else {
            if (!startTimeSelected || !endTimeSelected) {
                Toast.makeText(this, "Please select both start and end times", Toast.LENGTH_SHORT).show();
                return;
            }
            checkTimeSelectionAndSaveItinerary(name, location);
        }
    }

    private void checkTimeSelectionAndSaveItinerary(String name, String location) {
        String startTime = DateFormat.format("HH:mm", startCalendar).toString();
        String endTime = DateFormat.format("HH:mm", endCalendar).toString();

        Query query = itineraryRef.orderByChild("startTime").equalTo(startTime);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(EditItineraryTrackingActivity.this, "Start time already exists", Toast.LENGTH_SHORT).show();
                } else {
                    checkEndTimeAndSaveItinerary(name, location, startTime, endTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditItineraryTrackingActivity.this, "Failed to query database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkEndTimeAndSaveItinerary(String name, String location, String startTime, String endTime) {
        Query query = itineraryRef.orderByChild("endTime").equalTo(endTime);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(EditItineraryTrackingActivity.this, "End time already exists", Toast.LENGTH_SHORT).show();
                } else {
                    updateItineraryToFirebase(name, location, startTime, endTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditItineraryTrackingActivity.this, "Failed to query database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateItineraryToFirebase(String name, String location, String startTime, String endTime) {
        itineraryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        long snapshotStartTime = dataSnapshot.child("startTime").getValue(Long.class);
                        long snapshotEndTime = dataSnapshot.child("endTime").getValue(Long.class);

                        if (snapshotStartTime == startCalendar.getTimeInMillis() && snapshotEndTime == endCalendar.getTimeInMillis()) {
                            String key = dataSnapshot.getKey();
                            if (key != null) {
                                Itinerary itinerary = new Itinerary(name, location, startCalendar.getTimeInMillis(), endCalendar.getTimeInMillis(), date, dateMilis);
                                itineraryRef.child(key).setValue(itinerary)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(EditItineraryTrackingActivity.this, "Itinerary updated successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Toast.makeText(EditItineraryTrackingActivity.this, "Failed to update itinerary", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            return;
                        }
                    }
                }
                Toast.makeText(EditItineraryTrackingActivity.this, "Data not found in Firebase", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditItineraryTrackingActivity.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNameAndLocation(String name, String location) {
        itineraryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        long snapshotStartTime = dataSnapshot.child("startTime").getValue(Long.class);
                        long snapshotEndTime = dataSnapshot.child("endTime").getValue(Long.class);

                        String key = dataSnapshot.getKey();
                        if (key != null) {
                            Itinerary itinerary = new Itinerary(name, location, snapshotStartTime, snapshotEndTime, date, dateMilis);
                            itineraryRef.child(key).setValue(itinerary)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(EditItineraryTrackingActivity.this, "Name and location updated successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(EditItineraryTrackingActivity.this, "Failed to update name and location", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        return;
                    }
                }
                Toast.makeText(EditItineraryTrackingActivity.this, "Data not found in Firebase", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditItineraryTrackingActivity.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}