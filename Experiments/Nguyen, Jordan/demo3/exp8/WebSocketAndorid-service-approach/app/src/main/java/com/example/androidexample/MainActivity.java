package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{

    private Button connectBtn1, connectBtn2, backBtn1, backBtn2;
    private EditText serverEtx1, usernameEtx1, serverEtx2, usernameEtx2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* initialize UI elements */
        connectBtn1 = (Button) findViewById(R.id.connectBtn);
        connectBtn2 = (Button) findViewById(R.id.connectBtn2);
        backBtn1 = (Button) findViewById(R.id.backBtn);
        backBtn2 = (Button) findViewById(R.id.backBtn2);
        serverEtx1 = (EditText) findViewById(R.id.serverEdt);
        usernameEtx1 = (EditText) findViewById(R.id.unameEdt);
        serverEtx2 = (EditText) findViewById(R.id.serverEdt2);
        usernameEtx2 = (EditText) findViewById(R.id.unameEdt2);

        /* connect1 button listener */
        connectBtn1.setOnClickListener(view -> {
            if(usernameEtx1.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter a username.", Toast.LENGTH_SHORT).show();
            }
            else if (serverEtx1.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter a server URL", Toast.LENGTH_SHORT).show();
            }
            else {
                String serverUrl = serverEtx1.getText().toString() + usernameEtx1.getText().toString();

                // start Websocket service with key "chat1"
                Intent serviceIntent = new Intent(this, WebSocketService.class);
                serviceIntent.setAction("CONNECT");
                serviceIntent.putExtra("key", "chat1");
                serviceIntent.putExtra("url", serverUrl);
                startService(serviceIntent);

                // go to chat activity #1
                Intent intent = new Intent(this, ChatActivity1.class);
                // get the second server URL (if its filled)
                if (!serverEtx2.getText().toString().isEmpty()) {
                    intent.putExtra("currServer", serverEtx1.getText().toString());
                    intent.putExtra("otherServer", serverEtx2.getText().toString());
                    intent.putExtra("username", usernameEtx1.getText().toString());
                }
                startActivity(intent);
            }
        });

        /* connect2 button listener */
        connectBtn2.setOnClickListener(view -> {
            if(usernameEtx2.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter a username.", Toast.LENGTH_SHORT).show();
            }
            else if (serverEtx2.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter a server URL", Toast.LENGTH_SHORT).show();
            }
            else {
                String serverUrl = serverEtx2.getText().toString() + usernameEtx2.getText().toString();

                // start Websocket service with key "chat2"
                Intent serviceIntent = new Intent(this, WebSocketService.class);
                serviceIntent.setAction("CONNECT");
                serviceIntent.putExtra("key", "chat2");
                serviceIntent.putExtra("url", serverUrl);
                startService(serviceIntent);

                // go to chat activity #2
                Intent intent = new Intent(this, ChatActivity2.class);
                if (!serverEtx1.getText().toString().isEmpty()) {
                    intent.putExtra("currServer", serverEtx2.getText().toString());
                    intent.putExtra("otherServer", serverEtx1.getText().toString());
                    intent.putExtra("username", usernameEtx2.getText().toString());
                }
                startActivity(intent);
            }
        });

        /* back button listener */
        backBtn1.setOnClickListener(view -> {
            // got to chat activity
            Intent intent = new Intent(this, ChatActivity1.class);
            startActivity(intent);
        });

        /* back2 button listener */
        backBtn2.setOnClickListener(view -> {
            // got to chat activity
            Intent intent = new Intent(this, ChatActivity2.class);
            startActivity(intent);
        });
    }
}