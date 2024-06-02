package com.example.tripwise.Activity.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tripwise.Activity.view.auth.SignInFragment;
import com.example.tripwise.Activity.view.auth.SignUpFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new SignInFragment();
            case 1:
                return new SignUpFragment();
            default:
                return new SignInFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
