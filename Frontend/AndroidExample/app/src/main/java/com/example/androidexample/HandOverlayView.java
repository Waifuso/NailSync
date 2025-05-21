package com.example.androidexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmark;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;
import com.google.mediapipe.tasks.vision.core.RunningMode;

import java.util.List;

/**
 * Efficient overlay view for drawing MediaPipe hand landmarks with calibration support
 * and nail design overlays.
 */
public class HandOverlayView extends View {
    private static final String TAG = "HandOverlayView";
    private HandLandmarkerResult results;
    private final Paint landmarkPaint;
    private final Paint connectionPaint;
    private int viewWidth;
    private int viewHeight;
    private int imageWidth = 1;
    private int imageHeight = 1;
    private float scaleFactor = 1f;
    private boolean isFrontCamera = true;
    private RunningMode runningMode = RunningMode.LIVE_STREAM;

    // View mode constants
    public static final int MODE_CALIBRATION = 0;
    public static final int MODE_NAIL_DESIGN = 1;

    // Current view mode
    private int viewMode = MODE_CALIBRATION;

    // Nail design bitmap
    private Bitmap nailDesignBitmap;
    private final Matrix nailMatrix = new Matrix();
    private final Paint bitmapPaint = new Paint();

    // Fingertip landmark indices
    private static final int[] FINGERTIP_LANDMARKS = {
            HandLandmark.THUMB_TIP,
            HandLandmark.INDEX_FINGER_TIP,
            HandLandmark.MIDDLE_FINGER_TIP,
            HandLandmark.RING_FINGER_TIP,
            HandLandmark.PINKY_TIP
    };

    // Calibration offsets
    private float xOffset = 0f;
    private float yOffset = 0f;

    // Landmarks per hand
    private static final int LANDMARK_COUNT = 21;
    private static final int[][] CONNECTIONS = {
            // Connections array as in original code
            {HandLandmark.WRIST, HandLandmark.THUMB_CMC},
            {HandLandmark.THUMB_CMC, HandLandmark.THUMB_MCP},
            {HandLandmark.THUMB_MCP, HandLandmark.THUMB_IP},
            {HandLandmark.THUMB_IP, HandLandmark.THUMB_TIP},
            {HandLandmark.WRIST, HandLandmark.INDEX_FINGER_MCP},
            {HandLandmark.INDEX_FINGER_MCP, HandLandmark.INDEX_FINGER_PIP},
            {HandLandmark.INDEX_FINGER_PIP, HandLandmark.INDEX_FINGER_DIP},
            {HandLandmark.INDEX_FINGER_DIP, HandLandmark.INDEX_FINGER_TIP},
            {HandLandmark.WRIST, HandLandmark.MIDDLE_FINGER_MCP},
            {HandLandmark.MIDDLE_FINGER_MCP, HandLandmark.MIDDLE_FINGER_PIP},
            {HandLandmark.MIDDLE_FINGER_PIP, HandLandmark.MIDDLE_FINGER_DIP},
            {HandLandmark.MIDDLE_FINGER_DIP, HandLandmark.MIDDLE_FINGER_TIP},
            {HandLandmark.WRIST, HandLandmark.RING_FINGER_MCP},
            {HandLandmark.RING_FINGER_MCP, HandLandmark.RING_FINGER_PIP},
            {HandLandmark.RING_FINGER_PIP, HandLandmark.RING_FINGER_DIP},
            {HandLandmark.RING_FINGER_DIP, HandLandmark.RING_FINGER_TIP},
            {HandLandmark.WRIST, HandLandmark.PINKY_MCP},
            {HandLandmark.PINKY_MCP, HandLandmark.PINKY_PIP},
            {HandLandmark.PINKY_PIP, HandLandmark.PINKY_DIP},
            {HandLandmark.PINKY_DIP, HandLandmark.PINKY_TIP},
            {HandLandmark.INDEX_FINGER_MCP, HandLandmark.MIDDLE_FINGER_MCP},
            {HandLandmark.MIDDLE_FINGER_MCP, HandLandmark.RING_FINGER_MCP},
            {HandLandmark.RING_FINGER_MCP, HandLandmark.PINKY_MCP}
    };

    // Reusable coordinate buffers
    private final float[] pointCoords = new float[LANDMARK_COUNT * 2];
    private final float[] lineCoords = new float[CONNECTIONS.length * 4];

