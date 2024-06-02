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

    private ActivitySplashScreenBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        new Handler().postDelayed(() -> {
            if (isUserLoggedIn()) {
                goToMainActivity();
            } else {
                goToSignInActivity();
            }
            finish();
        }, 3000);
    }

    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean("isLogin", false);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void goToSignInActivity() {
        Intent intent = new Intent(SplashScreenActivity.this, AuthActivity.class);
        startActivity(intent);
    }
}
