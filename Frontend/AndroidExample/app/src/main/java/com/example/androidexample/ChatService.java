package com.example.androidexample;
import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.CompletableFuture;

public class ChatService {
    private static final String TAG = "ChatService";
    private static final String CHAT_API_BASE_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/chatBot/ask/";

    private final RequestQueue requestQueue;
    private final Context context;

    public ChatService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public CompletableFuture<String> sendMessage(String message, long userId) {
        CompletableFuture<String> future = new CompletableFuture<>();

        try {
            // Create the full URL with the user ID
            String fullUrl = CHAT_API_BASE_URL + userId;
            Log.d(TAG, "Sending message to URL: " + fullUrl);

            // Create JSON request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("message", message);

            // Create Volley request
            JsonObjectRequest jsonRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    fullUrl,
                    requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                // Extract the message from the response
                                String botMessage = response.getString("message");
                                future.complete(botMessage);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing response", e);
                                future.completeExceptionally(e);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "API call failed", error);

                            // Try to extract error message if available
                            String errorMessage = "Request failed";
                            if (error.networkResponse != null && error.networkResponse.data != null) {
                                try {
                                    String responseBody = new String(error.networkResponse.data, "UTF-8");
                                    JSONObject errorJson = new JSONObject(responseBody);
                                    errorMessage = errorJson.optString("message", errorMessage);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing error response", e);
                                }
                            }

                            future.completeExceptionally(new Exception(errorMessage));
                        }
                    }
            );

            // Add request to Volley queue
            requestQueue.add(jsonRequest);

        } catch (Exception e) {
            Log.e(TAG, "Error preparing request", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    public CompletableFuture<String> checkNewMessages(long userId) {
        // This is a placeholder for your polling functionality
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete("");
        return future;
    }
}