    public HandOverlayView(Context context) {
        super(context);
        landmarkPaint = new Paint();
        connectionPaint = new Paint();
        initPaints();
        loadNailDesign(context);
    }

    public HandOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        landmarkPaint = new Paint();
        connectionPaint = new Paint();
        initPaints();
        loadNailDesign(context);
    }

    private void initPaints() {
        landmarkPaint.setColor(Color.YELLOW);
        landmarkPaint.setStyle(Paint.Style.FILL);
        landmarkPaint.setStrokeWidth(8f);

        connectionPaint.setColor(Color.GREEN);
        connectionPaint.setStyle(Paint.Style.STROKE);
        connectionPaint.setStrokeWidth(4f);

        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setAntiAlias(true);
    }

    /**
     * Set the current view mode
     * @param mode MODE_CALIBRATION or MODE_NAIL_DESIGN
     */
    public void setViewMode(int mode) {
        if (mode == MODE_CALIBRATION || mode == MODE_NAIL_DESIGN) {
            this.viewMode = mode;
            invalidate();
        }
    }

    /**
     * Get the current view mode
     * @return current view mode
     */
    public int getViewMode() {
        return viewMode;
    }

    /**
     * Load the nail design bitmap
     */
    private void loadNailDesign(Context context) {
        try {
            nailDesignBitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.naildesign);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set a custom nail design bitmap
     */
    public void setNailDesignBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            this.nailDesignBitmap = bitmap;
            invalidate();
        }
    }

    /**
     * Update the landmarks result and redraw.
     */
    public void setResults(HandLandmarkerResult results, int imageWidth, int imageHeight, boolean isFrontCamera) {
        this.results = results;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.isFrontCamera = isFrontCamera;
        updateScaleFactor();
        invalidate();
    }

    /**
     * Get current x-axis calibration offset
     */
    public float getXOffset() {
        return xOffset;
    }

    /**
     * Get current y-axis calibration offset
     */
    public float getYOffset() {
        return yOffset;
    }

    /**
     * Set calibration offsets
     */
    public void setCalibrationOffset(float xOffset, float yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        invalidate();
    }

    /**
     * Set the running mode
     */
    public void setRunningMode(RunningMode runningMode) {
        this.runningMode = runningMode;
        updateScaleFactor();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        updateScaleFactor();
    }

    private void updateScaleFactor() {
        if (imageWidth <= 0 || imageHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) {
            return;
        }

        if (runningMode == RunningMode.LIVE_STREAM) {
            scaleFactor = Math.max(viewWidth * 1f / imageWidth, viewHeight * 1f / imageHeight);
        } else {
            scaleFactor = Math.min(viewWidth * 1f / imageWidth, viewHeight * 1f / imageHeight);
        }
    }

    public void clear() {
        results = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (results == null || results.landmarks().isEmpty()) return;

        // Draw each detected hand
        List<List<NormalizedLandmark>> multiLandmarks = results.landmarks();
        for (List<NormalizedLandmark> landmarks : multiLandmarks) {
            if (landmarks.size() < LANDMARK_COUNT) continue;

            // Fill points
            for (int i = 0; i < LANDMARK_COUNT; i++) {
                float nx = landmarks.get(i).x();
                float ny = landmarks.get(i).y();
                float px = translateX(nx);
                float py = translateY(ny);
                pointCoords[2 * i] = px;
                pointCoords[2 * i + 1] = py;
            }

            // Only draw connections and landmarks in CALIBRATION mode
            if (viewMode == MODE_CALIBRATION) {
                // Draw connections
                for (int i = 0; i < CONNECTIONS.length; i++) {
                    int a = CONNECTIONS[i][0];
                    int b = CONNECTIONS[i][1];
                    int idx = 4 * i;
                    lineCoords[idx] = pointCoords[2 * a];
                    lineCoords[idx + 1] = pointCoords[2 * a + 1];
                    lineCoords[idx + 2] = pointCoords[2 * b];
                    lineCoords[idx + 3] = pointCoords[2 * b + 1];
                }
                canvas.drawLines(lineCoords, connectionPaint);

                // Draw landmarks
                canvas.drawPoints(pointCoords, landmarkPaint);
            }

            // Draw nail designs on fingertips in both modes
            if (nailDesignBitmap != null && !nailDesignBitmap.isRecycled()) {
                for (int tipIdx : FINGERTIP_LANDMARKS) {
                    float tipX = pointCoords[2 * tipIdx];
                    float tipY = pointCoords[2 * tipIdx + 1];

                    // Get DIP landmark for each finger to calculate orientation and size
                    int dipIdx = getDIPLandmarkForFingertip(tipIdx);
                    float dipX = pointCoords[2 * dipIdx];
                    float dipY = pointCoords[2 * dipIdx + 1];

                    // Calculate nail orientation and size
                    float angle = (float) Math.toDegrees(Math.atan2(tipY - dipY, tipX - dipX));
                    float distance = calculateDistance(tipX, tipY, dipX, dipY);
                    float nailWidth = distance * 0.8f;

                    // Position nail slightly before the tip to look more natural
                    float offsetRatio = 0.3f;
                    float offsetX = (dipX - tipX) * offsetRatio;
                    float offsetY = (dipY - tipY) * offsetRatio;

                    // Draw the nail design
                    drawNailDesign(canvas, tipX + offsetX, tipY + offsetY, nailWidth, angle);
                }
            }
        }
    }

    /**
     * Get the DIP (Distal Interphalangeal Joint) landmark index for a fingertip
     */
    private int getDIPLandmarkForFingertip(int tipIdx) {
        switch (tipIdx) {
            case HandLandmark.THUMB_TIP:
                return HandLandmark.THUMB_IP;
            case HandLandmark.INDEX_FINGER_TIP:
                return HandLandmark.INDEX_FINGER_DIP;
            case HandLandmark.MIDDLE_FINGER_TIP:
                return HandLandmark.MIDDLE_FINGER_DIP;
            case HandLandmark.RING_FINGER_TIP:
                return HandLandmark.RING_FINGER_DIP;
            case HandLandmark.PINKY_TIP:
                return HandLandmark.PINKY_DIP;
            default:
                return tipIdx - 1; // Fallback
        }
    }
    /**
     * Set a default calibration offset based on screen dimensions
     * This provides a reasonable starting point that works for most users
     */
    public void setDefaultCalibrationOffset() {
        // For front-facing camera, a leftward offset often helps align the skeleton
        // with real hand positions. The values below are based on typical testing.
        float defaultXOffset = -viewWidth * 0.15f; // 15% leftward shift
        float defaultYOffset = 0; // No vertical shift by defaultAC

        // Apply the default offsets
        setCalibrationOffset(defaultXOffset, defaultYOffset);

        Log.d(TAG, "Default calibration applied. X offset: " + defaultXOffset);
    }

    /**
     * Calculate distance between two points
     */
    private float calculateDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Draw nail design at specified position with proper rotation and positioning
     */
    private void drawNailDesign(Canvas canvas, float x, float y, float width, float angle) {
        // Only draw nail designs in nail design mode
        if (viewMode == MODE_CALIBRATION || nailDesignBitmap == null || nailDesignBitmap.isRecycled()) {
            return;
        }

        float originalWidth = nailDesignBitmap.getWidth();
        float originalHeight = nailDesignBitmap.getHeight();

        // Adjust scale based on finger width - make 2x bigger in nail design mode

        // scale the size of the nail
        float scale = (width / originalWidth) * 1.5f;

        // Add 90 degrees to rotate the design properly
        angle = angle - 90;

        // Position nail slightly before the tip and aligned with finger direction
        float positionOffsetX = (float) (Math.cos(Math.toRadians(angle - 90)) * width * 0.2f);
        float positionOffsetY = (float) (Math.sin(Math.toRadians(angle - 90)) * width * 0.2f);

        nailMatrix.reset();
        // Center the bitmap on the target position
        nailMatrix.postTranslate(-originalWidth / 2f, -originalHeight / 2f);
        // Scale the bitmap
        nailMatrix.postScale(scale, scale);
        // Rotate the bitmap with corrected angle
        nailMatrix.postRotate(angle);
        // Translate to the target position with offset
        nailMatrix.postTranslate(x + positionOffsetX, y + positionOffsetY);

        canvas.drawBitmap(nailDesignBitmap, nailMatrix, bitmapPaint);
    }

    private float translateX(float x) {
        if (isFrontCamera) {
            // Apply mirroring for front camera
            x = 1 - x;
        }
        // Apply calibration offset
        return (x * imageWidth * scaleFactor) + xOffset;
    }

    private float translateY(float y) {
        // Apply calibration offset
        return (y * imageHeight * scaleFactor) + yOffset;
    }
}