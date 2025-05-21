package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidUI.Employee;
import com.example.androidUI.EmployeeAdapter;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewMessagesActivity extends AppCompatActivity implements EmployeeAdapter.OnEmployeeClickListener, WebSocketListener {

    private static final String TAG = "ViewMessagesActivity";
    private static final String API_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/employee/info/name";
    private static final String WEBSOCKET_BASE_URL = "ws://coms-3090-020.class.las.iastate.edu:8080/ws/chat/";

    // UI components
    private RecyclerView employeesRecyclerView;
    private ProgressBar progressBar;
    private ImageButton backButton;

    private TextView titleTextView;
    private TextView subtitleTextView;

    // Data
    private EmployeeAdapter employeeAdapter;
    private List<Employee> employeeList;
    private RequestQueue requestQueue;

    // Current user information
    private int userID = -1;
    private String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_messages);

        // Get user information from the intent
        userID = getIntent().getIntExtra("userID", -1);
        username = getIntent().getStringExtra("username");

        // Initialize UI components
        employeesRecyclerView = findViewById(R.id.employeesRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.backButton);
        titleTextView = findViewById(R.id.title);
        subtitleTextView = findViewById(R.id.subtitle);

        // Initialize employee list and adapter
        employeeList = new ArrayList<>();
        employeeAdapter = new EmployeeAdapter(this, employeeList, this);

        // Setup RecyclerView
        employeesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        employeesRecyclerView.setAdapter(employeeAdapter);

        // Set click listeners
        backButton.setOnClickListener(v -> onBackPressed());

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        // Fetch employees data
        fetchEmployees();
    }

    private void fetchEmployees() {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        employeesRecyclerView.setVisibility(View.GONE);

        // Create JSON request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                API_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressBar.setVisibility(View.GONE);
                        employeesRecyclerView.setVisibility(View.VISIBLE);
                        parseEmployeesResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        employeesRecyclerView.setVisibility(View.VISIBLE);

                        Log.e(TAG, "Error fetching employees: " + error.getMessage());
                        Toast.makeText(ViewMessagesActivity.this,
                                "Failed to load contacts", Toast.LENGTH_SHORT).show();

                        // If we want to show an error state in the UI
                        showErrorState();
                    }
                }
        );

        // Add request to queue
        requestQueue.add(jsonObjectRequest);
    }

    private void showErrorState() {
        // This could show an error message or retry button
        if (employeeList.isEmpty()) {
            subtitleTextView.setText("Could not load contacts. Pull down to refresh.");
        }
    }

    private void parseEmployeesResponse(JSONObject response) {
        try {
            // Clear current list
            employeeList.clear();

            // Get the "object" array from the response
            JSONArray objectArray = response.getJSONArray("object");

            // Iterate through the array
            for (int i = 0; i < objectArray.length(); i++) {
                JSONObject employeeObj = objectArray.getJSONObject(i);

                // Extract employee data
                String name = employeeObj.getString("message");
                int id = employeeObj.getInt("id");

                // Create and add employee object
                Employee employee = new Employee(id, name);
                employeeList.add(employee);
            }

            // Notify adapter of data change
            employeeAdapter.notifyDataSetChanged();

            // Update subtitle based on results
            if (employeeList.isEmpty()) {
                subtitleTextView.setText("No contacts available");
            } else {
                subtitleTextView.setText(employeeList.size() + " contacts available");
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
            Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
            showErrorState();
        }
    }

    @Override
    public void onEmployeeClick(Employee employee) {
        // First, disconnect any existing WebSocket connection
        WebSocketManager.getInstance().disconnectWebSocket();

        // Setup WebSocket connection for the selected employee
        setupWebSocketConnection(employee.getName());

        // Start chat activity with selected employee
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("userID", userID);
        intent.putExtra("username", username);
        intent.putExtra("nailtech", employee.getName());
        intent.putExtra("nailtechID", employee.getId());
        startActivity(intent);
    }

    private void setupWebSocketConnection(String nailtechName) {
        // Set up WebSocket connection for chat
        String serverUrl = WEBSOCKET_BASE_URL + username + "/to/" + nailtechName;

        // Get WebSocketManager instance and connect
        WebSocketManager webSocketManager = WebSocketManager.getInstance();

        // Set this activity as the WebSocket listener
        webSocketManager.setWebSocketListener(this);

        // Connect to WebSocket server
        webSocketManager.connectWebSocket(serverUrl);

        Log.d(TAG, "WebSocket connection setup with URL: " + serverUrl);
    }

    // WebSocketListener Implementation
    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "WebSocket connection opened");
    }

    @Override
    public void onWebSocketMessage(String message) {
        Log.d(TAG, "WebSocket message received: " + message);
        // You can handle incoming messages here if needed
        // Typically, the ChatActivity would handle these messages
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        Log.d(TAG, "WebSocket connection closed: " + reason);
    }

    @Override
    public void onWebSocketError(Exception ex) {
        Log.e(TAG, "WebSocket error: " + ex.getMessage());
        runOnUiThread(() -> {
            Toast.makeText(ViewMessagesActivity.this,
                    "Chat connection error: " + ex.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove this activity as the WebSocket listener
        // Note: We don't disconnect the WebSocket here because
        // the ChatActivity needs to use the same connection
        // The ChatActivity will handle disconnecting when it's finished
        WebSocketManager.getInstance().removeWebSocketListener();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Remove this activity as the WebSocket listener when paused
        // This prevents messages being processed by both activities
        WebSocketManager.getInstance().removeWebSocketListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If returning to this activity (back from ChatActivity),
        // we should disconnect any existing WebSocket as it's no longer needed
        WebSocketManager.getInstance().disconnectWebSocket();
    }
}