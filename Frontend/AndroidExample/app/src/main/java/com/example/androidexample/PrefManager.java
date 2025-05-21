package com.example.androidexample;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USER_ID = "userID";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";

    private static PrefManager instance;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private PrefManager(Context context) {
        // Use application context to avoid memory leaks
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized PrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new PrefManager(context);
        }
        return instance;
    }

    // Setters
    public void setUserId(int userId) {
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
    }

    public void setUsername(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public void setEmail(String email) {
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    // Getters
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    // Clear preferences (e.g., on logout)
    public void clear() {

        editor.clear().apply();
    }
}
