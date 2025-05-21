package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class GoogleSUActivity extends AppCompatActivity
{



    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_google_su);

        // Initializes UI components
        Button goBack = findViewById(R.id.back_button);

        // takes user back to sigh up page
        goBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)

            {
                finish();
            }

        });


    }
}
