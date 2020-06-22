package com.example.fyp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.R;
import com.example.fyp.model.User;
import com.example.fyp.service.ServerRequests;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LoginActivity extends AppCompatActivity {

    //    private static final String TAG = "LoginActivity";
    private static final int REQUEST_REGISTER = 0;

    private ServerRequests serverRequests;

    private EditText emailInput;
    private EditText passInput;

    private Button loginButton;
    private TextView registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_login);

        serverRequests = ServerRequests.getInstance();

        emailInput = findViewById(R.id.emailText);
        passInput = findViewById(R.id.passText);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.link_register);



        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginClick();
            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivityForResult(intent, REQUEST_REGISTER);
            }
        });
    }

    public void loginClick() {
        if (validate()) {
            Toast.makeText(LoginActivity.this, "Logging in...", Toast.LENGTH_LONG).show();
            User user = serverRequests.userAuth(emailInput.getText().toString(), passInput.getText().toString());
            if (user != null) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("surname", user.getSurname());
                intent.putExtra("email", user.getEmail());
                intent.putExtra("name", user.getName());
                intent.putExtra("pass", passInput.getText().toString());
                startActivity(intent);
            } else {
                emailInput.setError("Invalid username or password");
            }
        }

    }

    public boolean validate() {
        boolean valid = true;
        String emailString = emailInput.getText().toString();
        String passwordString = passInput.getText().toString();

        Log.d("emailString", emailString);
        Log.d("passwordString", passwordString);

        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String text = date.format(formatter);

        if (emailString.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {
            emailInput.setError("enter a valid email address");
            valid = false;
        } else {
            emailInput.setError(null);
        }

        if (passwordString.isEmpty()) {// || passwordString.length() < 4) {
            passInput.setError("enter password");
            valid = false;
        } else {
            passInput.setError(null);
        }

        return valid;
    }

}
