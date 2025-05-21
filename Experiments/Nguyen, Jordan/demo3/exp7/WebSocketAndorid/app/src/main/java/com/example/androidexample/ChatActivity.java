package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements WebSocketListener{

    private Button sendBtn;
    private EditText msgEtx;
    private TextView msgTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        /* initialize UI elements */
        sendBtn = (Button) findViewById(R.id.sendBtn);
        msgEtx = (EditText) findViewById(R.id.msgEdt);
        msgTv = (TextView) findViewById(R.id.tx1);

        /* connect this activity to the websocket instance */
        WebSocketManager.getInstance().setWebSocketListener(ChatActivity.this);

        /* send button listener */
        sendBtn.setOnClickListener(v -> {
            try {

                // check the message to see if there is a prohibited word.
                if (!check(msgEtx.getText().toString())) {
                    Toast.makeText(this, "Your message contains text that cannot be sent!", Toast.LENGTH_SHORT).show();
                }
                // send message because it is clean :)
                else {
                    WebSocketManager.getInstance().sendMessage(msgEtx.getText().toString());
                }
            } catch (Exception e) {
                Log.d("ExceptionSendMessage:", e.getMessage().toString());
            }
        });
    }


    @Override
    public void onWebSocketMessage(String message) {
        /**
         * In Android, all UI-related operations must be performed on the main UI thread
         * to ensure smooth and responsive user interfaces. The 'runOnUiThread' method
         * is used to post a runnable to the UI thread's message queue, allowing UI updates
         * to occur safely from a background or non-UI thread.
         */
        runOnUiThread(() -> {
            String s = msgTv.getText().toString();
            msgTv.setText(s + "\n"+message);
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() -> {
            String s = msgTv.getText().toString();
            msgTv.setText(s + "---\nconnection closed by " + closedBy + "\nreason: " + reason);
        });
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {}

    @Override
    public void onWebSocketError(Exception ex) {}


    // USER DEFINED FOR WORD CHECKING.
    // the check() method is used to input the message the user has typed in.
    private boolean check(String phrase) {
        Map<Character, Character> cypherMap = loadCypher();
        if (cypherMap.isEmpty()) return false;

        String encoded = encode(phrase, cypherMap).toLowerCase(); // convert to lowercase after encoding
        return !containsBlacklistedWord(encoded);
    }

    // the loadCypher() function takes the cypher read from the project files.
    private Map<Character, Character> loadCypher() {
        Map<Character, Character> cypherMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("cypher.txt")))) {
            String cypherText = br.readLine();
            if (cypherText == null || cypherText.length() < 26) {
                Log.e("CypherError", "Invalid cypher mapping.");
                return Collections.emptyMap();
            }
            for (int i = 0; i < 26; i++) {
                char mappedChar = Character.toLowerCase(cypherText.charAt(i));
                cypherMap.put((char) ('a' + i), mappedChar);
                cypherMap.put((char) ('A' + i), mappedChar); // map uppercase letters too
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cypherMap;
    }

    // the encode() function encodes the user's message.
    private String encode(String phrase, Map<Character, Character> cypherMap) {
        StringBuilder encoded = new StringBuilder();
        for (char ch : phrase.toCharArray()) {
            encoded.append(cypherMap.getOrDefault(Character.toLowerCase(ch), ch)); // always map to lowercase
        }
        return encoded.toString();
    }

    // this method is used to check if the encoded message 'hit', or has a prohibited word
    private boolean containsBlacklistedWord(String encoded) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("blacklist.txt")))) {
            String blacklistWord;
            while ((blacklistWord = br.readLine()) != null) {
                if (encoded.contains(blacklistWord.trim().toLowerCase())) {
                    Log.d("Filter", "Encoded message contains a word from the blacklist.");
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}