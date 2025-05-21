package com.example.androidUI;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

/**
 * Helper class to create points animations and visual feedback
 */
public class PointsAnimationHelper {

    /**
     * Animate a points change with a counting animation and color highlight
     * @param textView The TextView to animate
     * @param startValue Starting points value
     * @param endValue Ending points value
     */
    public static void animatePointsChange(TextView textView, int startValue, int endValue) {
        if (textView == null) return;

        Context context = textView.getContext();

        // Save original text color
        final int originalColor = textView.getCurrentTextColor();

        // Set color based on increase/decrease
        final int animationColor = (endValue > startValue)
                ? Color.parseColor("#4CAF50")  // Green for increase
                : Color.parseColor("#F44336"); // Red for decrease

        // Set up value animator for counting effect
        ValueAnimator animator = ValueAnimator.ofInt(startValue, endValue);
        animator.setDuration(1500); // 1.5 second duration
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            textView.setText(String.format("%,d", animatedValue));
            textView.setTextColor(animationColor);
        });

        // Start the animation
        animator.start();

        // Reset to original color after animation
        new Handler().postDelayed(() -> textView.setTextColor(originalColor), 2000);
    }

    /**
     * Show a temporary points notification message
     * @param textView The TextView to show the message in
     * @param points The points amount that changed
     * @param isRedemption True if this was a redemption (points decrease)
     */
    @SuppressLint("SetTextI18n")
    public static void showPointsNotification(TextView textView, int points, boolean isRedemption) {
        if (textView == null) return;

        // Save original text
        final CharSequence originalText = textView.getText();

        // Build notification message
        String pointsMessage = isRedemption
                ? "- " + String.format("%,d", points) + " Points Redeemed!"
                : "+ " + String.format("%,d", points) + " Points Earned!";

        // Set message and highlight color
        textView.setText(pointsMessage);
        textView.setTextColor(isRedemption
                ? Color.parseColor("#F44336") // Red for redemption
                : Color.parseColor("#4CAF50")); // Green for earning

        // Animate text
        textView.setAlpha(0f);
        textView.setVisibility(View.VISIBLE);
        textView.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        // Reset after delay
        new Handler().postDelayed(() -> {
            textView.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        textView.setText(originalText);
                        textView.setTextColor(Color.BLACK); // Default color
                        textView.setAlpha(1f);
                    })
                    .start();
        }, 2000);
    }

    /**
     * Highlight a view briefly to draw attention to it
     * @param view The View to highlight
     */
    public static void highlightView(View view) {
        if (view == null) return;

        view.setScaleX(1f);
        view.setScaleY(1f);

        view.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(200)
                                .start())
                .start();
    }
}