package com.example.androidexample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidUI.NailDesignAdapter;
import com.example.androidUI.NailDesignModel;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmark;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Activity for real-time hand tracking using MediaPipe and CameraX with calibration.
 */
public class LaptopCameraActivity extends AppCompatActivity implements NailDesignAdapter.OnNailDesignSelectedListener {
    private static final String TAG = "LaptopCameraActivity";
    private static final String NAIL_DESIGNS_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/images/image/loader/getALlNails";
    private static final String HAND_LANDMARKER_TASK = "hand_landmarker.task";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};
    private static final float CALIBRATION_STEP = 15f; // Pixels to move per adjustment

    private PreviewView previewView;
    private HandOverlayView handOverlayView;
    private HandLandmarker handLandmarker;
    private Executor analysisExecutor;
    private List<NailDesignModel> nailDesigns = new ArrayList<>();
    private Button btnShowDesigns;
    private LinearLayout designSelectorLayout;
    private boolean isDesignSelectorVisible = false;
    private MainViewModel viewModel;
    private HandLandmarkerResult latestResult;
    private Handler handler = new Handler();

    // UI elements for stage management
    private LinearLayout calibrationControls;
    private MaterialButton btnModeToggle;
    private boolean isCalibrationMode = true;  // Start in calibration mode

    // Reusable buffers
    private Bitmap bmpBuffer;
    private final Matrix rotationMatrix = new Matrix();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laptop_camera);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Override default settings for faster updates and single hand focus
        viewModel.setMinHandDetectionConfidence(0.2f);
        viewModel.setMinHandTrackingConfidence(0.2f);
        viewModel.setMinHandPresenceConfidence(0.2f);
        viewModel.setMaxHands(1);

        previewView = findViewById(R.id.previewView);
        handOverlayView = findViewById(R.id.handOverlayView);
        analysisExecutor = Executors.newSingleThreadExecutor();

        // Initialize UI elements
        calibrationControls = findViewById(R.id.calibrationControls);
        btnModeToggle = findViewById(R.id.btnModeToggle);

        // Initially we're in calibration mode
        handOverlayView.setViewMode(HandOverlayView.MODE_CALIBRATION);
        updateModeButton();

        // Initialize Select Nail Design button
        btnShowDesigns = findViewById(R.id.btnShowDesigns);
        btnShowDesigns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nailDesigns.isEmpty()) {
                    // First time, need to fetch designs
                    fetchNailDesigns();
                } else {
                    // Already fetched, just show the selector
                    showDesignSelector();
                }
            }
        });

        // Setup stage transition button
        setupModeToggleButton();

        // Setup calibration controls
        setupCalibrationControls();

        if (allPermissionsGranted()) {
            setupHandLandmarker();
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        // Setup design selector
        designSelectorLayout = findViewById(R.id.designSelectorLayout);
        Button btnCloseDesignSelector = findViewById(R.id.btnCloseDesignSelector);
        btnCloseDesignSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideDesignSelector();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isDesignSelectorVisible) {
            hideDesignSelector();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Setup mode toggle button to switch between calibration and nail design modes
     */
    private void setupModeToggleButton() {
        btnModeToggle.setOnClickListener(v -> {
            toggleViewMode();
        });
    }

    /**
     * Toggle between calibration and nail design modes
     */
    private void toggleViewMode() {
        isCalibrationMode = !isCalibrationMode;

        if (isCalibrationMode) {
            // Switch to calibration mode
            handOverlayView.setViewMode(HandOverlayView.MODE_CALIBRATION);
            calibrationControls.setVisibility(View.VISIBLE);
        } else {
            // Switch to nail design mode
            handOverlayView.setViewMode(HandOverlayView.MODE_NAIL_DESIGN);
            calibrationControls.setVisibility(View.GONE);
        }

        // Update button text based on current mode
        updateModeButton();

        Toast.makeText(this, isCalibrationMode ?
                        "Calibration mode activated" :
                        "Nail design mode activated",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Update the toggle button text based on current mode
     */
    private void updateModeButton() {
        if (isCalibrationMode) {
            btnModeToggle.setText("Confirm");
            btnModeToggle.setIcon(null);
        } else {
            btnModeToggle.setText("Calibrate");
            btnModeToggle.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_manage));
        }
    }

    /**
     * Initialize calibration control buttons
     */
    private void setupCalibrationControls() {
        // Horizontal movement
        findViewById(R.id.btnMoveLeft).setOnClickListener(v -> {
            handOverlayView.setCalibrationOffset(
                    handOverlayView.getXOffset() - CALIBRATION_STEP,
                    handOverlayView.getYOffset()
            );
            Log.d(TAG, "X offset: " + handOverlayView.getXOffset());
        });

        findViewById(R.id.btnMoveRight).setOnClickListener(v -> {
            handOverlayView.setCalibrationOffset(
                    handOverlayView.getXOffset() + CALIBRATION_STEP,
                    handOverlayView.getYOffset()
            );
            Log.d(TAG, "X offset: " + handOverlayView.getXOffset());
        });

        // Vertical movement
        findViewById(R.id.btnMoveUp).setOnClickListener(v -> {
            handOverlayView.setCalibrationOffset(
                    handOverlayView.getXOffset(),
                    handOverlayView.getYOffset() - CALIBRATION_STEP
            );
            Log.d(TAG, "Y offset: " + handOverlayView.getYOffset());
        });

        findViewById(R.id.btnMoveDown).setOnClickListener(v -> {
            handOverlayView.setCalibrationOffset(
                    handOverlayView.getXOffset(),
                    handOverlayView.getYOffset() + CALIBRATION_STEP
            );
            Log.d(TAG, "Y offset: " + handOverlayView.getYOffset());
        });

        // Auto calibration
        findViewById(R.id.btnAutoCalibrate).setOnClickListener(v -> {
            autoCalibrate();
        });
    }

    private void fetchNailDesigns() {
        // Show loading
        Toast.makeText(this, "Loading nail designs...", Toast.LENGTH_SHORT).show();

        // Make the request to get all nail designs
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                NAIL_DESIGNS_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            parseNailDesignsResponse(response);
                            showDesignSelector();
                        } catch (JSONException e) {
                            Toast.makeText(LaptopCameraActivity.this,
                                    "Error parsing nail designs", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "JSON parse error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LaptopCameraActivity.this,
                                "Error fetching nail designs", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Volley error: " + error.getMessage());
                    }
                }
        );

        // Add the request using VolleySingleton
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void parseNailDesignsResponse(JSONObject response) throws JSONException {
        nailDesigns.clear();

        JSONArray designsArray = response.getJSONArray("object");
        for (int i = 0; i < designsArray.length(); i++) {
            JSONObject designObj = designsArray.getJSONObject(i);

            // Each object has a single key-value pair where:
            // - key = design name
            // - value = image URL
            Iterator<String> keys = designObj.keys();
            if (keys.hasNext()) {
                String name = keys.next();
                String imageUrl = designObj.getString(name);
                nailDesigns.add(new NailDesignModel(name, imageUrl));
            }
        }

        Log.d(TAG, "Parsed " + nailDesigns.size() + " nail designs");
    }

    private void showDesignSelector() {
        // Setup the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.nailDesignRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // Create and set the adapter
        NailDesignAdapter adapter = new NailDesignAdapter(this, nailDesigns, this);
        recyclerView.setAdapter(adapter);

        // Show the selector - directly use LinearLayout instead of MaterialCardView
        designSelectorLayout.setVisibility(View.VISIBLE);
        isDesignSelectorVisible = true;
    }

    private void loadSelectedNailDesign(NailDesignModel design) {
        // Download the selected nail design image
        ImageRequest imageRequest = new ImageRequest(
                design.getImageUrl(),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        // Set the bitmap on the HandOverlayView
                        handOverlayView.setNailDesignBitmap(bitmap);

                        // If we're not already in nail design mode, switch to it
                        if (handOverlayView.getViewMode() != HandOverlayView.MODE_NAIL_DESIGN) {
                            // Switch to nail design mode
                            isCalibrationMode = false;
                            handOverlayView.setViewMode(HandOverlayView.MODE_NAIL_DESIGN);
                            calibrationControls.setVisibility(View.GONE);
                            updateModeButton();
                        }
                    }
                },
                0, 0, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LaptopCameraActivity.this,
                                "Error loading nail design image", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Image load error: " + error.getMessage());
                    }
                }
        );

        // Add the request using VolleySingleton
        VolleySingleton.getInstance(this).addToRequestQueue(imageRequest);
    }

    @Override
    public void onNailDesignSelected(NailDesignModel design) {
        Toast.makeText(this, "Selected: " + design.getName(), Toast.LENGTH_SHORT).show();

        // Load the selected design
        loadSelectedNailDesign(design);

        // Hide the selector
        hideDesignSelector();
    }

    private void hideDesignSelector() {
        designSelectorLayout.setVisibility(View.GONE);
        isDesignSelectorVisible = false;
    }

    /**
     * Auto-calibrate the hand overlay by analyzing hand position
     */
    private void autoCalibrate() {
        // Show calibration animation/toast
        Toast.makeText(this, "Place your hand in the center of the screen", Toast.LENGTH_LONG).show();

        // Visual feedback for auto-calibration
        MaterialButton btnAutoCalibrate = findViewById(R.id.btnAutoCalibrate);
        btnAutoCalibrate.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_popup_sync));
        btnAutoCalibrate.setEnabled(false);
        btnAutoCalibrate.setText("Calibrating...");

        handler.postDelayed(() -> {
            if (latestResult != null && !latestResult.landmarks().isEmpty()) {
                // Get hand landmarks
                List<NormalizedLandmark> landmarks = latestResult.landmarks().get(0);

                // Calculate center of palm (middle finger MCP joint)
                float centerX = landmarks.get(HandLandmark.MIDDLE_FINGER_MCP).x();
                float centerY = landmarks.get(HandLandmark.MIDDLE_FINGER_MCP).y();

                // Calculate offset needed to center the palm
                float targetX = 0.5f; // Center of screen horizontally
                float targetY = 0.5f; // Center of screen vertically

                float xOffset = (targetX - centerX) * previewView.getWidth();
                float yOffset = (targetY - centerY) * previewView.getHeight();

                // Adjust for front camera mirroring if needed
                if (true) { // isFrontCamera
                    xOffset = -xOffset;
                }

                // Apply calculated offset
                handOverlayView.setCalibrationOffset(xOffset, yOffset);

                Log.d(TAG, "Auto-calibration applied. X offset: " + xOffset + ", Y offset: " + yOffset);
                Toast.makeText(LaptopCameraActivity.this, "Calibration complete", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LaptopCameraActivity.this, "No hand detected. Try again.", Toast.LENGTH_SHORT).show();
            }

            // Reset button state
            btnAutoCalibrate.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_mylocation));
            btnAutoCalibrate.setEnabled(true);
            btnAutoCalibrate.setText("Auto Calibrate");
        }, 3000); // 3 second delay
    }

    /**
     * Initialize MediaPipe HandLandmarker using ViewModel settings.
     */
    private void setupHandLandmarker() {
        // Determine delegate type based on ViewModel setting
        Delegate delegateType = Delegate.CPU;
        if (viewModel.getCurrentDelegate() == HandLandmarkHelper.DELEGATE_GPU) {
            delegateType = Delegate.GPU;
        }

        BaseOptions baseOptions = BaseOptions.builder()
                .setModelAssetPath(HAND_LANDMARKER_TASK)
                .setDelegate(delegateType)
                .build();

        handLandmarker = HandLandmarker.createFromOptions(
                this,
                HandLandmarker.HandLandmarkerOptions.builder()
                        .setBaseOptions(baseOptions)
                        .setNumHands(viewModel.getCurrentMaxHands())
                        .setMinHandDetectionConfidence(viewModel.getCurrentMinHandDetectionConfidence())
                        .setMinHandPresenceConfidence(viewModel.getCurrentMinHandPresenceConfidence())
                        .setMinTrackingConfidence(viewModel.getCurrentMinHandTrackingConfidence())
                        .setRunningMode(RunningMode.LIVE_STREAM)
                        .setResultListener(this::onHandLandmarkerResult)
                        .build()
        );
    }

    /**
     * Callback for MediaPipe landmarks with image dimensions for better scaling.
     */
    private void onHandLandmarkerResult(HandLandmarkerResult result, MPImage image) {
        if (result == null || result.landmarks().isEmpty()) {
            return;
        }

        // Store latest result for auto-calibration
        latestResult = result;

        // Log dimensions to ensure they're correct
        Log.d(TAG, "Image dimensions: " + image.getWidth() + "x" + image.getHeight());

        runOnUiThread(() -> handOverlayView.setResults(
                result,
                image.getWidth(),
                image.getHeight(),
                true // isFrontCamera - assuming front camera is being used
        ));
    }

    /**
     * Configure and start CameraX preview and analysis with improved settings.
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Get screen metrics for better resolution matching
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int screenWidth = metrics.widthPixels;
                int screenHeight = metrics.heightPixels;
                float screenAspectRatio = (float)screenWidth / screenHeight;

                // Determine best camera resolution
                Size targetResolution;
                if (screenAspectRatio > 1) {
                    // Landscape
                    targetResolution = new Size(640, 480);
                } else {
                    // Portrait
                    targetResolution = new Size(480, 640);
                }

                // Preview with consistent resolution
                Preview preview = new Preview.Builder()
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .setTargetResolution(targetResolution)
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Analysis with the same resolution
                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setTargetResolution(targetResolution) // Consistent with preview
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setImageQueueDepth(1)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();
                analysis.setAnalyzer(analysisExecutor, this::analyzeImage);

                // Camera selector
                CameraSelector selector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, selector, preview, analysis);

                // Apply default calibration after a short delay to ensure view dimensions are set
                new Handler().postDelayed(() -> {
                    handOverlayView.setDefaultCalibrationOffset();
                    Toast.makeText(LaptopCameraActivity.this,
                            "Default calibration applied", Toast.LENGTH_SHORT).show();
                }, 1000);

            } catch (Exception e) {
                Log.e(TAG, "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Process camera frames for MediaPipe with improved efficiency.
     */
    private void analyzeImage(ImageProxy imageProxy) {
        if (handLandmarker == null) {
            imageProxy.close();
            return;
        }

        ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
        buffer.rewind();
        int width = imageProxy.getWidth();
        int height = imageProxy.getHeight();
        if (bmpBuffer == null || bmpBuffer.getWidth() != width || bmpBuffer.getHeight() != height) {
            bmpBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        bmpBuffer.copyPixelsFromBuffer(buffer);

        MPImage mpImage;
        int rotation = imageProxy.getImageInfo().getRotationDegrees();
        if (rotation != 0) {
            rotationMatrix.reset();
            rotationMatrix.postRotate(rotation);
            Bitmap rotated = Bitmap.createBitmap(bmpBuffer, 0, 0, width, height, rotationMatrix, true);
            mpImage = new BitmapImageBuilder(rotated).build();
        } else {
            mpImage = new BitmapImageBuilder(bmpBuffer).build();
        }

        handLandmarker.detectAsync(mpImage, imageProxy.getImageInfo().getTimestamp());
        imageProxy.close();
    }

    /**
     * Check all required permissions.
     */
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
            setupHandLandmarker();
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handLandmarker != null) {
            handLandmarker.close();
        }
    }
}