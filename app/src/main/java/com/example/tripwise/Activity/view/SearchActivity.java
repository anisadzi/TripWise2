package com.example.tripwise.Activity.view;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tripwise.Activity.view.itinerary.adapter.ItineraryAdapter;
import com.example.tripwise.Activity.view.itinerary.item.Itinerary;
import com.example.tripwise.R;
import com.example.tripwise.databinding.ActivitySearchBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private DatabaseReference itineraryRef;
    private List<Itinerary> itineraryList;
    private ItineraryAdapter itineraryAdapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Search");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            itineraryRef = FirebaseDatabase.getInstance().getReference("itinerary").child(userId);
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        itineraryList = new ArrayList<>();
        itineraryAdapter = new ItineraryAdapter(this, itineraryList);
        binding.rvSearch.setAdapter(itineraryAdapter);
        binding.rvSearch.setLayoutManager(new LinearLayoutManager(this));

        loadPastItineraries();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchItineraries(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    private void loadPastItineraries() {
        long today = Calendar.getInstance().getTimeInMillis();
        Query query = itineraryRef.orderByChild("dateMilis").endAt(today);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                itineraryList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Itinerary itinerary = snapshot.getValue(Itinerary.class);
                    if (itinerary != null) {
                        itineraryList.add(itinerary);
                        itineraryAdapter.updateList(itineraryList);
                    }
                }
                itineraryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SearchActivity.this, "Failed to load itineraries", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchItineraries(String query) {
        List<Itinerary> filteredList = new ArrayList<>();
        for (Itinerary itinerary : itineraryList) {
            if (itinerary.getName().toLowerCase().contains(query.toLowerCase()) ||
                    itinerary.getLocation().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(itinerary);
            }
        }
        itineraryAdapter.updateList(filteredList);
    }

}
