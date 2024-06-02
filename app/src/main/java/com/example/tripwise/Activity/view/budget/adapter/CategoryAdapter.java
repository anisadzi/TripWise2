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

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }
    public void setData(List<Category> categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        CategoryItemBinding binding = CategoryItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.binding.tvCategoryName.setText(category.getName());
        holder.binding.viewColor.setTextColor(category.getColor());

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
    }

    private void updateSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("category_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String categoriesJson = gson.toJson(categoryList);
        editor.putString("categories", categoriesJson);
        editor.apply();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CategoryItemBinding binding;

        ViewHolder(CategoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
