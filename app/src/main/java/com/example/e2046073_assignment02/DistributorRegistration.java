package com.example.e2046073_assignment02;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.e2046073_assignment02.databinding.ActivityDistributorRegistrationBinding;

public class DistributorRegistration extends AppCompatActivity {

    private EditText etDistributorId;
    private EditText etPassword;
    private Button btnRegister;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distributor_registration);

// Initialize views
        etDistributorId = findViewById(R.id.distributor_id_edit_text);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // Initialize the database helper
        dbHelper = new DatabaseHelper(this);

        // Set click listener for the register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String distributorId = etDistributorId.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (validateInput(distributorId, password)) {
                    // Insert the new distributor into the database
                    boolean isSuccess = dbHelper.insertDistributor(distributorId, password);
                    if (isSuccess) {
                        Toast.makeText(DistributorRegistration.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        finish(); // Finish the activity
                    } else {
                        Toast.makeText(DistributorRegistration.this, "Registration failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private boolean validateInput(String distributorId, String password) {
        if (TextUtils.isEmpty(distributorId)) {
            etDistributorId.setError("Enter a distributor ID");
            etDistributorId.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter a password");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }
}