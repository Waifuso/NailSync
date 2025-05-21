package com.example.androidexample;

import android.graphics.PointF;
import android.util.Log;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmark;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for working with MediaPipe hand landmarks
 */
public class HandLandmarkHelper {
    private static final String TAG = "HandLandmarkHelper";
    public static final int DELEGATE_CPU = 0;
    public static final int DELEGATE_GPU = 1;

    public static final float DEFAULT_HAND_DETECTION_CONFIDENCE = 0.5f;
    public static final float DEFAULT_HAND_TRACKING_CONFIDENCE = 0.5f;
    public static final float DEFAULT_HAND_PRESENCE_CONFIDENCE = 0.5f;
    public static final int DEFAULT_NUM_HANDS = 1;

    /**
     * Get the fingertip landmark indices
     * @return array of fingertip landmark indices (thumb, index, middle, ring, pinky)
     */
    public static int[] getFingertipIndices() {
        return new int[] {
                HandLandmark.THUMB_TIP,        // 4
                HandLandmark.INDEX_FINGER_TIP, // 8
                HandLandmark.MIDDLE_FINGER_TIP, // 12
                HandLandmark.RING_FINGER_TIP,  // 16
                HandLandmark.PINKY_TIP         // 20
        };
    }

    /**
     * Get the joint before a fingertip (useful for orientation)
     * @param fingertipIndex the landmark index of the fingertip
     * @return the landmark index of the joint before the fingertip
     */
    public static int getJointBeforeFingertip(int fingertipIndex) {
        return fingertipIndex - 1;
    }

    /**
     * Calculate the angle of a finger in degrees
     * @param landmarks list of hand landmarks
     * @param fingertipIndex the landmark index of the fingertip
     * @return the angle in degrees (0-360)
     */
    public static float calculateFingerAngle(List<PointF> landmarks, int fingertipIndex) {
        if (landmarks == null || landmarks.size() <= fingertipIndex) {
            return 0f;
        }

        // Get fingertip and the joint before it
        PointF tip = landmarks.get(fingertipIndex);
        PointF joint = landmarks.get(getJointBeforeFingertip(fingertipIndex));

        // Calculate angle
        float dx = tip.x - joint.x;
        float dy = tip.y - joint.y;

        return (float) Math.toDegrees(Math.atan2(dy, dx)) + 90; // +90 to align with finger
    }

    /**
     * Estimate the width of a finger for proper nail scaling
     * @param landmarks list of hand landmarks
     * @param fingertipIndex the landmark index of the fingertip
     * @param viewWidth the width of the view in pixels
     * @return estimated width in pixels for the nail image
     */
    public static float estimateFingerWidth(List<PointF> landmarks, int fingertipIndex, int viewWidth) {
        if (landmarks == null || landmarks.size() <= fingertipIndex) {
            return 40f; // Default value
        }

        // Different scaling factors based on finger type
        float scaleFactor;
        if (fingertipIndex == HandLandmark.THUMB_TIP) {
            scaleFactor = 0.12f; // Thumb is wider
        } else if (fingertipIndex == HandLandmark.PINKY_TIP) {
            scaleFactor = 0.07f; // Pinky is narrower
        } else {
            scaleFactor = 0.09f; // Medium for other fingers
        }

        return viewWidth * scaleFactor;
    }

    /**
     * Check if a hand is likely facing the camera (palm visible)
     * @param landmarks list of hand landmarks
     * @return true if palm is likely visible
     */
    public static boolean isPalmVisible(List<PointF> landmarks) {
        if (landmarks == null || landmarks.size() < 21) {
            return false;
        }

        // This is a simple heuristic - in reality you might want something more robust
        // We check if the thumb is to the left of the index finger (for right hand)
        // or to the right (for left hand)
        PointF wrist = landmarks.get(HandLandmark.WRIST);
        PointF thumb = landmarks.get(HandLandmark.THUMB_TIP);
        PointF index = landmarks.get(HandLandmark.INDEX_FINGER_TIP);
        PointF pinky = landmarks.get(HandLandmark.PINKY_TIP);

        // Determine if it's likely a right hand or left hand
        boolean isRightHand = (index.x - pinky.x) > 0;

        if (isRightHand) {
            // For right hand, thumb should be to the left of index finger when palm is visible
            return thumb.x < index.x;
        } else {
            // For left hand, thumb should be to the right of index finger when palm is visible
            return thumb.x > index.x;
        }
    }

    /**
     * Get the normalized depth of a landmark (z-value)
     * Note: MediaPipe hand landmark z values are in an arbitrary scale, not real-world units
     * @param result the hand landmarker result
     * @param landmarkIndex the index of the landmark
     * @return the normalized z value, or 0 if not available
     */
    public static float getLandmarkDepth(HandLandmarkerResult result, int landmarkIndex) {
        if (result == null || result.landmarks().isEmpty() ||
                result.landmarks().get(0).size() <= landmarkIndex) {
            return 0f;
        }

        // The z coordinate represents depth in an arbitrary scale
        return result.worldLandmarks().get(0).get(landmarkIndex).z();
    }

    /**
     * Convert NormalizedLandmarks to PointF list for easier handling
     * @param result the hand landmarker result
     * @return List of PointF objects, or empty list if no hand detected
     */
    public static List<PointF> convertLandmarksToPointF(HandLandmarkerResult result) {
        List<PointF> points = new ArrayList<>();

        if (result == null || result.landmarks().isEmpty()) {
            return points;
        }

        List<NormalizedLandmark> landmarks = result.landmarks().get(0);
        for (NormalizedLandmark landmark : landmarks) {
            points.add(new PointF(landmark.x(), landmark.y()));
        }

        return points;
    }

    /**
     * Log all landmark positions for debugging
     * @param result the hand landmarker result
     */
    public static void logLandmarkPositions(HandLandmarkerResult result) {
        if (result == null || result.landmarks().isEmpty()) {
            Log.d(TAG, "No hand landmarks detected");
            return;
        }

        List<NormalizedLandmark> landmarks = result.landmarks().get(0);
        for (int i = 0; i < landmarks.size(); i++) {
            NormalizedLandmark point = landmarks.get(i);
            Log.d(TAG, "Landmark " + i + ": x=" + point.x() + ", y=" + point.y());
        }
    }
}
