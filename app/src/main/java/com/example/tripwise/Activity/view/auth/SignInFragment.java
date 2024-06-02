package com.example.tripwise.Activity.view.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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
import com.example.tripwise.databinding.FragmentSignInBinding;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignInFragment extends Fragment {

    private FragmentSignInBinding binding;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance();
        // Get a reference to the users' database
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize progress bar
        progressBar = binding.progressBar;

        // Configure Google sign-in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Initialize SharedPreferences for saving login state
        sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        // Set click listeners for sign-in button, Google sign-in button, and forgot password text view
        binding.signInButton.setOnClickListener(view -> {
            if (validateInputs()) {
                signInWithEmail();
            }
        });
        binding.googleSignInButton.setOnClickListener(view -> signInWithGoogle());
        binding.forgotPasswordTextView.setOnClickListener(view -> openForgotPasswordActivity());

        return binding.getRoot();
    }

    // Method to validate user inputs (email and password)
    private boolean validateInputs() {
        boolean valid = true;

        String email = binding.emailTextInputEditText.getText().toString().trim();
        String password = binding.passwordTextInputEditText.getText().toString().trim();

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
        } else {
            binding.passwordTextInputLayout.setError(null);
        }

        return valid;
    }

    // Method to sign in with email and password
    private void signInWithEmail() {
        String email = binding.emailTextInputEditText.getText().toString().trim();
        String password = binding.passwordTextInputEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        saveLoginState(true);
                        updateUI(user);
                    } else {
                        Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    // Method to initiate Google sign-in flow
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("SignInFragment", "Google sign in failed", e);
            }
        }
    }

    // Method to authenticate with Firebase using Google credentials
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        checkAndSaveUserToDatabase(user, account.getDisplayName());
                    } else {
                        Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to save login state
    private void saveLoginState(boolean isLoggedIn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLogin", isLoggedIn);
        editor.apply();
    }

    // Method to update UI after successful sign-in
    private void updateUI(FirebaseUser user) {

        if (user != null) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        }
    }

    // Method to open ForgotPasswordActivity
    private void openForgotPasswordActivity() {
        Intent intent = new Intent(getActivity(), ForgotPasswordActivity.class);
        startActivity(intent);
    }

    // Method to check if the user exists in the database and save the user data
    private void checkAndSaveUserToDatabase(FirebaseUser user, String name) {
        if (user != null) {
            String uid = user.getUid();
            String email = user.getEmail();

            databaseReference.child(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (!snapshot.exists()) {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", uid);
                        userMap.put("name", name);
                        userMap.put("email", email);

                        databaseReference.child(uid).setValue(userMap)
                                .addOnCompleteListener(saveTask -> {
                                    if (saveTask.isSuccessful()) {
                                        saveLoginState(true);
                                        updateUI(user);
                                    } else {
                                        Toast.makeText(getActivity(), "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        saveLoginState(true);
                        updateUI(user);
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to check user data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Lifecycle method called when the view is destroyed
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
