package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity2 extends AppCompatActivity {

    private Button sendBtn, backMainBtn, switchbtn;
    private EditText msgEtx;
    private TextView msgTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat2);

        /* initialize UI elements */
        sendBtn = (Button) findViewById(R.id.sendBtn2);
        backMainBtn = (Button) findViewById(R.id.backMainBtn);
        msgEtx = (EditText) findViewById(R.id.msgEdt2);
        msgTv = (TextView) findViewById(R.id.tx2);
        switchbtn = findViewById(R.id.switchbtn);

        /* send button listener */
        sendBtn.setOnClickListener(v -> {
            // broadcast this message to the WebSocketService
            // tag it with the key - to specify which WebSocketClient (connection) to send
            // in this case: "chat2"
            Intent intent = new Intent("SendWebSocketMessage");
            intent.putExtra("key", "chat2");
            intent.putExtra("message", msgEtx.getText().toString());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        });

        /* back button listener */
        backMainBtn.setOnClickListener(view -> {
            // go to chat activity
            endConnection();
            finish();
        });

        switchbtn.setOnClickListener(v -> {
            endConnection();
            // attempt to connect to websocket
            // start Websocket service with key "chat1"
            if (getIntent().getExtras() != null) {
                Intent serviceIntent = new Intent(this, WebSocketService.class);
                serviceIntent.setAction("CONNECT");
                serviceIntent.putExtra("key", "chat1");
                serviceIntent.putExtra("url", getIntent().getExtras().getString("otherServer") + getIntent().getExtras().getString("username"));
                startService(serviceIntent);
            }
            // finish the current activity and open the first server.
            Intent intent = new Intent(this, ChatActivity1.class);
            try {
                intent.putExtra("otherServer", getIntent().getExtras().getString("otherServer"));
                intent.putExtra("username", getIntent().getExtras().getString("username"));
            }
            catch (Exception e) {
                Toast.makeText(this, "Couldn't open other server, not enough passed info!", Toast.LENGTH_SHORT).show();
            }
            startActivity(intent);
            finish();

        });
    }

    // For receiving messages
    // only react to messages with tag "chat2"
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("key");
            if ("chat2".equals(key)){
                String message = intent.getStringExtra("message");
                runOnUiThread(() -> {
                    String s = msgTv.getText().toString();
                    msgTv.setText(s + "\n" + message);
                });
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,
                new IntentFilter("WebSocketMessageReceived"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
    }

    public void endConnection() {
        Intent endPrev = new Intent(this, WebSocketService.class);
        endPrev.setAction("DISCONNECT");
        endPrev.putExtra("key", "chat1");
        endPrev.putExtra("url", getIntent().getExtras().getString("currServer") + getIntent().getExtras().getString("username"));
    }
}