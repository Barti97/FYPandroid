package com.example.fyp.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.R;
import com.example.fyp.service.ServerRequests;
import com.example.fyp.model.User;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private EditText name;
    private EditText surname;
    private EditText dateOfBirth;
    private EditText phoneNumber;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private Button registerBtn;
    private TextView loginLink;

    private ServerRequests serverRequests;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_register);

        serverRequests = ServerRequests.getInstance();

        final Calendar myCalendar = Calendar.getInstance();

        name = findViewById(R.id.input_name);
        surname = findViewById(R.id.input_surname);
        dateOfBirth = findViewById(R.id.dateOfBirth);
        phoneNumber = findViewById(R.id.phone_number);
        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        confirmPassword = findViewById(R.id.input_passwordconfirm);
        registerBtn = findViewById(R.id.registerBtn);
        loginLink = findViewById(R.id.link_login);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(myCalendar);
            }

        };

        dateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(RegisterActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel(Calendar myCalendar) {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);

        dateOfBirth.setText(sdf.format(myCalendar.getTime()));
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        registerBtn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this, R.style.Theme_AppCompat_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        // TODO: Implement your own signup logic here.

        String nameString = name.getText().toString();
        String surnameString = surname.getText().toString();
        String DoBString = dateOfBirth.getText().toString();
        String phoneNoString = phoneNumber.getText().toString();
        String emailString = email.getText().toString();
        String passwordString = password.getText().toString();
        String confirmPasswordString = confirmPassword.getText().toString();


        User user = new User(emailString, passwordString, nameString, surnameString, Integer.parseInt(phoneNoString), LocalDate.parse(DoBString, DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        serverRequests.registerUser(user);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignupSuccess();
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    public void onSignupSuccess() {
        registerBtn.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        registerBtn.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String nameString = name.getText().toString();
        String surnameString = surname.getText().toString();
        String DoBString = dateOfBirth.getText().toString();
        String phoneNoString = phoneNumber.getText().toString();
        String emailString = email.getText().toString();
        String passwordString = password.getText().toString();
        String confirmPasswordString = confirmPassword.getText().toString();

        Log.d("nameString", nameString);
        Log.d("surnameString", surnameString);
        Log.d("DoBString", DoBString);
        Log.d("phoneNoString", phoneNoString);
        Log.d("emailString", emailString);
        Log.d("passwordString", passwordString);
        Log.d("confirmPasswordString", confirmPasswordString);

        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String text = date.format(formatter);

        Log.d("date", text);
        Log.d("dob", DoBString);
        Log.d("localdate", LocalDate.parse(DoBString, DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());

        if (nameString.isEmpty() || nameString.length() < 3) {
            name.setError("at least 3 characters");
            valid = false;
        } else {
            name.setError(null);
        }

        if (surnameString.isEmpty() || surnameString.length() < 3) {
            surname.setError("at least 3 characters");
            valid = false;
        } else {
            surname.setError(null);
        }

        if (phoneNoString.isEmpty() || phoneNoString.length() != 10) {
            phoneNumber.setError("must contain 10 digits");
            valid = false;
        } else {
            phoneNumber.setError(null);
        }

        if (emailString.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {
            email.setError("enter a valid email address");
            valid = false;
        } else {
            email.setError(null);
        }

        if (passwordString.isEmpty() || passwordString.length() < 4) {
            password.setError("at least 4 alphanumeric characters");
            valid = false;
        } else {
            password.setError(null);
        }

        if (confirmPasswordString.isEmpty() || confirmPasswordString.length() < 4 || confirmPasswordString.length() > 20) {
            confirmPassword.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            confirmPassword.setError(null);
        }

        if (!confirmPasswordString.equals(passwordString)) {
            confirmPassword.setError("passwords must match");
            valid = false;
        } else {
            confirmPassword.setError(null);
        }

        return valid;
    }
}
