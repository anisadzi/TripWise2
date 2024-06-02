package com.example.tripwise.Activity.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tripwise.Activity.view.auth.SignInFragment;
import com.example.tripwise.Activity.view.auth.SignUpFragment;

// Adapter class for managing fragments in a ViewPager2
public class ViewPagerAdapter extends FragmentStateAdapter {

    // Constructor to initialize the adapter with the fragment activity
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    // Creates the fragment to display at the specified position
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return the appropriate fragment based on the position
        switch (position) {
            case 0:
                return new SignInFragment(); // Return SignInFragment for the first tab
            case 1:
                return new SignUpFragment(); // Return SignUpFragment for the second tab
            default:
                return new SignInFragment(); // Default case returns SignInFragment
        }
    }

    // Returns the total number of fragments
    @Override
    public int getItemCount() {
        return 2; // There are two fragments: SignInFragment and SignUpFragment
    }
}
