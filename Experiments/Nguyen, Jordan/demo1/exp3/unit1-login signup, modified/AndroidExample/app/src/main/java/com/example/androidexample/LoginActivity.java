package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.String;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable
    private Button loginButton;         // define login button variable
    private Button signupButton;        // define signup button variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);            // link to Login activity XML

        /* initialize UI elements */
        usernameEditText = findViewById(R.id.login_username_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);    // link to login button in the Login activity XML
        signupButton = findViewById(R.id.login_signup_btn);  // link to signup button in the Login activity XML


        /* click listener on login button pressed */
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = false, kill = false;

                /* grab strings from user inputs */
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                while (!valid && !kill) {
                    if (MainActivity.userInfo.size() != 0) {
                        for (ArrayList<String> user : MainActivity.userInfo) {
                            if ((user.get(0).compareTo(username) == 0) && (user.get(1).compareTo(password) == 0)) {
                                valid = true;
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "User credentials were incorrect.", Toast.LENGTH_LONG).show();
                                kill = true;
                            }
                        }
                    }
                    else {
                        kill = true;
                    }
                }

                if (valid) {
                    /* when login button is pressed, use intent to switch to Login Activity */
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USERNAME", username);  // key-value to pass to the MainActivity
                    intent.putExtra("PASSWORD", password);  // key-value to pass to the MainActivity

                    finish();
                    startActivity(intent);  // go to MainActivity with the key-value data
                }
                if (kill && MainActivity.userInfo.size() == 0) {
                    Toast.makeText(getApplicationContext(), "There are no active users.", Toast.LENGTH_LONG).show();
                }
            }
        });

        /* click listener on signup button pressed */
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* when signup button is pressed, use intent to switch to Signup Activity */
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);

                // TODO: added these lines of code that transfer over what was written in the username field to the sign-in screen.
                String cached = usernameEditText.getText().toString();
                intent.putExtra("CACHED", cached);
                finish();
                startActivity(intent);  // go to SignupActivity
            }
        });
    }
}