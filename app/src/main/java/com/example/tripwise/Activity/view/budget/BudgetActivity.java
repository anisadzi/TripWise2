package com.example.tripwise.Activity.view.budget;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tripwise.Activity.view.budget.item.Category;
import com.example.tripwise.Activity.view.MainActivity;
import com.example.tripwise.Activity.view.budget.adapter.CategoryAdapter;
import com.example.tripwise.Activity.view.budget.fragmentdialog.AddCategoryFragment;
import com.example.tripwise.Activity.view.budget.item.Budget;
import com.example.tripwise.databinding.ActivityBudgetBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BudgetActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private ActivityBudgetBinding binding;
    private List<Category> categoryList = new ArrayList<>();
    private CategoryAdapter categoryAdapter;
    private Calendar startDateCalendar;
    private Calendar endDateCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBudgetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Budget");

        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();
        endDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
        binding.rvTripCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this, categoryList);
        binding.rvTripCategories.setAdapter(categoryAdapter);

        binding.fabAdd.setOnClickListener(view -> openCategoryDialog());
        binding.tvStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        binding.tvEndDate.setOnClickListener(v -> showDatePickerDialog(false));

        binding.btnSave.setOnClickListener(v -> checkAndSaveBudget());
        retrieveCategoriesFromSharedPreferences();
        binding.tvStartDate.setText(formatDate(startDateCalendar));
        binding.tvEndDate.setText(formatDate(endDateCalendar));
    }

    private void openCategoryDialog() {
        AddCategoryFragment dialogFragment = new AddCategoryFragment();
        dialogFragment.show(getSupportFragmentManager(), "addCategoryDialog");
    }

    public void retrieveCategoriesFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("category_prefs", Context.MODE_PRIVATE);
        String categoriesJson = sharedPreferences.getString("categories", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<Category>>(){}.getType();
        categoryList = gson.fromJson(categoriesJson, type);
        if (categoryList == null) {
            categoryList = new ArrayList<>();
        }
        categoryAdapter.setData(categoryList);
        categoryAdapter.notifyDataSetChanged();
    }


    private void showDatePickerDialog(boolean isStartDate) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        if (isStartDate) {
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.getDatePicker().setTag("startDate");
        } else {
            datePickerDialog.getDatePicker().setMinDate(startDateCalendar.getTimeInMillis() + (24 * 60 * 60 * 1000));
            datePickerDialog.getDatePicker().setTag("endDate");
        }
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String tag = (String) view.getTag();
        if (tag != null) {
            if (tag.equals("startDate")) {
                startDateCalendar.set(Calendar.YEAR, year);
                startDateCalendar.set(Calendar.MONTH, month);
                startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                binding.tvStartDate.setText(formatDate(startDateCalendar));

                if (endDateCalendar.getTimeInMillis() <= startDateCalendar.getTimeInMillis()) {
                    endDateCalendar.setTimeInMillis(startDateCalendar.getTimeInMillis());
                    endDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    binding.tvEndDate.setText(formatDate(endDateCalendar));
                }
            } else if (tag.equals("endDate")) {
                endDateCalendar.set(Calendar.YEAR, year);
                endDateCalendar.set(Calendar.MONTH, month);
                endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                binding.tvEndDate.setText(formatDate(endDateCalendar));
            }
        }
    }
    private void checkAndSaveBudget() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference userBudgetsRef = FirebaseDatabase.getInstance().getReference("budgets").child(userId);

        long newStartDate = startDateCalendar.getTimeInMillis();
        long newEndDate = endDateCalendar.getTimeInMillis();

        userBudgetsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean hasOverlap = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    long existingStartDate = snapshot.child("startDate").getValue(Long.class);
                    long existingEndDate = snapshot.child("endDate").getValue(Long.class);

                    if ((newStartDate >= existingStartDate && newStartDate <= existingEndDate) ||
                            (newEndDate >= existingStartDate && newEndDate <= existingEndDate) ||
                            (existingStartDate >= newStartDate && existingStartDate <= newEndDate)) {
                        hasOverlap = true;
                        break;
                    }
                }

                if (hasOverlap) {
                    Toast.makeText(BudgetActivity.this, "Selected dates overlap with an existing budget", Toast.LENGTH_LONG).show();
                } else {
                    saveBudget(userBudgetsRef);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(BudgetActivity.this, "Failed to check existing budgets", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveBudget(DatabaseReference userBudgetsRef) {
        String budgetAmount = binding.etBudgetAmount.getText().toString();
        String tripName = binding.etTripName.getText().toString();
        String tripLocation = binding.etTripLocation.getText().toString();

        if (budgetAmount.isEmpty() || tripName.isEmpty() || tripLocation.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoryList.isEmpty()){
            Toast.makeText(this, "Please add at least one category", Toast.LENGTH_LONG).show();
            return;
        }

        Budget budget = new Budget();
        budget.setBudgetAmount(Double.parseDouble(budgetAmount));
        budget.setTripName(tripName);
        budget.setStartDate(startDateCalendar.getTimeInMillis());
        budget.setEndDate(endDateCalendar.getTimeInMillis());
        budget.setTripLocation(tripLocation);
        budget.setCategoryList(categoryList);

        userBudgetsRef.push().setValue(budget)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Budget saved successfully", Toast.LENGTH_SHORT).show();
                        deleteAllCategories();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to save budget", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        deleteAllCategories();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteAllCategories();
    }

    @Override
    protected void onResume() {
        super.onResume();
        retrieveCategoriesFromSharedPreferences();
    }

    private void deleteAllCategories() {
        SharedPreferences sharedPreferences = getSharedPreferences("category_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }


    @SuppressLint("DefaultLocale")
    private String formatDate(Calendar calendar) {
        return String.format("%02d/%02d/%d", calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
    }

}
