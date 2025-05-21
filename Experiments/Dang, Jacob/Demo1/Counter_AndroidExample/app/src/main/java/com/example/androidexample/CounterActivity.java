package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CounterActivity extends AppCompatActivity {

    private TextView numberTxt; // define number textview variable
    private Button increaseBtn; // define increase button variable
    private Button decreaseBtn; // define decrease button variable
    private Button backBtn;     // define back button variable

    private Button increaseBtn1;// define second increase button varaible
    private Button decreaseBtn1;// define second decrease button variable

    private TextView numberTxt1; // define second number textview variable

    private int counter = 0;    // counter variable
    private int counter1 = 0;   //second counter variable


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        /* initialize UI elements */
        numberTxt = findViewById(R.id.number);
        numberTxt1 = findViewById(R.id.number1);

        increaseBtn = findViewById(R.id.counter_increase_btn);
        increaseBtn1 = findViewById(R.id.counter_increase_btn1);

        decreaseBtn = findViewById(R.id.counter_decrease_btn);
        decreaseBtn1 = findViewById(R.id.counter_decrease_btn1);

        backBtn = findViewById(R.id.counter_back_btn);


        /* when increase btn is pressed, counter++, reset number textview */

        increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Counter will only display numbers 0 - 21
                if(counter < 22 && counter > -1)
                {
                    numberTxt.setText(String.valueOf(counter++));
                }
                else
                {
                    counter = 0;
                    numberTxt.setText(String.valueOf(counter));
                }
            }
        });

        increaseBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Counter will only display numbers 0 - 21
                if(counter1 < 22 && counter1 > -1)
                {
                    numberTxt1.setText(String.valueOf(counter1++));
                }
                else
                {
                    counter1 = 0;
                    numberTxt1.setText(String.valueOf(counter1));
                }
            }
        });

        /* when decrease btn is pressed, counter--, reset number textview */
        decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Counter will decrease as long as number is positive
                if((counter -1) != -1)
                {
                    numberTxt.setText(String.valueOf(counter--));
                }
                else
                {
                    counter = 0;
                    numberTxt.setText(String.valueOf(counter));
                }
            }
        });
        decreaseBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //Counter will decrease as long as number is positive
                if((counter1 - 1)!= -1)
                {
                    numberTxt1.setText(String.valueOf(counter1--));
                }
                else
                {
                    counter1 = 0;
                    numberTxt1.setText(String.valueOf(counter1));
                }
            }
            }
        );

        /* when back btn is pressed, switch back to MainActivity */
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CounterActivity.this, MainActivity.class);
                intent.putExtra("NUM", Integer.valueOf(counter));  // key-value to pass to the MainActivity
                intent.putExtra("NUM2", Integer.valueOf(counter1));  // key-value to pass to the MainActivity
                startActivity(intent);
            }
        });

    }
}