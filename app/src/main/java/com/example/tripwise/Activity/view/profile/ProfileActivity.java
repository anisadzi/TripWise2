package com.example.tripwise.Activity.view.profile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tripwise.Activity.view.auth.AuthActivity;
import com.example.tripwise.Activity.view.budget.BudgetActivity;
import com.example.tripwise.Activity.view.expense.ExpenseListActivity;
import com.example.tripwise.Activity.view.itinerary.ItineraryTrackingActivity;
import com.example.tripwise.R;
import com.example.tripwise.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding; // View binding for the layout
    private DatabaseReference mDatabase; // Reference to Firebase Realtime Database
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private FirebaseUser currentUser; // Currently authenticated user
    private SharedPreferences sharedPreferences; // SharedPreferences for storing user data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge display
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply window insets to handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Authentication and get the current user
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Set up the toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        // Set click listeners for various actions
        binding.editProfileBtn.setOnClickListener(view -> navigateToEditProfileActivity());
        binding.textLogOut.setOnClickListener(view -> showLogoutConfirmationDialog());
        binding.textBudgetAllocate.setOnClickListener(view -> startActivity(new Intent(ProfileActivity.this, BudgetActivity.class)));
        binding.textExpenseTracking.setOnClickListener(view -> startActivity(new Intent(ProfileActivity.this, ExpenseListActivity.class)));
        binding.textItineraryList.setOnClickListener(view -> startActivity(new Intent(ProfileActivity.this, ItineraryTrackingActivity.class)));

        // Load the current account information
        getAccountInfo();
    }

    // Fetch the user's account information from Firebase
    private void getAccountInfo() {
        String userId = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String photoUrl = snapshot.child("photoUrl").getValue(String.class);

                    // Set the retrieved information to the corresponding views
                    binding.textName.setText(name);
                    binding.textPhoneNumber.setText(phoneNumber);
                    binding.textEmail.setText(email);

                    // Load the profile image using Picasso
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

    // Show a confirmation dialog before logging out
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialogInterface, i) -> logout())
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Log out the user and clear the shared preferences
    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLogin", false);
        editor.clear();
        editor.apply();

        Intent intent = new Intent(ProfileActivity.this, AuthActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    // Navigate to the EditProfileActivity
    private void navigateToEditProfileActivity() {
        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
        startActivity(intent);
    }

    // Handle the back navigation
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
