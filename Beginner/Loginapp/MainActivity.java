package com.example.loginpage;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText usernameEditText, passwordEditText;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);

        loginButton.setOnClickListener(view -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Basic validation for empty fields
            if (TextUtils.isEmpty(username)) {
                usernameEditText.setError("Please enter username");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Please enter password");
                return;
            }

            // For simplicity, allow any non-empty credentials
            Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

            // Redirect to WelcomeActivity
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }
}
