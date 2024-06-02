package com.example.tripwise.Activity.view.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tripwise.Activity.view.MainActivity;
import com.example.tripwise.R;
import com.example.tripwise.databinding.FragmentSignUpBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

// Fragment class for the sign-up functionality
public class SignUpFragment extends Fragment {

    // View binding for the sign-up fragment layout
    private FragmentSignUpBinding binding;
    // Firebase authentication instance
    private FirebaseAuth auth;
    // Google Sign-In client for handling Google authentication
    private GoogleSignInClient googleSignInClient;
    // Request code for Google Sign-In
    private static final int RC_SIGN_IN = 9001;
    // Shared preferences for storing user login state
    private SharedPreferences sharedPreferences;
    // Progress bar for indicating loading state
    private ProgressBar progressBar;
    // Firebase database reference for user data
    private DatabaseReference databaseReference;

    // Method called to create and return the fragment's view hierarchy
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        progressBar = binding.progressBar;

        // Configure Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Initialize Google Sign-In client
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        // Set up sign-up button click listener
        binding.signUpButton.setOnClickListener(view -> {
            if (validateInputs()) {
                signUpWithEmail();
            }
        });

        // Set up Google sign-up button click listener
        binding.signUpGoogleButton.setOnClickListener(view -> signUpWithGoogle());

        return binding.getRoot();
    }

    // Method to validate user input fields
    private boolean validateInputs() {
        boolean valid = true;

        String name = binding.nameTextInputEditText.getText().toString().trim();
        String email = binding.emailTextInputEditText.getText().toString().trim();
        String password = binding.passwordTextInputEditText.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordTextInputEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.nameTextInputLayout.setError("Name is required");
            valid = false;
        } else {
            binding.nameTextInputLayout.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            binding.emailTextInputLayout.setError("Email is required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailTextInputLayout.setError("Enter a valid email address");
            valid = false;
        } else {
            binding.emailTextInputLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordTextInputLayout.setError("Password is required");
            valid = false;
        } else if (password.length() < 6) {
            binding.passwordTextInputLayout.setError("Password should be at least 6 characters");
            valid = false;
        } else {
            binding.passwordTextInputLayout.setError(null);
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.confirmPasswordTextInputLayout.setError("Confirm your password");
            valid = false;
        } else if (!confirmPassword.equals(password)) {
            binding.confirmPasswordTextInputLayout.setError("Passwords do not match");
            valid = false;
        } else {
            binding.confirmPasswordTextInputLayout.setError(null);
        }

        return valid;
    }

    // Method to handle sign-up with email and password
    private void signUpWithEmail() {
        String name = binding.nameTextInputEditText.getText().toString().trim();
        String email = binding.emailTextInputEditText.getText().toString().trim();
        String password = binding.passwordTextInputEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        saveUserToDatabase(user, name);
                    } else {
                        Toast.makeText(getActivity(), "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to initiate Google sign-up process
    private void signUpWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Handle activity result for Google sign-in
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("SignUpFragment", "Google sign in failed", e);
            }
        }
    }

    // Authenticate with Firebase using Google sign-in credentials
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        saveUserToDatabase(user, account.getDisplayName());
                    } else {
                        Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Save user data to Firebase database
    private void saveUserToDatabase(FirebaseUser user, String name) {
        if (user != null) {
            String uid = user.getUid();
            String email = user.getEmail();

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("uid", uid);
            userMap.put("name", name);
            userMap.put("email", email);

            databaseReference.child(uid).setValue(userMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            saveLoginState(true);
                            updateUI(user);
                        } else {
                            Toast.makeText(getActivity(), "Failed to save user data.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Save the user's login state in shared preferences
    private void saveLoginState(boolean isLoggedIn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLogin", isLoggedIn);
        editor.apply();
    }

    // Update UI after successful sign-up
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        }
    }

    // Clean up binding when the view is destroyed
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
