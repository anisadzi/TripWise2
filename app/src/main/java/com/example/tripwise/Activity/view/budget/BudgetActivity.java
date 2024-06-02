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

        // Initialize start and end date calendars
        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();
        endDateCalendar.add(Calendar.DAY_OF_MONTH, 1);

        // Set up RecyclerView for displaying categories
        binding.rvTripCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this, categoryList);
        binding.rvTripCategories.setAdapter(categoryAdapter);

        // Set onClickListeners for adding categories and selecting start/end dates
        binding.fabAdd.setOnClickListener(view -> openCategoryDialog());
        binding.tvStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        binding.tvEndDate.setOnClickListener(v -> showDatePickerDialog(false));

        // Set onClickListener for saving the budget
        binding.btnSave.setOnClickListener(v -> checkAndSaveBudget());

        // Retrieve categories from SharedPreferences and display them
        retrieveCategoriesFromSharedPreferences();

        // Display the selected start and end dates
        binding.tvStartDate.setText(formatDate(startDateCalendar));
        binding.tvEndDate.setText(formatDate(endDateCalendar));
    }

    // Method to open the dialog for adding a new category
    private void openCategoryDialog() {
        AddCategoryFragment dialogFragment = new AddCategoryFragment();
        dialogFragment.show(getSupportFragmentManager(), "addCategoryDialog");
    }

    // Method to retrieve categories from SharedPreferences
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

    // Method to display the date picker dialog
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

    // Callback method when a date is set in the date picker dialog
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String tag = (String) view.getTag();
        if (tag != null) {
            if (tag.equals("startDate")) {
                // Update start date and display
                startDateCalendar.set(Calendar.YEAR, year);
                startDateCalendar.set(Calendar.MONTH, month);
                startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                binding.tvStartDate.setText(formatDate(startDateCalendar));

                // If end date is before start date, reset it to the next day
                if (endDateCalendar.getTimeInMillis() <= startDateCalendar.getTimeInMillis()) {
                    endDateCalendar.setTimeInMillis(startDateCalendar.getTimeInMillis());
                    endDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    binding.tvEndDate.setText(formatDate(endDateCalendar));
                }
            } else if (tag.equals("endDate")) {
                // Update end date and display
                endDateCalendar.set(Calendar.YEAR, year);
                endDateCalendar.set(Calendar.MONTH, month);
                endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                binding.tvEndDate.setText(formatDate(endDateCalendar));
            }
        }
    }

    // Method to check if there's any overlap with existing budgets and save the budget if there's no overlap
    private void checkAndSaveBudget() {
        // Get the current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Check if the user is logged in
        if (currentUser == null) {
            // Show a toast message if the user is not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the user ID
        String userId = currentUser.getUid();
        // Reference to the user's budgets node in the Firebase Realtime Database
        DatabaseReference userBudgetsRef = FirebaseDatabase.getInstance().getReference("budgets").child(userId);

        // Get the start and end dates of the new budget
        long newStartDate = startDateCalendar.getTimeInMillis();
        long newEndDate = endDateCalendar.getTimeInMillis();

        // Check for existing budgets
        userBudgetsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean hasOverlap = false;
                // Iterate through each existing budget
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get the start and end dates of the existing budget
                    long existingStartDate = snapshot.child("startDate").getValue(Long.class);
                    long existingEndDate = snapshot.child("endDate").getValue(Long.class);

                    // Check for overlap between new and existing budget dates
                    if ((newStartDate >= existingStartDate && newStartDate <= existingEndDate) ||
                            (newEndDate >= existingStartDate && newEndDate <= existingEndDate) ||
                            (existingStartDate >= newStartDate && existingStartDate <= newEndDate)) {
                        // Set flag to true if there's overlap
                        hasOverlap = true;
                        break;
                    }
                }

                // If there's overlap, show a toast message
                if (hasOverlap) {
                    Toast.makeText(BudgetActivity.this, "Selected dates overlap with an existing budget", Toast.LENGTH_LONG).show();
                } else {
                    // If no overlap, save the budget
                    saveBudget(userBudgetsRef);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Show a toast message if there's an error in accessing the database
                Toast.makeText(BudgetActivity.this, "Failed to check existing budgets", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to save the budget to the Firebase Realtime Database
    private void saveBudget(DatabaseReference userBudgetsRef) {
        // Retrieve budget details from the input fields
        String budgetAmount = binding.etBudgetAmount.getText().toString();
        String tripName = binding.etTripName.getText().toString();
        String tripLocation = binding.etTripLocation.getText().toString();

        // Validate if all fields are filled
        if (budgetAmount.isEmpty() || tripName.isEmpty() || tripLocation.isEmpty()) {
            // Show a toast message if any field is empty
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate if at least one category is added
        if (categoryList.isEmpty()){
            Toast.makeText(this, "Please add at least one category", Toast.LENGTH_LONG).show();
            return;
        }

        // Create a new Budget object
        Budget budget = new Budget();
        budget.setBudgetAmount(Double.parseDouble(budgetAmount));
        budget.setTripName(tripName);
        budget.setStartDate(startDateCalendar.getTimeInMillis());
        budget.setEndDate(endDateCalendar.getTimeInMillis());
        budget.setTripLocation(tripLocation);
        budget.setCategoryList(categoryList);

        // Push the budget object to the user's budgets node in the database
        userBudgetsRef.push().setValue(budget)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Show a toast message upon successful budget creation
                        Toast.makeText(this, "Budget saved successfully", Toast.LENGTH_SHORT).show();
                        // Clear all categories from SharedPreferences
                        deleteAllCategories();
                        // Navigate back to the main activity
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Show a toast message if there's an error saving the budget
                        Toast.makeText(this, "Failed to save budget", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Override method to handle the 'Up' button press in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        deleteAllCategories();
        return true;
    }

    // Override method to handle the 'Back' button press
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteAllCategories();
    }

    // Override method to handle the onResume lifecycle event
    @Override
    protected void onResume() {
        super.onResume();
        // Retrieve categories from SharedPreferences when the activity resumes
        retrieveCategoriesFromSharedPreferences();
    }

    // Method to delete all categories from SharedPreferences
    private void deleteAllCategories() {
        SharedPreferences sharedPreferences = getSharedPreferences("category_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    // Method to format a Calendar object into a String with the format "DD/MM/YYYY"
    @SuppressLint("DefaultLocale")
    private String formatDate(Calendar calendar) {
        return String.format("%02d/%02d/%d", calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
    }

}
