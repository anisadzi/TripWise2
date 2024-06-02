package com.example.tripwise.Activity.view.expense;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tripwise.Activity.view.MainActivity;
import com.example.tripwise.Activity.view.expense.adapter.CategoryAmountExpenseAdapter;
import com.example.tripwise.Activity.view.expense.adapter.CategoryExpenseAdapter;
import com.example.tripwise.Activity.view.expense.fragmentdialog.AddCategoryExpense;
import com.example.tripwise.Activity.view.expense.item.CategoryExpense;
import com.example.tripwise.Activity.view.expense.item.Expense;
import com.example.tripwise.R;
import com.example.tripwise.databinding.ActivityExpenseTrackingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExpenseTrackingActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, CategoryAmountExpenseAdapter.OnCategoryExpenseChangedListener {

    private ActivityExpenseTrackingBinding binding;
    private CategoryExpenseAdapter categoryExpenseAdapter;
    private CategoryAmountExpenseAdapter categoryAmountExpenseAdapter;
    private List<CategoryExpense> categoryList;
    private Calendar calendar;
    private double totalExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityExpenseTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Expense Tracking");

        calendar = Calendar.getInstance();

        binding.rvCategory.setLayoutManager(new GridLayoutManager(this, 2));
        categoryExpenseAdapter = new CategoryExpenseAdapter(this, categoryList);
        binding.rvCategory.setAdapter(categoryExpenseAdapter);

        binding.rvCategoryAmount.setLayoutManager(new LinearLayoutManager(this));
        categoryAmountExpenseAdapter = new CategoryAmountExpenseAdapter(this, categoryList, this);
        binding.rvCategoryAmount.setAdapter(categoryAmountExpenseAdapter);

        binding.fabAdd.setOnClickListener(view -> openCategoryDialog());
        binding.txtDatePicker.setOnClickListener(view -> openDatePickerDialog());
        binding.txtDatePicker.setText(formatDate(Calendar.getInstance()));
        binding.btnSave.setOnClickListener(view -> saveExpense());
        retrieveCategoriesFromSharedPreferences();
    }

    private void openCategoryDialog() {
        AddCategoryExpense dialogFragment = new AddCategoryExpense();
        dialogFragment.show(getSupportFragmentManager(), "addCategoryExpenseDialog");
    }

    public void retrieveCategoriesFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("category_expense_prefs", Context.MODE_PRIVATE);
        String categoriesJson = sharedPreferences.getString("categories", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<CategoryExpense>>(){}.getType();
        categoryList = gson.fromJson(categoriesJson, type);
        if (categoryList == null) {
            categoryList = new ArrayList<>();
        }
        totalExpense = 0;
        for (CategoryExpense category : categoryList) {
            totalExpense += category.getAmount();
        }

        categoryExpenseAdapter.setData(categoryList);
        categoryAmountExpenseAdapter.setData(categoryList);
        categoryAmountExpenseAdapter.notifyDataSetChanged();
        categoryExpenseAdapter.notifyDataSetChanged();

        binding.txtTotal.setText(String.valueOf(totalExpense));
    }

    private void openDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteAllCategories();
    }

    private void deleteAllCategories() {
        SharedPreferences sharedPreferences = getSharedPreferences("category_expense_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    @SuppressLint("DefaultLocale")
    private String formatDate(Calendar calendar) {
        return String.format("%02d/%02d/%d", calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        binding.txtDatePicker.setText(formatDate(calendar));
    }

    private void saveExpense(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference userExpenseRef = FirebaseDatabase.getInstance().getReference("expense").child(userId);

        long date = calendar.getTimeInMillis();
        double total = totalExpense;

        if (categoryList.isEmpty()){
            Toast.makeText(this, "Please add at least one category", Toast.LENGTH_LONG).show();
            return;
        }
        Expense expense = new Expense();
        expense.setDate(date);
        expense.setTotal(total);
        expense.setCategoryExpenses(categoryList);
        userExpenseRef.push().setValue(expense).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show();
                deleteAllCategories();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCategoryExpenseChanged(double totalExpense) {
        this.totalExpense = totalExpense;
        binding.txtTotal.setText(String.valueOf(totalExpense));
    }
}
