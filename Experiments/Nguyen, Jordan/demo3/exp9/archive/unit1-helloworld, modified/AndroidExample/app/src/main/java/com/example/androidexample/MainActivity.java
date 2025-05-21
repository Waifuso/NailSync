package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private TextView messageText;   // define message textview variable
    // in the line above, a private variable of object TextViw was made called messageText which will contain the message to be printed to the emulator.

    @Override
    protected void onCreate(Bundle savedInstanceState) { // the onCreate() method essentially works as the main() method, in swing this would be your render method.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);             // link to Main activity XML
        // this connects the rendered program to the app layout manager.

        /* initialize UI elements */
        messageText = findViewById(R.id.main_msg_txt);      // link to message textview in the Main activity XML
        // the message is linked to the activity_main.

        messageText.setText("changed message test.");
        // the message is then set.
    }
}