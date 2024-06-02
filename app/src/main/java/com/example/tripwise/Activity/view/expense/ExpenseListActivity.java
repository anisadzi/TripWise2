package com.example.tripwise.Activity.view.expense;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tripwise.Activity.view.expense.adapter.ExpenseListAdapter;
import com.example.tripwise.Activity.view.expense.item.Expense;
import com.example.tripwise.R;
import com.example.tripwise.databinding.ActivityExpenseListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExpenseListActivity extends AppCompatActivity {

    private ActivityExpenseListBinding binding;
    private ExpenseListAdapter expenseListAdapter;
    private List<Expense> expenseList;
    private DatabaseReference userExpenseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);

        // Inflate the layout using view binding
        binding = ActivityExpenseListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Expense Tracking List");

        // Set up the RecyclerView for displaying expenses
        binding.rvExpense.setLayoutManager(new LinearLayoutManager(this));

        // Initialize list of expenses and the adapter
        expenseList = new ArrayList<>();
        expenseListAdapter = new ExpenseListAdapter(expenseList, this);
        binding.rvExpense.setAdapter(expenseListAdapter);

        // Check if a user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // If logged in, get the user's UID
            String userId = currentUser.getUid();
            // Get reference to user's expenses in Firebase database
            userExpenseRef = FirebaseDatabase.getInstance().getReference("expense").child(userId);
            // Fetch expenses from Firebase
            fetchExpenses();
        } else {
            // Show a toast message if the user is not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to fetch expenses from Firebase database
    private void fetchExpenses() {
        userExpenseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear existing list of expenses
                expenseList.clear();
                // Iterate through each expense snapshot
                for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
                    // Deserialize the expense snapshot into an Expense object
                    Expense expense = expenseSnapshot.getValue(Expense.class);
                    // Add the expense to the list if it's not null
                    if (expense != null) {
                        expenseList.add(expense);
                    }
                }
                // Update the adapter with the new list of expenses
                expenseListAdapter.setExpenseList(expenseList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Show a toast message if failed to load expenses
                Toast.makeText(ExpenseListActivity.this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Override the up navigation button to act as back navigation
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
