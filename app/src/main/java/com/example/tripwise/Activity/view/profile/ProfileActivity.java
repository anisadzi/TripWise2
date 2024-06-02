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
import com.example.tripwise.Activity.view.expense.ExpenseTrackingActivity;
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

    private ActivityProfileBinding binding;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");

        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        binding.editProfileBtn.setOnClickListener(view -> navigateToEditProfileActivity());
        binding.textLogOut.setOnClickListener(view -> showLogoutConfirmationDialog());

        binding.textBudgetAllocate.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, BudgetActivity.class);
            startActivity(intent);
        });
        binding.textExpenseTracking.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, ExpenseListActivity.class);
            startActivity(intent);
        });
        binding.textItineraryList.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, ItineraryTrackingActivity.class);
            startActivity(intent);
        });

        getAccounbtInfo();
    }
    private void getAccounbtInfo(){
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

                    binding.textName.setText(name);
                    binding.textPhoneNumber.setText(phoneNumber);
                    binding.textEmail.setText(email);

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
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                logout();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLogin", false);
        editor.clear();
        editor.apply();

        Intent intent = new Intent(ProfileActivity.this, AuthActivity.class);
        startActivity(intent);
        finishAffinity();
    }
    private void navigateToEditProfileActivity() {
        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
        startActivity(intent);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}