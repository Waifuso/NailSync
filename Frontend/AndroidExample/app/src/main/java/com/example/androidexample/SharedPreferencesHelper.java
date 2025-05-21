package com.example.androidexample;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class to manage SharedPreferences
 */
public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_POINTS = "user_points";

    private final SharedPreferences prefs;

    public SharedPreferencesHelper(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get the user ID from SharedPreferences
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "user123");
    }

    /**
     * Set the user ID in SharedPreferences
     */
    public void setUserId(String userId) {
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    /**
     * Get the user's current points
     */
    public int getUserPoints() {
        return prefs.getInt(KEY_USER_POINTS, 3245); // Default to 3245 for testing
    }

    /**
     * Update the user's points
     */
    public void updateUserPoints(int points) {
        prefs.edit().putInt(KEY_USER_POINTS, points).apply();
    }
}