package com.example.tripwise.Activity.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tripwise.Activity.view.auth.AuthActivity;
import com.example.tripwise.databinding.ActivitySplashScreenBinding;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    private ActivitySplashScreenBinding binding; // View binding for the layout
    private SharedPreferences sharedPreferences; // SharedPreferences to manage user login state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge display
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Delay for 3 seconds and then check the login state
        new Handler().postDelayed(() -> {
            if (isUserLoggedIn()) {
                goToMainActivity(); // Navigate to MainActivity if user is logged in
            } else {
                goToSignInActivity(); // Navigate to AuthActivity if user is not logged in
            }
            finish(); // Close SplashScreenActivity
        }, 3000); // 3 seconds delay
    }

    // Check if the user is logged in based on SharedPreferences
    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean("isLogin", false);
    }

    // Navigate to MainActivity
    private void goToMainActivity() {
        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
        startActivity(intent);
    }

    // Navigate to AuthActivity
    private void goToSignInActivity() {
        Intent intent = new Intent(SplashScreenActivity.this, AuthActivity.class);
        startActivity(intent);
    }
}
