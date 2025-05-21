package com.example.androidexample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Simple activity that shows scrollable reward program images
 */
public class LearnMoreActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnStartEarning;

    private long userID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_more);

        // Get userID from intent if available
        if (getIntent().hasExtra("userID")) {
            userID = getIntent().getLongExtra("userID", -1);
        }

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        btnStartEarning = findViewById(R.id.btnStartEarning);

        // Set click listeners
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnStartEarning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LearnMoreActivity.this, ApplicationActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });
    }
}