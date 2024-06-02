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

    private ActivityItineraryTrackingBinding binding; // View binding for the layout
    private Calendar calendar; // Calendar instance for date manipulation
    private DatabaseReference itineraryRef; // Firebase database reference
    private List<Itinerary> itineraryList; // List to hold itineraries
    private ItineraryAdapter itineraryAdapter; // Adapter for the RecyclerView
    private String userId; // User ID of the authenticated user
    private long selectedDate; // Selected date in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge display
        binding = ActivityItineraryTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Adjust the view's padding to accommodate system bars (status bar, navigation bar, etc.)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Itinerary Tracking");

        // Get the currently authenticated user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            itineraryRef = FirebaseDatabase.getInstance().getReference("itinerary").child(userId);
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize calendar and set selected date to current date
        calendar = Calendar.getInstance();
        selectedDate = calendar.getTimeInMillis();
        binding.tvDate.setText(formatDate(calendar)); // Display the selected date

        // Initialize the calendar view
        binding.calendarView.setDate(selectedDate, false, true);
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth); // Update calendar with selected date
            selectedDate = calendar.getTimeInMillis(); // Update selected date
            binding.tvDate.setText(formatDate(calendar)); // Display the selected date
            loadItinerary(); // Load itinerary for the selected date
        });

        // Set click listener for the floating action button to add a new itinerary
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddItineraryTrackingActivity.class);
            intent.putExtra("date", formatDate(calendar)); // Pass the selected date
            intent.putExtra("dateMilis", selectedDate); // Pass the selected date in milliseconds
            startActivity(intent); // Start the activity to add a new itinerary
        });

        // Initialize itinerary list and adapter
        itineraryList = new ArrayList<>();
        itineraryAdapter = new ItineraryAdapter(this, itineraryList);
        binding.rvItinerary.setAdapter(itineraryAdapter);
        binding.rvItinerary.setLayoutManager(new LinearLayoutManager(this)); // Set layout manager for RecyclerView

        loadItinerary(); // Load itinerary for the current date
    }

    // Handle back navigation
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Format the calendar date to a readable string
    private String formatDate(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    // Load itinerary from Firebase for the selected date
    private void loadItinerary() {
        String selectedDate = formatDate(calendar); // Format the selected date
        itineraryRef.orderByChild("date").equalTo(selectedDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                itineraryList.clear(); // Clear the existing list
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Itinerary itinerary = snapshot.getValue(Itinerary.class);
                        if (itinerary != null) {
                            itineraryList.add(itinerary); // Add the itinerary to the list
                        }
                    }
                    itineraryAdapter.notifyDataSetChanged(); // Notify adapter to update RecyclerView
                } else {
                    itineraryAdapter.notifyDataSetChanged(); // Notify adapter to update RecyclerView
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
