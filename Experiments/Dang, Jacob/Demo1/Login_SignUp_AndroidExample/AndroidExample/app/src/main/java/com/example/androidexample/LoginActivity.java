package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable
    private Button loginButton;         // define login button variable
    private Button signupButton;        // define signup button variable

    private Button forgotPasswordButton;

    private boolean username_flag = false;
    private boolean password_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);            // link to Login activity XML

        /* initialize UI elements */
        usernameEditText = findViewById(R.id.login_username_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);    // link to login button in the Login activity XML
        signupButton = findViewById(R.id.login_signup_btn);  // link to signup button in the Login activity XML
        forgotPasswordButton = findViewById(R.id.forgotBtn); // link to forgot password button in the Login activity XML

        //Test for registered users
        List<String> Registered_User = new ArrayList<>();
        Registered_User.add("Jacob");
        Registered_User.add("Jordan");
        Registered_User.add("Nick");
        Registered_User.add("Bao");

        /* click listener on login button pressed */
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* grab strings from user inputs */
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                /* when login button is pressed, use intent to switch to Login Activity */
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("USERNAME", username);  // key-value to pass to the MainActivity
                intent.putExtra("PASSWORD", password);  // key-value to pass to the MainActivity

                // Runs checks and requirements on usernames and passwords
                check_username(username, Registered_User);
                check_password(password);


                if(!username_flag && !password_flag)
                {
                    startActivity(intent);  // go to MainActivity with the key-value data
                }
            }
        });

        /* click listener on signup button pressed */
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when signup button is pressed, use intent to switch to Signup Activity */
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);  // go to SignupActivity
            }
        });

        forgotPasswordButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });


    }

    private void check_username(String username, List<String> Registered_User)
    {
        //If field is empty, reminds the user to enter a username
        if(username.isEmpty())
        {
            Toast.makeText(getApplicationContext(), "Please enter a username", Toast.LENGTH_LONG).show();
            username_flag = true;
        }

        //Username cannot be longer than 10 characters
        else if(username.length() > 10)
        {
            Toast.makeText(getApplicationContext(), "Username is too long", Toast.LENGTH_LONG).show();
            username_flag = true;
        }

        else
        {
            username_flag = false;
        }

        //Checks to make sure username is not already in system
        for(int i = 0; i < Registered_User.size(); i++)
        {

            if(Registered_User.get(i).toLowerCase().equals(username.toLowerCase()))
            {
                Toast.makeText(getApplicationContext(), "Username already taken", Toast.LENGTH_LONG).show();
                username_flag = true;
                break;
            }
            else
            {
                username_flag = false;
            }
        }
    }

    private void check_password(String password)
    {
        //Password must be greater than 5 characters and contain "!"
        if(!password.contains("!"))
        {
            Toast.makeText(getApplicationContext(), "Password MUST INCLUDE !", Toast.LENGTH_LONG).show();
            password_flag = true;

        }
        //If field is empty or password has 5 or less characters, tells user to enter a longer password
        else if(password.length() < 6 || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Password is too short", Toast.LENGTH_LONG).show();
            password_flag = true;


        }
        else
        {
            password_flag = false;
        }


    }
}