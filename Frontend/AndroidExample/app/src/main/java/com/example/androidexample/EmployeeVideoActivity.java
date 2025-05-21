package com.example.androidexample;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class EmployeeVideoActivity extends AppCompatActivity {
    private static final String TAG = "VideoPlayerActivity";

    // UI Components
    private Toolbar toolbar;
    private VideoView videoView;
    private TextView videoTitleText;
    private ProgressBar loadingProgressBar;
    private ImageButton backButton;
    private ImageButton fullscreenButton;

    // Video Data
    private int videoId;
    private String videoTitle;
    private String videoUrl;
    private boolean isRequired;

    // State
    private boolean isFullscreen = false;
    private int currentPosition = 0;
    private boolean isVideoCompleted = false;

    // Constants
    private static final String TRAINING_VIDEO_COMPLETION_URL =
            "http://coms-3090-020.class.las.iastate.edu:8080/api/employee/training/complete/";
    private static final String EMPLOYEE_PREFS = "EmployeePrefs";
    private static final String VIDEO_COMPLETION_KEY = "video_completed_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_videoplayer);

        // Get data from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            videoId = extras.getInt("videoId", -1);
            videoTitle = extras.getString("videoTitle", "Training Video");
            videoUrl = extras.getString("videoUrl", "");
            isRequired = extras.getBoolean("isRequired", false);
        }

        // Initialize UI components
        initializeUIComponents();

        // Set up video player
        setupVideoPlayer();

        // Set up click listeners
        setupClickListeners();

        // Check if video was previously completed
        checkVideoCompletionStatus();
    }

    private void initializeUIComponents() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Video components
        videoView = findViewById(R.id.videoView);
        videoTitleText = findViewById(R.id.videoTitleText);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        // Buttons
        backButton = findViewById(R.id.backButton);
        fullscreenButton = findViewById(R.id.fullscreenButton);

        // Set video title
        videoTitleText.setText(videoTitle);
    }

    private void setupVideoPlayer() {
        // Use a sample video URL if none provided
        if (videoUrl == null || videoUrl.isEmpty()) {
            // Use a public domain sample video URL
            videoUrl = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4";
        }

        try {
            // Create media controller
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);

            // Set media controller to video view
            videoView.setMediaController(mediaController);

            // Set video URI
            videoView.setVideoURI(Uri.parse(videoUrl));

            // Set listeners
            videoView.setOnPreparedListener(mp -> {
                // Hide loading progress
                loadingProgressBar.setVisibility(View.GONE);

                // Start playback
                if (currentPosition > 0) {
                    videoView.seekTo(currentPosition);
                }
                videoView.start();

                // Set media player listeners
                setupMediaPlayerListeners(mp);
            });

            videoView.setOnErrorListener((mp, what, extra) -> {
                // Handle video playback errors
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(EmployeeVideoActivity.this,
                        "Error playing video",
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error playing video: " + what + ", " + extra);
                return true;
            });

            // Request focus
            videoView.requestFocus();

            // Show loading progress
            loadingProgressBar.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            Log.e(TAG, "Error setting up video player", e);
            Toast.makeText(this, "Error setting up video player", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupMediaPlayerListeners(MediaPlayer mp) {
        // Set up completion listener
        mp.setOnCompletionListener(mediaPlayer -> {
            // Mark video as completed
            markVideoAsCompleted();

            // Handle UI changes on completion
            isVideoCompleted = true;
            Toast.makeText(EmployeeVideoActivity.this,
                    "Video completed",
                    Toast.LENGTH_SHORT).show();

            // Restart video after a delay
            new Handler().postDelayed(() -> {
                if (videoView != null) {
                    videoView.seekTo(0);
                }
            }, 500);
        });
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> onBackPressed());

        // Fullscreen button
        fullscreenButton.setOnClickListener(v -> toggleFullscreen());
    }

    private void toggleFullscreen() {
        isFullscreen = !isFullscreen;

        if (isFullscreen) {
            // Hide system UI for fullscreen
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            // Hide toolbar and controls
            toolbar.setVisibility(View.GONE);

            // Update fullscreen button icon
            fullscreenButton.setImageResource(R.drawable.ic_fullscreen_exit);
        } else {
            // Show system UI
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            // Show toolbar and controls
            toolbar.setVisibility(View.VISIBLE);

            // Update fullscreen button icon
            fullscreenButton.setImageResource(R.drawable.ic_fullscreen);
        }
    }

    private void checkVideoCompletionStatus() {
        SharedPreferences prefs = getSharedPreferences(EMPLOYEE_PREFS, MODE_PRIVATE);
        isVideoCompleted = prefs.getBoolean(VIDEO_COMPLETION_KEY + videoId, false);
    }

    private void markVideoAsCompleted() {
        if (isVideoCompleted) {
            return; // Already completed
        }

        // Mark as completed locally
        SharedPreferences prefs = getSharedPreferences(EMPLOYEE_PREFS, MODE_PRIVATE);
        prefs.edit().putBoolean(VIDEO_COMPLETION_KEY + videoId, true).apply();

        // Update completion status on server if we have a valid video ID and user ID
        int userID = prefs.getInt("userID", -1);
        if (videoId > 0 && userID > 0) {
            updateVideoCompletionOnServer(userID, videoId);
        }
    }

    private void updateVideoCompletionOnServer(int userID, int videoId) {
        String url = TRAINING_VIDEO_COMPLETION_URL + userID + "/" + videoId;

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("completed", true);

            JsonObjectRequest completionRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    requestBody,
                    response -> {
                        Log.d(TAG, "Video completion updated on server");
                    },
                    error -> {
                        Log.e(TAG, "Error updating video completion on server", error);
                    }
            );

            // Add request to queue
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(completionRequest);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating completion request JSON", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save current position
        if (videoView != null) {
            currentPosition = videoView.getCurrentPosition();
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume playback
        if (videoView != null) {
            videoView.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release resources
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }

    @Override
    public void onBackPressed() {
        if (isFullscreen) {
            // Exit fullscreen first
            toggleFullscreen();
        } else {
            super.onBackPressed();
        }
    }
}