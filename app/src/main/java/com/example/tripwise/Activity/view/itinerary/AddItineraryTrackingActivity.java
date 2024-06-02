package com.example.tripwise.Activity.view.itinerary;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.widget.TextClock;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tripwise.Activity.view.itinerary.item.Itinerary;
import com.example.tripwise.R;
import com.example.tripwise.databinding.ActivityAddItineraryTrackingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class AddItineraryTrackingActivity extends AppCompatActivity {

    private ActivityAddItineraryTrackingBinding binding;
    private DatabaseReference itineraryRef;
    private FirebaseUser currentUser;
    private Calendar startCalendar, endCalendar;
    private boolean startTimeSelected, endTimeSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddItineraryTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setting padding to handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setting up the toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Itinerary Tracking");

        // Initializing Firebase components
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            itineraryRef = FirebaseDatabase.getInstance().getReference("itinerary").child(userId);
        }

        // Initializing start and end time calendars
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        // Setting onClickListeners for start and end time TextClocks
        binding.tcStart.setOnClickListener(v -> showTimePickerDialog(startCalendar, binding.tcStart));
        binding.tcEnd.setOnClickListener(v -> showTimePickerDialog(endCalendar, binding.tcEnd));

        // Setting onClickListener for the "Add" button
        binding.btnAdd.setOnClickListener(v -> checkAndSaveItinerary());
    }

    // Method to show TimePickerDialog
    private void showTimePickerDialog(Calendar calendar, TextClock textClock) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfDay) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minuteOfDay);
                    textClock.setText(DateFormat.format("HH:mm", calendar));
                    if (textClock.getId() == R.id.tcStart) {
                        startTimeSelected = true;
                    } else if (textClock.getId() == R.id.tcEnd) {
                        endTimeSelected = true;
                    }
                    checkTimeSelection();
                }, hour, minute, true);
        timePickerDialog.show();
    }

    // Method to check if both start and end times are selected
    private void checkTimeSelection() {
        if (startTimeSelected && endTimeSelected) {
            String startTime = DateFormat.format("HH:mm", startCalendar).toString();
            String endTime = DateFormat.format("HH:mm", endCalendar).toString();
            if (startTime.equals(endTime)) {
                Toast.makeText(this, "Start time cannot be the same as end time", Toast.LENGTH_SHORT).show();
                startTimeSelected = false;
                endTimeSelected = false;
            }
        }
    }

    // Method to check and save the itinerary to Firebase
    private void checkAndSaveItinerary() {
        if (!startTimeSelected || !endTimeSelected) {
            Toast.makeText(this, "Please select both start and end times", Toast.LENGTH_SHORT).show();
            return;
        }

        String startTime = DateFormat.format("HH:mm", startCalendar).toString();
        String endTime = DateFormat.format("HH:mm", endCalendar).toString();

        Query startQuery = itineraryRef.orderByChild("startTime").equalTo(startTime);
        startQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(AddItineraryTrackingActivity.this, "Start time already exists", Toast.LENGTH_SHORT).show();
                } else {
                    checkEndTimeAndSaveItinerary(endTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddItineraryTrackingActivity.this, "Failed to query database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to check if the end time already exists in the database
    private void checkEndTimeAndSaveItinerary(String endTime) {
        Query endQuery = itineraryRef.orderByChild("endTime").equalTo(endTime);
        endQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(AddItineraryTrackingActivity.this, "End time already exists", Toast.LENGTH_SHORT).show();
                } else {
                    saveItineraryToFirebase();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddItineraryTrackingActivity.this, "Failed to query database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to save the itinerary to Firebase
    private void saveItineraryToFirebase() {
        String name = binding.etName.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();
        long startTime = startCalendar.getTimeInMillis();
        long endTime = endCalendar.getTimeInMillis();
        Intent intent = getIntent();
        String date = intent.getStringExtra("date");
        long dateMilis = intent.getLongExtra("dateMilis", 0);

        if (itineraryRef != null) {
            String key = itineraryRef.push().getKey();
            if (key != null) {
                Itinerary itinerary = new Itinerary(name, location, startTime, endTime, date, dateMilis);
                itineraryRef.child(key).setValue(itinerary)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Itinerary saved successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(this, "Failed to save itinerary", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

}
