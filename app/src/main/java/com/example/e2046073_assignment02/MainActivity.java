package com.example.e2046073_assignment02;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity {
    private EditText distributorIdEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        distributorIdEditText = findViewById(R.id.distributor_id_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        TextView signupText = findViewById(R.id.signupText);
        Button loginButton = findViewById(R.id.btnRegister);

        loginButton.setOnClickListener(v -> {
            String distributorId = distributorIdEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // Perform login verification and navigate to the main screen
            // if login is successful
            if (performLogin(distributorId, password)) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        signupText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DistributorRegistration.class);
            startActivity(intent);
        });
    }
    private boolean performLogin(String distributorId, String password) {
        DatabaseHelper dbHelper = new DatabaseHelper(this); // Replace 'context' with the appropriate context
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = { DatabaseHelper.COLUMN_DISTRIBUTOR_ID };
        String selection = DatabaseHelper.COLUMN_DISTRIBUTOR_ID + " = ? AND " + DatabaseHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = { distributorId, password };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_DISTRIBUTORS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean isAuthenticated = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return isAuthenticated;
    }

}