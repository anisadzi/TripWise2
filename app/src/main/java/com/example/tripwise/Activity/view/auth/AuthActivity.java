package com.example.tripwise.Activity.view.auth;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tripwise.Activity.view.adapter.ViewPagerAdapter;
import com.example.tripwise.R;
import com.example.tripwise.databinding.ActivityAuthBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class AuthActivity extends AppCompatActivity {

    // Binding object to access views defined in XML
    private ActivityAuthBinding binding;
    // Adapter for managing the fragments in the ViewPager
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge content
        EdgeToEdge.enable(this);

        // Initialize view binding
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply window insets to adjust view padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the ViewPager adapter
        viewPagerAdapter = new ViewPagerAdapter(this);
        binding.viewPager2.setAdapter(viewPagerAdapter);

        // Connect the TabLayout with the ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager2,
                (tab, position) -> {
                    // Set the tab titles based on the position
                    switch (position) {
                        case 0:
                            tab.setText("Sign-In");
                            break;
                        case 1:
                            tab.setText("Sign-Up");
                            break;
                    }
                }).attach();
    }
}
