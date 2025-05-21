package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private TextView messageText;     // define message textview variable
    private Button counterButton;     // define counter button variable

    @Override
    protected void onCreate(Bundle savedInstanceState) { // TODO: this is the main wrapper activity that works as the start screen.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);             // link to Main activity XML

        /* initialize UI elements */
        messageText = findViewById(R.id.main_msg_txt);      // link to message textview in the Main activity XML
        counterButton = findViewById(R.id.main_counter_btn);// link to counter button in the Main activity XML

        // TODO: these two elements hold the message and the button that starts the actual counter app.

        /* extract data passed into this activity from another activity */
        Bundle extras = getIntent().getExtras(); // TODO: this line obtains data from the counter activity, passing on the number counted when the activity was running.
        if(extras == null) {
            messageText.setText("Intent Example"); // if there was no passed data, default message.
        } else {
            String number = extras.getString("NUM");  // this will come from LoginActivity
            messageText.setText("The number was " + number); // if there was a value, prints out the value of the last state.
        }

        /* click listener on counter button pressed */
        counterButton.setOnClickListener(new View.OnClickListener() { // TODO: listener like in java swing buttons.
            @Override
            public void onClick(View v) { // TODO: when the button is clicked (you have to override the click listener object function)...

                /* when counter button is pressed, use intent to switch to Counter Activity */
                Intent intent = new Intent(MainActivity.this, CounterActivity.class); // TODO: you switch to the other activity.
                startActivity(intent); // TODO: and then you render the activity.
            }
        });
    }
}