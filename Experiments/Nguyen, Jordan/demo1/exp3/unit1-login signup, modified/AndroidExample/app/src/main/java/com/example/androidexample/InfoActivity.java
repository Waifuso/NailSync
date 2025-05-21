package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

// this entire activity was made from scratch using the provided activities as reference.
public class InfoActivity extends AppCompatActivity {
    private TextView usernameText, passwordText, dateText; // defining text variable.

    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // initializing the buttons and linking them to their xml identifiers
        backButton = findViewById(R.id.info_back_btn);

        usernameText = findViewById(R.id.info_username_inherit);
        passwordText = findViewById(R.id.info_password_inherit);
        dateText = findViewById(R.id.info_creation_inherit);

        Bundle information = getIntent().getExtras();
        if (information != null) {
            usernameText.setText(information.getString("USERNAME"));
            passwordText.setText(information.getString("PASSWORD"));
            dateText.setText(information.getString("DATE"));
        }
        else {
            usernameText.setText("none");
            passwordText.setText("none");
            dateText.setText(information.getString("DATE"));
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when back button is pressed, use finish to return to main */
                finish();
            }
        });


    }
}