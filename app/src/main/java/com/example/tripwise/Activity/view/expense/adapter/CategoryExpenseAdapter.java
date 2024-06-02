package com.example.tripwise.Activity.view.expense.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripwise.Activity.view.expense.item.CategoryExpense;
import com.example.tripwise.databinding.CategoryExpenseItemBinding;

import java.util.List;

public class CategoryExpenseAdapter extends RecyclerView.Adapter<CategoryExpenseAdapter.ViewHolder> {
    private Context context;
    private List<CategoryExpense> categoryList;

    // Constructor to initialize the adapter with data
    public CategoryExpenseAdapter(Context context, List<CategoryExpense> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    // Method to set data to the adapter
    public void setData(List<CategoryExpense> categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged();
    }

    // Inflates the layout when creating ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        CategoryExpenseItemBinding binding = CategoryExpenseItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    // Binds data to the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryExpense category = categoryList.get(position);
        holder.binding.tvCategoryName.setText(category.getName());
        holder.binding.viewColor.setTextColor(category.getColor());
    }

    // Returns the total number of items in the list
    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    // ViewHolder class to hold references to the views
    static class ViewHolder extends RecyclerView.ViewHolder {
        CategoryExpenseItemBinding binding;

        ViewHolder(CategoryExpenseItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
