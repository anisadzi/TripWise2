package com.example.tripwise.Activity.view.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.tripwise.R;
import com.example.tripwise.databinding.ActivityEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding; // View binding for the layout
    private DatabaseReference mDatabase; // Reference to Firebase Realtime Database
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private FirebaseUser currentUser; // Currently authenticated user
    private StorageReference mStorageRef; // Reference to Firebase Storage

    private ActivityResultLauncher<Intent> selectPictureLauncher; // Launcher for selecting picture from gallery

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Set up the toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");

        // Register a launcher for selecting an image from the gallery
        selectPictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                Uri selectedImageUri = data != null ? data.getData() : null;
                if (selectedImageUri != null) {
                    try {
                        // Get the selected image as a Bitmap and display it
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        binding.profileImage.setImageBitmap(bitmap);
                        // Save the selected photo to Firebase
                        savePhotoToFirebase(selectedImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Set click listeners for changing and updating the profile
        binding.changeProfileButton.setOnClickListener(view -> openGallery());
        binding.updateProfileButton.setOnClickListener(view -> updateProfile());

        // Load the current account information
        getAccountInfo();
    }

    // Load the current user's account information from Firebase
    private void getAccountInfo() {
        String userId = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String photoUrl = snapshot.child("photoUrl").getValue(String.class);

                    // Set the retrieved information to the corresponding views
                    binding.nameTextInputEditText.setText(name);
                    binding.phoneTextInputEditText.setText(phoneNumber);
                    binding.emailTextInputEditText.setText(email);

                    // Load the profile image using Picasso
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Picasso.get()
                                .load(photoUrl)
                                .placeholder(R.drawable.ic_account)
                                .into(binding.profileImage);
                    }
                }
            } else {
                Toast.makeText(this, "Failed to fetch account information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Update the user's profile information in Firebase
    private void updateProfile() {
        String name = binding.nameTextInputEditText.getText().toString().trim();
        String phoneNumber = binding.phoneTextInputEditText.getText().toString().trim();

        String userId = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> update = new HashMap<>();
        update.put("name", name);
        update.put("phoneNumber", phoneNumber);

        // Update the user information in Firebase
        mDatabase.child("users").child(userId).updateChildren(update)
                .addOnSuccessListener(aVoid -> Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show());
    }

    // Open the gallery to select a new profile picture
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        selectPictureLauncher.launch(intent);
    }

    // Save the selected photo to Firebase Storage and update the user's profile photo URL
    private void savePhotoToFirebase(Uri selectedImageUri) {
        String userId = currentUser.getUid();
        StorageReference photoRef = mStorageRef.child("profile_images").child(userId + ".jpg");

        // Upload the selected photo to Firebase Storage
        photoRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String photoUrl = uri.toString();
                    mDatabase.child("users").child(userId).child("photoUrl").setValue(photoUrl)
                            .addOnSuccessListener(aVoid -> Toast.makeText(EditProfileActivity.this, "Photo updated successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Failed to update photo", Toast.LENGTH_SHORT).show());
                }))
                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Failed to upload photo", Toast.LENGTH_SHORT).show());
    }

    // Handle back navigation
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
