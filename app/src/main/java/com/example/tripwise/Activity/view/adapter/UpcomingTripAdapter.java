package com.example.tripwise.Activity.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripwise.Activity.item.UpcomingTrip;
import com.example.tripwise.databinding.UpcomingtripItemBinding;
import com.squareup.picasso.Picasso;
import java.util.List;

// Adapter class for managing upcoming trip items in a RecyclerView
public class UpcomingTripAdapter extends RecyclerView.Adapter<UpcomingTripAdapter.UpcomingTripViewHolder> {

    // List to store upcoming trips
    private final List<UpcomingTrip> upcomingTripList;
    // Context to access application-specific resources
    private final Context context;

    // Constructor to initialize the adapter with context and list of upcoming trips
    public UpcomingTripAdapter(Context context, List<UpcomingTrip> upcomingTripList) {
        this.context = context;
        this.upcomingTripList = upcomingTripList;
    }

    // Inflates the item layout and creates the ViewHolder object
    @NonNull
    @Override
    public UpcomingTripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        UpcomingtripItemBinding binding = UpcomingtripItemBinding.inflate(inflater, parent, false);
        return new UpcomingTripViewHolder(binding);
    }

    // Binds the data to the ViewHolder at the specified position
    @Override
    public void onBindViewHolder(@NonNull UpcomingTripViewHolder holder, int position) {
        UpcomingTrip trip = upcomingTripList.get(position);
        holder.bind(trip);
    }

    // Returns the total number of items in the data set held by the adapter
    @Override
    public int getItemCount() {
        return upcomingTripList.size();
    }

    // ViewHolder class to represent an individual item view in the RecyclerView
    public static class UpcomingTripViewHolder extends RecyclerView.ViewHolder {

        // Binding object to access the views in the layout
        private final UpcomingtripItemBinding binding;

        // Constructor to initialize the ViewHolder with the binding object
        public UpcomingTripViewHolder(@NonNull UpcomingtripItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Method to bind the trip data to the views in the layout
        public void bind(UpcomingTrip trip) {
            binding.txtCoundownToOnTrip.setText(trip.getCountdown());
            binding.txtDateRange.setText(trip.getDateRange());
            binding.txtNightsDays.setText(trip.getNightsDays());
            binding.txtNameTrip.setText(trip.getNameTrip());
            binding.txtLocationTrip.setText(trip.getLocationTrip());
            // Assuming that there is an image URL in the UpcomingTrip class, uncomment the next line if applicable
            // Picasso.get().load(trip.getImageUrl()).into(binding.imageViewTrip);
        }
    }
}
