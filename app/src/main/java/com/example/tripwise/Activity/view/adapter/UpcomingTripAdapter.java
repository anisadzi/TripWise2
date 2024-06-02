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

public class UpcomingTripAdapter extends RecyclerView.Adapter<UpcomingTripAdapter.UpcomingTripViewHolder> {

    private final List<UpcomingTrip> upcomingTripList;
    private final Context context;

    public UpcomingTripAdapter(Context context, List<UpcomingTrip> upcomingTripList) {
        this.context = context;
        this.upcomingTripList = upcomingTripList;
    }

    @NonNull
    @Override
    public UpcomingTripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        UpcomingtripItemBinding binding = UpcomingtripItemBinding.inflate(inflater, parent, false);
        return new UpcomingTripViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UpcomingTripViewHolder holder, int position) {
        UpcomingTrip trip = upcomingTripList.get(position);
        holder.bind(trip);
    }

    @Override
    public int getItemCount() {
        return upcomingTripList.size();
    }

    public static class UpcomingTripViewHolder extends RecyclerView.ViewHolder {

        private final UpcomingtripItemBinding binding;

        public UpcomingTripViewHolder(@NonNull UpcomingtripItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(UpcomingTrip trip) {
            binding.txtCoundownToOnTrip.setText(trip.getCountdown());
            binding.txtDateRange.setText(trip.getDateRange());
            binding.txtNightsDays.setText(trip.getNightsDays());
            binding.txtNameTrip.setText(trip.getNameTrip());
            binding.txtLocationTrip.setText(trip.getLocationTrip());

        }
    }
}
