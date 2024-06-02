package com.example.tripwise.Activity.view.budget.fragmentdialog;

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

import com.example.tripwise.Activity.view.budget.item.Category;
import com.example.tripwise.Activity.view.budget.BudgetActivity;
import com.example.tripwise.databinding.FragmentAddCategoryBinding;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class AddCategoryFragment extends DialogFragment {

    private int selectedColor = Color.WHITE;
    private FragmentAddCategoryBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate the layout for this fragment
        binding = FragmentAddCategoryBinding.inflate(getLayoutInflater());

        // Set onClickListener for the color picker button
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

        // Set onClickListener for the save button
        binding.btnSave.setOnClickListener(v -> {
            String categoryName = binding.etCategoryName.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                int categoryColor = selectedColor;

                // Retrieve shared preferences
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("category_prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Retrieve categories list from shared preferences
                String categoriesJson = sharedPreferences.getString("categories", "[]");
                Gson gson = new Gson();
                Type type = new TypeToken<List<Category>>(){}.getType();
                List<Category> categories = gson.fromJson(categoriesJson, type);

                // Add new category to the list
                categories.add(new Category(categoryName, categoryColor));

                // Convert categories list back to JSON
                String updatedCategoriesJson = gson.toJson(categories);

                // Update shared preferences with the updated categories list
                editor.putString("categories", updatedCategoriesJson);
                editor.apply();

                // Retrieve and update categories in BudgetActivity
                ((BudgetActivity) requireActivity()).retrieveCategoriesFromSharedPreferences();

                // Dismiss the dialog fragment
                dismiss();
            } else {
                Toast.makeText(getActivity(), "Category name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Set the view of the dialog fragment
        builder.setView(binding.getRoot());

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
