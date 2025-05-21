package com.example.androidUI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

/**
 * Helper class for smooth item animations in RecyclerView.
 */
public class ItemAnimationHelper {

    /**
     * Animates the expansion or collapse of a view.
     *
     * @param view       The view to animate.
     * @param expand     True to expand, false to collapse.
     * @param duration   Animation duration in milliseconds.
     * @param onComplete Optional callback to execute when animation completes.
     */
    public static void animateViewHeight(final View view, final boolean expand, int duration, Runnable onComplete) {
        // Calculate target height before making any changes to the view
        final int targetHeight;

        if (expand) {
            // Save current height and visibility state
            int currentHeight = view.getHeight();
            int currentVisibility = view.getVisibility();

            // Temporarily set visibility to VISIBLE if it's not
            boolean wasVisible = currentVisibility == View.VISIBLE;
            if (!wasVisible) {
                view.setVisibility(View.VISIBLE);
            }

            // Set to wrap content temporarily just to measure
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            int originalHeight = lp.height;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.setLayoutParams(lp);

            // Force layout and measure
            view.measure(
                    View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            targetHeight = view.getMeasuredHeight();

            // Restore original state before starting animation
            lp.height = originalHeight;
            view.setLayoutParams(lp);

            if (!wasVisible) {
                view.setVisibility(currentVisibility);
            }

            // Now prepare for animation - set to 0 height and make visible
            if (currentVisibility != View.VISIBLE) {
                lp.height = 0;
                view.setLayoutParams(lp);
                view.setVisibility(View.VISIBLE);
                view.setAlpha(0f);
            }
        } else {
            // For collapse, target height is always 0
            targetHeight = 0;
        }

        // Get current height as starting point
        final int startHeight = view.getHeight();

        // Skip animation if there's no change
        if (startHeight == targetHeight) {
            // Just trigger completion callback if provided
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        // Create animator
        final ValueAnimator animator = ValueAnimator.ofInt(startHeight, targetHeight);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(duration);

        animator.addUpdateListener(animation -> {
            // Update height
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = value;
            view.setLayoutParams(layoutParams);

            // Update alpha for smoother visual
            if (expand) {
                float fraction = animation.getAnimatedFraction();
                view.setAlpha(fraction);
            } else {
                float fraction = 1f - animation.getAnimatedFraction();
                view.setAlpha(fraction);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            private boolean isCanceled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                isCanceled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isCanceled) return;

                // Update final state
                if (expand) {
                    // After expanding, set height to wrap_content
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    view.setLayoutParams(layoutParams);
                    view.setAlpha(1f);
                } else {
                    // After collapsing, hide the view
                    view.setVisibility(View.GONE);
                }

                // Execute callback
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });

        animator.start();
    }
}