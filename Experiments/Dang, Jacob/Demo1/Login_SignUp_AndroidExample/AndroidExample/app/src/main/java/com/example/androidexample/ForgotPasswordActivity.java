package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;  // define email edittext variable

    private Button emailBtn;         // define login button variable

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);            // link to forgotpassword activity XML

        emailBtn.findViewById(R.id.forgotBtn);

        // click listener on login button pressed
        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

}
