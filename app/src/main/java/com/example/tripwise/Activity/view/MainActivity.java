package com.example.tripwise.Activity.view;

import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.example.tripwise.Activity.view.expense.ExpenseTrackingActivity;
import com.example.tripwise.Activity.item.UpcomingTrip;
import com.example.tripwise.Activity.view.adapter.UpcomingTripAdapter;
import com.example.tripwise.Activity.view.budget.BudgetActivity;
import com.example.tripwise.Activity.view.budget.item.Budget;
import com.example.tripwise.Activity.view.itinerary.ItineraryTrackingActivity;
import com.example.tripwise.Activity.view.profile.ProfileActivity;
import com.example.tripwise.R;
import com.example.tripwise.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private boolean isImageVisible = true;
    private List<UpcomingTrip> upcomingTripList;
    private UpcomingTripAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        setupScrollListener();
        getUserAccount();
        getDataBudget();
        getUpcomingTrips();

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
        binding.btnBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BudgetActivity.class);
                startActivity(intent);
            }
        });
        binding.btnExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ExpenseTrackingActivity.class);
                startActivity(intent);
            }
        });
        binding.btnItinerary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ItineraryTrackingActivity.class);
                startActivity(intent);
            }
        });
        binding.searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getUserAccount() {
        String userId = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String photoUrl = snapshot.child("photoUrl").getValue(String.class);
                    binding.nameTextView.setText("Hello, " + name + "!");
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Picasso.get()
                                .load(photoUrl)
                                .placeholder(R.drawable.ic_account)
                                .into(binding.profileImage);
                    }
                }
            } else {
                Toast.makeText(this, "Failed to fetch account information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupScrollListener() {
        binding.nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > 0 && isImageVisible) {
                    isImageVisible = false;
                    binding.imageView.animate().alpha(0.0f).setDuration(300).withEndAction(() -> binding.imageView.setVisibility(View.GONE));
                } else if (scrollY == 0 && !isImageVisible) {
                    isImageVisible = true;
                    binding.imageView.setVisibility(View.VISIBLE);
                    binding.imageView.animate().alpha(1.0f).setDuration(300);
                }
            }
        });
    }

    private void getDataBudget() {
        String userId = currentUser.getUid();
        DatabaseReference budgetRef = FirebaseDatabase.getInstance().getReference("budgets").child(userId);

        budgetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean hasOngoingBudget = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Budget budget = snapshot.getValue(Budget.class);
                    if (budget != null && isOngoing(budget.getStartDate(), budget.getEndDate())) {
                        displayOngoingBudget(budget);
                        hasOngoingBudget = true;
                        break;
                    }
                }
                if (!hasOngoingBudget) {
                    displayNoOngoingBudget();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to fetch budget data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isOngoing(long startDate, long endDate) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        return startDate <= currentTime && currentTime <= endDate;
    }

    private void displayOngoingBudget(Budget budget) {
        binding.textViewBefore.setVisibility(View.GONE);
        binding.llAfter.setVisibility(View.VISIBLE);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        String dateRange = sdf.format(budget.getStartDate()) + " - " + sdf.format(budget.getEndDate());
        binding.txtDateRange.setText(dateRange);

        long diff = budget.getEndDate() - budget.getStartDate();
        long days = diff / (1000 * 60 * 60 * 24);
        binding.txtNightsDays.setText(days + "N" + (days + 1) + "D");

        binding.txtNameTrip.setText(budget.getTripName());
        binding.txtPriceTrip.setText("Total trip budget: Rp " + budget.getBudgetAmount());
    }

    private void displayNoOngoingBudget() {
        binding.textViewBefore.setVisibility(View.VISIBLE);
        binding.llAfter.setVisibility(View.GONE);
    }

    private void getUpcomingTrips() {
        String userId = currentUser.getUid();
        DatabaseReference tripsRef = FirebaseDatabase.getInstance().getReference("budgets").child(userId);

        tripsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                upcomingTripList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String tripName = snapshot.child("tripName").getValue(String.class);
                    String tripLocation = snapshot.child("tripLocation").getValue(String.class);
                    long tripStartDate = snapshot.child("startDate").getValue(Long.class);
                    long tripEndDate = snapshot.child("endDate").getValue(Long.class);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                    String dateRange = sdf.format(tripStartDate) + " - " + sdf.format(tripEndDate);

                    if (!isOngoing(tripStartDate, tripEndDate)){
                        long diff = tripEndDate - tripStartDate;
                        long days = diff / (1000 * 60 * 60 * 24);
                        String nightsDays = (days + "N" + (days + 1) + "D");
                        String countdown = calculateCountdown(tripStartDate);
                        upcomingTripList.add(new UpcomingTrip(countdown, dateRange, nightsDays, tripName, tripLocation));
                        if (isUpcomingWithinThreshold(tripStartDate, 1)) {
                            sendNotification(tripName, tripLocation, countdown);
                        }
                    }


                }
                displayUpcomingTrips();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to fetch upcoming trips", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String calculateCountdown(long startDate) {
        long currentTime = Calendar.getInstance().getTimeInMillis();

        if (currentTime < startDate) {
            long diff = startDate - currentTime;
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
            return String.format(Locale.getDefault(), "%02dD : %02dH : %02dM", days, hours, minutes);
        } else {
            return "Trip has started";
        }
    }
    private void sendNotification(String tripName, String tripLocation, String countdown) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String userId = currentUser.getUid();

        FirebaseMessaging.getInstance().subscribeToTopic("upcoming_trip");
        String notificationTitle = "Upcoming Trip: " + tripName;
        String notificationMessage = "Your trip to " + tripLocation + " is starting soon! Countdown: " + countdown;

        RemoteMessage notification = new RemoteMessage.Builder("594529180593" + "@fcm.googleapis.com")
                .setMessageId(Integer.toString(1))
                .addData("title", notificationTitle)
                .addData("body", notificationMessage)
                .addData("userId", userId)
                .build();

        FirebaseMessaging.getInstance().send(notification);
    }

    private boolean isUpcomingWithinThreshold(long startDate, int daysThreshold) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long thresholdTime = startDate - TimeUnit.DAYS.toMillis(daysThreshold);
        return currentTime >= thresholdTime;
    }
    private void displayUpcomingTrips() {
        adapter = new UpcomingTripAdapter(this, upcomingTripList);
        binding.rvUpComingTrip.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUpComingTrip.setAdapter(adapter);
    }
}
