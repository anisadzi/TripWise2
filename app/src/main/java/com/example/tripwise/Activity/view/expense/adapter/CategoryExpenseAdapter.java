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

    public CategoryExpenseAdapter(Context context, List<CategoryExpense> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }
    public void setData(List<CategoryExpense> categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryExpenseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        CategoryExpenseItemBinding binding = CategoryExpenseItemBinding.inflate(inflater, parent, false);
        return new CategoryExpenseAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryExpenseAdapter.ViewHolder holder, int position) {
        CategoryExpense category = categoryList.get(position);
        holder.binding.tvCategoryName.setText(category.getName());
        holder.binding.viewColor.setTextColor(category.getColor());

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CategoryExpenseItemBinding binding;

        ViewHolder(CategoryExpenseItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
