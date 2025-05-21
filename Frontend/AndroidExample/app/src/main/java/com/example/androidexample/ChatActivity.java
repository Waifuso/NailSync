package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidUI.MessageAdapter;

import org.java_websocket.handshake.ServerHandshake;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements WebSocketListener {

    private static final String TAG = "ChatActivity";

    // UI components
    private ImageButton sendBtn;
    private ImageButton attachButton;
    private ImageButton backButton;
    private EditText msgEdt;
    private RecyclerView messagesRecyclerView;
    private TextView userName;

    // Message data
    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    int userID = -1;
    String username = "";
    private List<String> sentMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userID = this.getIntent().getIntExtra("userID", -1);
        username = this.getIntent().getStringExtra("username");

        // Initialize UI components
        sendBtn = findViewById(R.id.sendBtn);
        attachButton = findViewById(R.id.attachButton);
        backButton = findViewById(R.id.backButton);
        msgEdt = findViewById(R.id.msgEdt);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        userName = findViewById(R.id.userName);

        // Get person to message username from previous intent if available
        // pretty much the
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("nailtech")) {
            userName.setText(intent.getStringExtra("nailtech"));
        }

        // Initialize message list and adapter
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);


        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);

        // Connect to WebSocket
        WebSocketManager.getInstance().setWebSocketListener(this);

        // Set click listeners
        sendBtn.setOnClickListener(v -> sendMessage());
        attachButton.setOnClickListener(v -> attachFile());
        backButton.setOnClickListener(v -> safelyCloseAndFinish());
    }

    // In your sendMessage() method, after sending the message:
    private void sendMessage() {
        String messageContent = msgEdt.getText().toString().trim();
        if (messageContent.isEmpty()) {
            return;
        }

        try {
            // Send message through WebSocket
            WebSocketManager.getInstance().sendMessage(messageContent);

            // Add to tracking list
            sentMessages.add(messageContent);

            // Add message to UI
            Message message = new Message(
                    messageContent,
                    System.currentTimeMillis(),
                    Message.TYPE_OUTGOING,
                    userID
            );
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            messagesRecyclerView.smoothScrollToPosition(messageList.size() - 1);

            // Clear input field
            msgEdt.setText("");

        }
        catch (Exception e) {
            Log.e(TAG, "Error sending message: " + e.getMessage());
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
        }
    }

    private void attachFile() {
        // Implement file attachment functionality
        Toast.makeText(this, "Attachment feature coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWebSocketMessage(String message) {
        // Only show message when the user has sent something first
        if (!messageList.isEmpty()) {
            runOnUiThread(() -> {
                // Check if this message is in our sent messages list
                boolean alreadySent = false;
                for (String sentMsg : sentMessages) {
                    if (message.contains(sentMsg)) {
                        alreadySent = true;
                        break;
                    }
                }

                // Only add if not already sent by this user
                if (!alreadySent) {
                    Message incomingMessage = new Message(
                            message,
                            System.currentTimeMillis(),
                            Message.TYPE_INCOMING,
                            -1 // Not the current user
                    );

                    messageList.add(incomingMessage);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    messagesRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                }
            });
        }
    }


    // Helper method to extract user ID from message
    private int extractUserIdFromMessage(String message) {
        // Implement based on your server's message format
        // Example: "userID:123|content:Hello"
        try {
            if (message.contains("userID:")) {
                int startIndex = message.indexOf("userID:") + 7;
                int endIndex = message.indexOf("|", startIndex);
                String userIdStr = message.substring(startIndex, endIndex);
                return Integer.parseInt(userIdStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting user ID: " + e.getMessage());
        }
        return -1; // Default user ID if extraction fails
    }

    // Helper method to extract content from message
    private String extractContentFromMessage(String message) {
        // Implement based on your server's message format
        // Example: "userID:123|content:Hello"
        try {
            if (message.contains("content:")) {
                int startIndex = message.indexOf("content:") + 8;
                return message.substring(startIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting content: " + e.getMessage());
        }
        return message; // Return original message if extraction fails
    }




    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() -> {
            Toast.makeText(
                    this,
                    "Connection closed by " + closedBy + ": " + reason,
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        // Do not show any toast or message on connection
    }

    @Override
    public void onWebSocketError(Exception ex) {
        runOnUiThread(() -> {
            Toast.makeText(
                    this,
                    "Error: " + ex.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    // safely leave the chat and close the activity
    private void safelyCloseAndFinish() {
        // Disconnect from WebSocket
        WebSocketManager.getInstance().removeWebSocketListener();
        WebSocketManager.getInstance().disconnectWebSocket();

        Log.d(TAG, "Safely closing and finishing activity");
        Log.d(TAG, "Websocket Closed");
        // Finish activity
        finish();
    }

    @Override
    protected void onDestroy() {
        // Ensure WebSocket is disconnected when activity is destroyed
        WebSocketManager.getInstance().removeWebSocketListener();
        WebSocketManager.getInstance().disconnectWebSocket();
        super.onDestroy();
    }
}
