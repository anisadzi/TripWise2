package com.example.tripwise.Activity.view.expense.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripwise.Activity.view.expense.item.CategoryExpense;
import com.example.tripwise.Activity.view.expense.item.Expense;
import com.example.tripwise.R;
import com.example.tripwise.databinding.ListExpenseItemBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseListAdapter extends RecyclerView.Adapter<ExpenseListAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;
    private Context context;

    // Constructor to initialize the adapter with data
    public ExpenseListAdapter(List<Expense> expenseList, Context context) {
        this.expenseList = expenseList;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListExpenseItemBinding binding = ListExpenseItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ExpenseViewHolder(binding);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.bind(expense);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    // Set data to the adapter
    public void setExpenseList(List<Expense> expenseList) {
        this.expenseList = expenseList;
        notifyDataSetChanged();
    }

    // ViewHolder class to hold references to the views
    class ExpenseViewHolder extends RecyclerView.ViewHolder {

        private ListExpenseItemBinding binding;

        public ExpenseViewHolder(@NonNull ListExpenseItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Bind data to the ViewHolder
        public void bind(Expense expense) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String date = dateFormat.format(new Date(expense.getDate()));
            binding.dateExpense.setText("Date: " + date);
            binding.amountExpense.setText(String.valueOf("Total Amount: Rp " + expense.getTotal()));

            // Set up RecyclerView for category expenses
            CategoryExpenseAdapter categoryExpenseAdapter = new CategoryExpenseAdapter(context, expense.getCategoryExpenses());
            binding.rvCategoryExpense.setLayoutManager(new GridLayoutManager(context, 2));
            binding.rvCategoryExpense.setAdapter(categoryExpenseAdapter);
            binding.rvCategoryExpense.setVisibility(View.GONE);

            // Toggle visibility of category expenses RecyclerView on category click
            binding.categoryExpense.setOnClickListener(v -> {
                if (binding.rvCategoryExpense.getVisibility() == View.VISIBLE) {
                    binding.rvCategoryExpense.setVisibility(View.GONE);
                    binding.categoryExpense.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_dropdown, 0);
                } else {
                    binding.rvCategoryExpense.setVisibility(View.VISIBLE);
                    binding.categoryExpense.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_drowup, 0);
                }
            });
        }
    }
}
