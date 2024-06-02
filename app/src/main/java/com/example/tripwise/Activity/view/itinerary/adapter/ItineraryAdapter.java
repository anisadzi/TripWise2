package com.example.tripwise.Activity.view.itinerary.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripwise.Activity.view.itinerary.EditItineraryTrackingActivity;
import com.example.tripwise.Activity.view.itinerary.item.Itinerary;
import com.example.tripwise.databinding.ItineraryItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ViewHolder> {

    private Context mContext;
    private List<Itinerary> mItineraryList;
    private DatabaseReference mItineraryRef;
    private String mUserId;

    // Constructor to initialize the adapter
    public ItineraryAdapter(Context context, List<Itinerary> itineraryList) {
        mContext = context;
        mItineraryList = itineraryList;

        // Get current user ID and setup Firebase reference to the itinerary
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            mUserId = currentUser.getUid();
            mItineraryRef = FirebaseDatabase.getInstance().getReference("itinerary").child(mUserId);
        }
    }

    // Create view holder for the itinerary item
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ItineraryItemBinding binding = ItineraryItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    // Bind data to the view holder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Itinerary itinerary = mItineraryList.get(position);
        String startTime = DateFormat.format("HH:mm", itinerary.getStartTime()).toString();
        String endTime = DateFormat.format("HH:mm", itinerary.getEndTime()).toString();
        holder.binding.tvStartEndTime.setText(startTime + " - " + endTime);
        holder.binding.tvNameTrip.setText(itinerary.getName());
        holder.binding.tvLocation.setText(itinerary.getLocation());

        // Click listener for editing the itinerary
        holder.binding.tvEdit.setOnClickListener(v -> {
            startEditItineraryActivity(itinerary.getStartTime(), itinerary.getEndTime(), itinerary.getDate());
        });

        // Click listener for deleting the itinerary
        holder.binding.tvDelete.setOnClickListener(v -> {
            deleteItinerary(itinerary, position);
        });

        // Click listener for opening location in map
        holder.itemView.setOnClickListener(v -> {
            openLocationInMap(itinerary.getLocation());
        });
    }

    // Get the number of items in the list
    @Override
    public int getItemCount() {
        return mItineraryList.size();
    }

    // Start the EditItineraryTrackingActivity with necessary data
    private void startEditItineraryActivity(long startTime, long endTime, String date) {
        Intent intent = new Intent(mContext, EditItineraryTrackingActivity.class);
        intent.putExtra("startTime", startTime);
        intent.putExtra("endTime", endTime);
        intent.putExtra("date", date);
        mContext.startActivity(intent);
    }

    // Delete the itinerary
    private void deleteItinerary(Itinerary itinerary, int position) {
        if (mItineraryList.isEmpty() || position < 0 || position >= mItineraryList.size()) {
            return;
        }

        // Query to find the itinerary to delete
        Query query = mItineraryRef.orderByChild("startTime").equalTo(itinerary.getStartTime());
        mItineraryList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mItineraryList.size());

        // Event listener to delete the itinerary from Firebase
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Show success message if deletion is successful
                                    Toast.makeText(mContext, "Itinerary deleted successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Show error message if deletion fails
                                    Toast.makeText(mContext, "Failed to delete itinerary", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Show error message if deletion is cancelled
                Toast.makeText(mContext, "Failed to delete itinerary", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Open the location in map
    private void openLocationInMap(String location) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        mContext.startActivity(mapIntent);
    }

    // Update the list with new data
    public void updateList(List<Itinerary> newList) {
        mItineraryList = newList;
        notifyDataSetChanged();
    }

    // View holder class to hold the views of the itinerary item
    public class ViewHolder extends RecyclerView.ViewHolder {
        ItineraryItemBinding binding;

        // Constructor to initialize the view holder with the binding
        public ViewHolder(ItineraryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
