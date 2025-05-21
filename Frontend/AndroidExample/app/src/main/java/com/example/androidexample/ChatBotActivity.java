package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidUI.MessageAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChatBotActivity extends AppCompatActivity {

    private static final String TAG = "ChatBotActivity";

    // UI components
    private ImageButton sendBtn;
    private ImageButton backButton;
    private EditText msgEdt;
    private RecyclerView messagesRecyclerView;
    private TextView userName, subtitle;

    // Message data
    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    long userID = -1;
    String username = "";
    private List<String> sentMessages = new ArrayList<>();

    // Chat service
    private ChatService chatService;

    // Handler for polling (optional)
    private Handler pollHandler;
    private Runnable pollRunnable;
    private boolean isPollActive = false;
    private static final int POLL_INTERVAL = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot); // Make sure to create this layout file

        userID = this.getIntent().getLongExtra("userID", -1);
        username = this.getIntent().getStringExtra("username");

        Log.d(TAG, "Chatbot started for userID: " + userID);

        // Initialize UI components
        sendBtn = findViewById(R.id.sendBtn);
        backButton = findViewById(R.id.backButton);
        msgEdt = findViewById(R.id.msgEdt);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        userName = findViewById(R.id.userName);
        subtitle = findViewById(R.id.subtitle);

        // Initialize message list and adapter
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);

        // Initialize Chat Service
        chatService = new ChatService(this);

        // Initialize polling handler (optional)
        pollHandler = new Handler();
        pollRunnable = this::pollForNewMessages;

        // Set click listeners
        sendBtn.setOnClickListener(v -> sendMessage());
        backButton.setOnClickListener(v -> finish());

        // Add welcome message
        addWelcomeMessage();

        // Start polling for messages (optional)
        startPolling();
    }

    private void addWelcomeMessage() {
        Message welcomeMessage = new Message(
                "Hello! I'm your nail salon assistant. How can I help you today?",
                System.currentTimeMillis(),
                Message.TYPE_INCOMING,
                -1
        );
        messageList.add(welcomeMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        messagesRecyclerView.smoothScrollToPosition(messageList.size() - 1);
    }

    private void sendMessage() {
        String messageContent = msgEdt.getText().toString().trim();
        if (messageContent.isEmpty()) {
            return;
        }

        try {
            // Add message to UI immediately for better UX
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

            // Add to tracking list
            sentMessages.add(messageContent);

            // Send message via ChatService with userID
            chatService.sendMessage(messageContent, userID)
                    .thenAccept(response -> {
                        runOnUiThread(() -> {
                            // Process the response from the server
                            handleServerResponse(response);
                        });
                    })
                    .exceptionally(error -> {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Error: " + error.getMessage());
                            Toast.makeText(ChatBotActivity.this,
                                    "Failed to send message: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                        return null;
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error sending message: " + e.getMessage());
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle response from the chatbot server
    private void handleServerResponse(String response) {
        try {
            // Check if this message is in our sent messages list to avoid duplicates
            boolean alreadySent = false;
            for (String sentMsg : sentMessages) {
                if (response.contains(sentMsg)) {
                    alreadySent = true;
                    break;
                }
            }

            // Only add if not already sent by this user
            if (!alreadySent) {
                // Add bot response to the message list
                Message botMessage = new Message(
                        response,
                        System.currentTimeMillis(),
                        Message.TYPE_INCOMING,
                        -1 // Not the current user
                );

                messageList.add(botMessage);
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                messagesRecyclerView.smoothScrollToPosition(messageList.size() - 1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing response: " + e.getMessage());
        }
    }

    // Optional: Polling for new messages
    private void startPolling() {
        if (!isPollActive && userID != -1) {
            isPollActive = true;
            pollHandler.postDelayed(pollRunnable, POLL_INTERVAL);
        }
    }

    private void stopPolling() {
        isPollActive = false;
        if (pollHandler != null) {
            pollHandler.removeCallbacks(pollRunnable);
        }
    }

    private void pollForNewMessages() {
        // Only poll if we have a valid user ID
        if (userID != -1) {
            chatService.checkNewMessages(userID)
                    .thenAccept(response -> {
                        runOnUiThread(() -> {
                            if (response != null && !response.isEmpty()) {
                                handleServerResponse(response);
                            }

                            // Schedule next poll if active
                            if (isPollActive) {
                                pollHandler.postDelayed(pollRunnable, POLL_INTERVAL);
                            }
                        });
                    })
                    .exceptionally(error -> {
                        // Continue polling despite errors
                        runOnUiThread(() -> {
                            if (isPollActive) {
                                pollHandler.postDelayed(pollRunnable, POLL_INTERVAL);
                            }
                        });
                        return null;
                    });
        }
    }

    @Override
    protected void onDestroy() {
        // Ensure polling is stopped when activity is destroyed
        stopPolling();
        super.onDestroy();
    }
}