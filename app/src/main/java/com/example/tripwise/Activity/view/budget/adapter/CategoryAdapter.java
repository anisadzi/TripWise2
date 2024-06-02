package com.example.tripwise.Activity.view.budget.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tripwise.Activity.view.budget.item.Category;
import com.example.tripwise.databinding.CategoryItemBinding;
import com.google.gson.Gson;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private Context context;
    private List<Category> categoryList;

    // Constructor
    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    // Method to set data in the adapter
    public void setData(List<Category> categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged();
    }

    // Method to create View Holder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        CategoryItemBinding binding = CategoryItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    // Method to bind data to View Holder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.binding.tvCategoryName.setText(category.getName());
        holder.binding.viewColor.setTextColor(category.getColor());

        // Set onClickListener for delete button
        holder.binding.ivDelete.setOnClickListener(v -> {
            removeItem(position);
        });
    }

    // Method to get item count
    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    // Method to remove item from the list
    public void removeItem(int position) {
        categoryList.remove(position);
        notifyDataSetChanged();

        // Update shared preferences after removing an item
        updateSharedPreferences();
    }

    // Method to update shared preferences with the updated category list
    private void updateSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("category_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String categoriesJson = gson.toJson(categoryList);
        editor.putString("categories", categoriesJson);
        editor.apply();
    }

    // View Holder class
    static class ViewHolder extends RecyclerView.ViewHolder {
        CategoryItemBinding binding;

        ViewHolder(CategoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
