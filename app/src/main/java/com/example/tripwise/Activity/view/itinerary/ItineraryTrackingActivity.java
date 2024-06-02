package com.example.tripwise.Activity.view.itinerary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tripwise.Activity.view.itinerary.adapter.ItineraryAdapter;
import com.example.tripwise.Activity.view.itinerary.item.Itinerary;
import com.example.tripwise.R;
import com.example.tripwise.databinding.ActivityItineraryTrackingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ItineraryTrackingActivity extends AppCompatActivity {

    private ActivityItineraryTrackingBinding binding;
    private Calendar calendar;
    private DatabaseReference itineraryRef;
    private List<Itinerary> itineraryList;
    private ItineraryAdapter itineraryAdapter;
    private String userId;
    private long selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityItineraryTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Itinerary Tracking");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            itineraryRef = FirebaseDatabase.getInstance().getReference("itinerary").child(userId);
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        calendar = Calendar.getInstance();
        selectedDate = calendar.getTimeInMillis();
        binding.tvDate.setText(formatDate(calendar));

        binding.calendarView.setDate(selectedDate, false, true);
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            selectedDate = calendar.getTimeInMillis();
            binding.tvDate.setText(formatDate(calendar));
            loadItinerary();
        });

        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddItineraryTrackingActivity.class);
            intent.putExtra("date", formatDate(calendar));
            intent.putExtra("dateMilis", selectedDate);
            startActivity(intent);
        });

        itineraryList = new ArrayList<>();
        itineraryAdapter = new ItineraryAdapter(this, itineraryList);
        binding.rvItinerary.setAdapter(itineraryAdapter);
        binding.rvItinerary.setLayoutManager(new LinearLayoutManager(this));

        loadItinerary();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private String formatDate(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    private void loadItinerary() {
        String selectedDate = formatDate(calendar);
        itineraryRef.orderByChild("date").equalTo(selectedDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                itineraryList.clear();
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Itinerary itinerary = snapshot.getValue(Itinerary.class);
                        if (itinerary != null) {
                            itineraryList.add(itinerary);
                        }
                    }
                    itineraryAdapter.notifyDataSetChanged();
                } else {
                    itineraryAdapter.notifyDataSetChanged();
                    Toast.makeText(ItineraryTrackingActivity.this, "No itinerary found for selected date", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ItineraryTrackingActivity.this, "Failed to load itinerary", Toast.LENGTH_SHORT).show();
            }
        });
    }



}
