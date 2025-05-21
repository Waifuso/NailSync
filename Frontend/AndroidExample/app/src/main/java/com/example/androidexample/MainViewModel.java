package com.example.androidexample;

import androidx.lifecycle.ViewModel;

/**
 * ViewModel for storing hand landmarker settings.
 */
public class MainViewModel extends ViewModel {
    private int delegate = HandLandmarkHelper.DELEGATE_CPU;
    private float minHandDetectionConfidence = HandLandmarkHelper.DEFAULT_HAND_DETECTION_CONFIDENCE;
    private float minHandTrackingConfidence = HandLandmarkHelper.DEFAULT_HAND_TRACKING_CONFIDENCE;
    private float minHandPresenceConfidence = HandLandmarkHelper.DEFAULT_HAND_PRESENCE_CONFIDENCE;
    private int maxHands = HandLandmarkHelper.DEFAULT_NUM_HANDS;

    // Getters
    public int getCurrentDelegate() {
        return delegate;
    }

    public float getCurrentMinHandDetectionConfidence() {
        return minHandDetectionConfidence;
    }

    public float getCurrentMinHandTrackingConfidence() {
        return minHandTrackingConfidence;
    }

    public float getCurrentMinHandPresenceConfidence() {
        return minHandPresenceConfidence;
    }

    public int getCurrentMaxHands() {
        return maxHands;
    }

    // Setters
    public void setDelegate(int delegate) {
        this.delegate = delegate;
    }

    public void setMinHandDetectionConfidence(float confidence) {
        this.minHandDetectionConfidence = confidence;
    }

    public void setMinHandTrackingConfidence(float confidence) {
        this.minHandTrackingConfidence = confidence;
    }

    public void setMinHandPresenceConfidence(float confidence) {
        this.minHandPresenceConfidence = confidence;
    }

    public void setMaxHands(int maxHands) {
        this.maxHands = maxHands;
    }
}