package com.example.tripwise.Activity.view.expense.fragmentdialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.tripwise.Activity.view.expense.ExpenseTrackingActivity;
import com.example.tripwise.Activity.view.expense.item.CategoryExpense;
import com.example.tripwise.databinding.AddCategoryExpenseBinding;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class AddCategoryExpense extends DialogFragment {

    private int selectedColor = Color.WHITE;
    private AddCategoryExpenseBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        binding = AddCategoryExpenseBinding.inflate(getLayoutInflater());

        // Set up color picker dialog
        binding.btnPickColor.setOnClickListener(v -> ColorPickerDialogBuilder
                .with(requireContext())
                .setTitle("Choose color")
                .initialColor(selectedColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        Toast.makeText(getContext(), "onColorSelected: 0x" + Integer.toHexString(color), Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int color, Integer[] allColors) {
                        selectedColor = color;
                        binding.colorPreview.setBackgroundColor(selectedColor);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .build()
                .show());

        // Save the category
        binding.btnSave.setOnClickListener(v -> {
            String categoryName = binding.etCategoryName.getText().toString().trim();
            Double categoryAmount = Double.parseDouble(binding.etCategoryAmount.getText().toString());

            if (!categoryName.isEmpty() && !categoryAmount.equals(0.0)) {
                int categoryColor = selectedColor;

                // Save category to SharedPreferences
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("category_expense_prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                String categoriesJson = sharedPreferences.getString("categories", "[]");
                Gson gson = new Gson();
                Type type = new TypeToken<List<CategoryExpense>>(){}.getType();
                List<CategoryExpense> categories = gson.fromJson(categoriesJson, type);
                categories.add(new CategoryExpense(categoryName, categoryAmount, categoryColor));

                String updatedCategoriesJson = gson.toJson(categories);
                editor.putString("categories", updatedCategoriesJson);
                editor.apply();

                // Update category list in the activity
                ((ExpenseTrackingActivity) requireActivity()).retrieveCategoriesFromSharedPreferences();

                dismiss(); // Dismiss the dialog
            } else {
                Toast.makeText(getActivity(), "Category name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(binding.getRoot());

        return builder.create();
    }

    // Clean up the view binding
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
