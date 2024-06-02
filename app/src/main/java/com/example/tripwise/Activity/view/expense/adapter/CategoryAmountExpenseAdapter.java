package com.example.tripwise.Activity.view.expense.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripwise.Activity.view.expense.item.CategoryExpense;
import com.example.tripwise.databinding.CategoryAmountItemBinding;
import com.google.gson.Gson;

import java.util.List;

public class CategoryAmountExpenseAdapter extends RecyclerView.Adapter<CategoryAmountExpenseAdapter.ViewHolder> {
    private Context context;
    private List<CategoryExpense> categoryList;
    private OnCategoryExpenseChangedListener onCategoryExpenseChangedListener;

    public interface OnCategoryExpenseChangedListener {
        void onCategoryExpenseChanged(double totalExpense);
    }

    public CategoryAmountExpenseAdapter(Context context, List<CategoryExpense> categoryList, OnCategoryExpenseChangedListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.onCategoryExpenseChangedListener = listener;
    }

    public void setData(List<CategoryExpense> categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged();
        notifyTotalExpenseChanged();
    }

    @NonNull
    @Override
    public CategoryAmountExpenseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        CategoryAmountItemBinding binding = CategoryAmountItemBinding.inflate(inflater, parent, false);
        return new CategoryAmountExpenseAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAmountExpenseAdapter.ViewHolder holder, int position) {
        CategoryExpense category = categoryList.get(position);
        holder.binding.tvCategoryAmount.setText(category.getAmount().toString());
        holder.binding.viewColor.setCardBackgroundColor(category.getColor());

        holder.binding.ivDelete.setOnClickListener(v -> {
            removeItem(position);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void removeItem(int position) {
        categoryList.remove(position);
        notifyDataSetChanged();
        updateSharedPreferences();
        notifyTotalExpenseChanged();
    }

    private void updateSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("category_expense_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String categoriesJson = gson.toJson(categoryList);
        editor.putString("categories", categoriesJson);
        editor.apply();
    }

    private void notifyTotalExpenseChanged() {
        if (onCategoryExpenseChangedListener != null) {
            double totalExpense = 0;
            for (CategoryExpense category : categoryList) {
                totalExpense += category.getAmount();
            }
            onCategoryExpenseChangedListener.onCategoryExpenseChanged(totalExpense);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CategoryAmountItemBinding binding;

        ViewHolder(CategoryAmountItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
