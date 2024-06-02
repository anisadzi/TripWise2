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
        EdgeToEdge.enable(this);
        binding = ActivityExpenseListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Expense Tracking List");
        binding.rvExpense.setLayoutManager(new LinearLayoutManager(this));

        expenseList = new ArrayList<>();
        expenseListAdapter = new ExpenseListAdapter(expenseList, this);
        binding.rvExpense.setAdapter(expenseListAdapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            userExpenseRef = FirebaseDatabase.getInstance().getReference("expense").child(userId);
            fetchExpenses();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
    private void fetchExpenses() {
        userExpenseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseList.clear();
                for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
                    Expense expense = expenseSnapshot.getValue(Expense.class);
                    if (expense != null) {
                        expenseList.add(expense);
                    }
                }
                expenseListAdapter.setExpenseList(expenseList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExpenseListActivity.this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}