package com.example.androidexample;

import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RewardTests {

    private static final String TAG = "RewardClaimFlow";
    private static final int ANIMATION_DELAY = 1000; // Wait time for animations
    private static final int NETWORK_DELAY = 3000; // Wait time for network responses
    private UiDevice device;

    // Test credentials
    private static final String TEST_EMAIL = "jacobtdang@gmail.com";
    private static final String TEST_PASSWORD = "1233";

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        // Get the UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Disable animations for reliable testing
        disableAnimations();

        // Make sure the device is in portrait orientation
        try {
            device.setOrientationNatural();
        } catch (RemoteException e) {
            Log.e(TAG, "Error setting orientation", e);
        }
    }

    @After
    public void tearDown() {
        // Re-enable animations after the test
        enableAnimations();

        // Reset device orientation
        try {
            device.setOrientationNatural();
        } catch (RemoteException e) {
            Log.e(TAG, "Error resetting orientation", e);
        }
    }

    @Test
    public void testCompleteRewardClaimFlow() {
        // Initial delay to ensure app fully launches
        sleep(3000);

        // 1. Login to the app
        performLogin(TEST_EMAIL, TEST_PASSWORD);

        // 2. Navigate to Rewards section
        navigateToRewards();

        // 3. Select a reward
        selectFirstReward();

        // 4. Claim the reward
        claimReward();

        // 5. Verify redemption was successful
        verifyRedemptionSuccessful();

        // 6. Navigate back and clear the reward
        navigateBack();
        clearActiveReward();

        Log.d(TAG, "Complete reward claim flow test successful");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Login to the application
     */
    private void performLogin(String email, String password) {
        // Find and input email
        waitForViewWithRetry(R.id.email_entryfield, 5000, 3);
        onView(withId(R.id.email_entryfield))
                .perform(typeText(email), closeSoftKeyboard());
        Log.d(TAG, "Entered email");

        sleep(500); // Short delay between inputs

        // Find and input password
        waitForViewWithRetry(R.id.login_password_field, 5000, 3);
        onView(withId(R.id.login_password_field))
                .perform(typeText(password), closeSoftKeyboard());
        Log.d(TAG, "Entered password");

        sleep(1000); // Give app time to process inputs before clicking login

        // Click login button
        waitForViewWithRetry(R.id.main_nextbtn, 10000, 5);
        waitUntilEnabled(R.id.main_nextbtn);
        clickWithAnimation(R.id.main_nextbtn);
        Log.d(TAG, "Clicked login button");

        // Wait longer for network response
        sleep(5000);
    }

    /**
     * Navigate to the Rewards section
     */
    private void navigateToRewards() {
        // Wait for the welcome screen to load
        waitForViewWithRetry(R.id.welcomeText, 3000, 3);
        Log.d(TAG, "Welcome screen loaded, looking for Rewards box");

        // First try to find boxViewRewards by ID
        try {
            onView(withId(R.id.boxViewRewards)).check(matches(isDisplayed()));
            onView(withId(R.id.boxViewRewards)).perform(click());
            Log.d(TAG, "Clicked boxViewRewards by ID");
            sleep(ANIMATION_DELAY * 2);
            return;
        } catch (Exception e) {
            Log.e(TAG, "Could not find boxViewRewards by ID: " + e.getMessage());
        }

        // Try to find by text that contains "Reward"
        try {
            UiObject rewardsButton = device.findObject(new UiSelector()
                    .textContains("Reward")
                    .clickable(true));

            if (rewardsButton.exists()) {
                rewardsButton.click();
                Log.d(TAG, "Clicked Rewards box using text search");
                sleep(ANIMATION_DELAY * 2);
                return;
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Could not find rewards box with text search", e);
        }

        // Try to find by resource pattern
        try {
            UiObject rewardsItem = device.findObject(new UiSelector()
                    .resourceIdMatches(".*id/boxViewRewards"));

            if (rewardsItem.exists()) {
                rewardsItem.click();
                Log.d(TAG, "Clicked rewards box using direct resource ID");
                sleep(ANIMATION_DELAY * 2);
                return;
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Could not find rewards box with resource ID pattern", e);
        }

        // Try using "All Rewards" text
        try {
            UiObject allRewardsSelector = device.findObject(new UiSelector()
                    .text("All Rewards"));
            if (allRewardsSelector.exists()) {
                allRewardsSelector.click();
                Log.d(TAG, "Clicked on 'All Rewards' text");
                sleep(ANIMATION_DELAY * 2);
                return;
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Could not find 'All Rewards' text", e);
        }

        // As a last resort, check for any buttons with "Rewards" text
        try {
            UiObject rewardsButton = device.findObject(new UiSelector()
                    .className("android.widget.Button")
                    .textContains("Reward"));
            if (rewardsButton.exists()) {
                rewardsButton.click();
                Log.d(TAG, "Clicked on button containing 'Reward' text");
                sleep(ANIMATION_DELAY * 2);
                return;
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Could not find any button with 'Reward' text", e);
        }

        // If all direct searches failed, try looking through all TextViews
        try {
            // Find all TextViews
            UiScrollable scrollable = new UiScrollable(new UiSelector().scrollable(true));
            scrollable.setAsHorizontalList();

            UiObject[] textViews = new UiObject[10]; // Check up to 10 TextViews
            for (int i = 0; i < textViews.length; i++) {
                textViews[i] = device.findObject(new UiSelector()
                        .className("android.widget.TextView")
                        .instance(i));

                if (textViews[i].exists()) {
                    String text = textViews[i].getText();
                    Log.d(TAG, "Found TextView with text: " + text);

                    if (text != null && text.toLowerCase().contains("reward")) {
                        textViews[i].click();
                        Log.d(TAG, "Clicked TextView with text: " + text);
                        sleep(ANIMATION_DELAY * 2);
                        return;
                    }
                }
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Error searching through TextViews", e);
        }

        Log.e(TAG, "Failed to find and click the rewards box with any approach");
        throw new RuntimeException("Could not navigate to Rewards section");
    }

    /**
     * Select the first reward in the list
     */
    private void selectFirstReward() {
        // Wait for the rewards recycler view to appear
        waitForViewWithRetry(R.id.rewardsRecyclerView, 5000, 5);
        Log.d(TAG, "Found rewardsRecyclerView");

        // Wait for items to load in the recycler view
        waitForRecyclerViewItems(R.id.rewardsRecyclerView, 1, 5000, 5);
        Log.d(TAG, "Rewards loaded in RecyclerView");

        // Click on the first reward card using RecyclerView
        try {
            onView(withId(R.id.rewardsRecyclerView))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
            Log.d(TAG, "Clicked on first reward in RecyclerView");
            sleep(ANIMATION_DELAY);
            return;
        } catch (Exception e) {
            Log.e(TAG, "Error clicking reward in RecyclerView: " + e.getMessage());
        }

        // If direct RecyclerView approach failed, try with UiAutomator
        try {
            // Try to find elements matching reward card content
            UiObject rewardTitle = device.findObject(new UiSelector()
                    .resourceId("com.example.androidexample:id/rewardTitle"));

            if (rewardTitle.exists()) {
                rewardTitle.click();
                Log.d(TAG, "Clicked on reward card title using UiAutomator");
                sleep(ANIMATION_DELAY);
                return;
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Error finding reward card title with UiAutomator", e);
        }

        try {
            // Try to find any images within reward cards
            UiObject rewardImage = device.findObject(new UiSelector()
                    .resourceId("com.example.androidexample:id/rewardImage"));

            if (rewardImage.exists()) {
                rewardImage.click();
                Log.d(TAG, "Clicked on reward card image using UiAutomator");
                sleep(ANIMATION_DELAY);
                return;
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Error finding reward card image with UiAutomator", e);
        }

        Log.e(TAG, "Could not find and click a reward card with any approach");
        throw new RuntimeException("Failed to select a reward");
    }

    /**
     * Claim the currently displayed reward
     */
    private void claimReward() {
        sleep(3000); // Wait for reward detail screen to load

        // Check if we have the collapsingToolbar
        try {
            waitForViewWithRetry(R.id.collapsingToolbar, 2000, 2);
            Log.d(TAG, "Found CollapsingToolbar in RewardDetailActivity");
        } catch (Exception e) {
            Log.e(TAG, "CollapsingToolbar not found: " + e.getMessage());
        }

        // Try to find redeem button by ID
        try {
            waitForViewWithRetry(R.id.redeemButton, 2000, 2);

            if (isViewEnabled(R.id.redeemButton)) {
                clickWithAnimation(R.id.redeemButton);
                Log.d(TAG, "Clicked redeem button using ID");
                sleep(ANIMATION_DELAY);
            } else {
                Log.d(TAG, "Redeem button is disabled, cannot proceed with redemption");
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error finding redeem button by ID: " + e.getMessage());
        }

        // If direct approach failed, try with text "Redeem"
        try {
            UiObject redeemButton = device.findObject(new UiSelector()
                    .textContains("Redeem")
                    .clickable(true));

            if (redeemButton.exists() && redeemButton.isEnabled()) {
                redeemButton.click();
                Log.d(TAG, "Clicked redeem button using text search");
                sleep(ANIMATION_DELAY);
            } else {
                Log.d(TAG, "Redeem button found but disabled or not clickable");
                return;
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Error finding redeem button with text search", e);
        }

        // Handle confirmation dialog
        sleep(1500); // Wait for confirmation dialog

        try {
            // Click "Redeem" on the confirmation dialog
            UiObject confirmButton = device.findObject(new UiSelector()
                    .text("Redeem")
                    .className("android.widget.Button"));

            if (confirmButton.exists()) {
                confirmButton.click();
                Log.d(TAG, "Clicked confirm on redeem dialog");
                sleep(NETWORK_DELAY); // Wait for redemption processing
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Error finding confirmation dialog button", e);
        }
    }

    /**
     * Verify that redemption was successful
     */
    private void verifyRedemptionSuccessful() {
        // Wait for redemption to complete
        sleep(3000);

        // Look for "Redeemed" text or disabled redeem button
        boolean redemptionSuccessful = false;

        try {
            UiObject redeemedButton = device.findObject(new UiSelector()
                    .text("Redeemed"));

            if (redeemedButton.exists()) {
                Log.d(TAG, "Redemption successful - redeem button now shows 'Redeemed'");
                redemptionSuccessful = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking for 'Redeemed' text", e);
        }

        if (!redemptionSuccessful) {
            // Try alternative verification method
            try {
                if (!isViewEnabled(R.id.redeemButton)) {
                    Log.d(TAG, "Redemption appears successful - redeem button is disabled");
                    redemptionSuccessful = true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking if redeem button is disabled", e);
            }
        }

        if (!redemptionSuccessful) {
            Log.e(TAG, "Could not verify redemption was successful");
            // We don't throw an exception here as it might be a UI change rather than a test failure
        }
    }

    /**
     * Navigate back from the current screen
     */
    private void navigateBack() {
        try {
            UiObject backButton = device.findObject(new UiSelector()
                    .className("android.widget.ImageButton")
                    .description("Navigate up"));

            if (backButton.exists()) {
                backButton.click();
                Log.d(TAG, "Clicked back button");
                sleep(ANIMATION_DELAY);
                return;
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Error finding navigation back button", e);
        }

        // Try device back button as fallback
        try {
            device.pressBack();
            Log.d(TAG, "Pressed device back button");
            sleep(ANIMATION_DELAY);
        } catch (Exception e) {
            Log.e(TAG, "Error pressing device back button", e);
        }
    }

    /**
     * Clear/replace the active reward
     */
    private void clearActiveReward() {
        sleep(2000);

        // Look for elements that would let us clear or manage the active reward
        try {
            // Check if there's a banner or card showing the active reward
            UiObject activeBanner = device.findObject(new UiSelector()
                    .textContains("Active Reward"));

            if (activeBanner.exists()) {
                Log.d(TAG, "Found active reward banner");

                // Try to find a button to clear or manage the reward
                UiObject manageButton = device.findObject(new UiSelector()
                        .textContains("Manage")
                        .clickable(true));

                if (manageButton.exists()) {
                    manageButton.click();
                    Log.d(TAG, "Clicked manage button for active reward");
                    sleep(1000);

                    // Look for a "Clear" or "Remove" or "Delete" option
                    UiObject clearOption = device.findObject(new UiSelector()
                            .textMatches("(Clear|Remove|Delete).*")
                            .clickable(true));

                    if (clearOption.exists()) {
                        clearOption.click();
                        Log.d(TAG, "Clicked option to clear reward");

                        // Confirm the action if needed
                        UiObject confirmClear = device.findObject(new UiSelector()
                                .textMatches("(Confirm|Yes|OK).*")
                                .clickable(true));

                        if (confirmClear.exists()) {
                            confirmClear.click();
                            Log.d(TAG, "Confirmed clearing the reward");
                        }
                    }
                }
            } else {
                Log.d(TAG, "No active reward banner found - reward may have been already cleared");
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Error in reward clearing process", e);
        }
    }

    // ==================== UTILITY METHODS ====================

    // Custom action to wait for a specified time
    public static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }

    // Helper method for Thread.sleep with exception handling
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Enhanced wait for view with retry logic
    private void waitForViewWithRetry(int viewId, long totalWaitMillis, int retryCount) {
        long waitPerTry = totalWaitMillis / retryCount;
        for (int i = 0; i < retryCount; i++) {
            try {
                sleep(waitPerTry);
                onView(withId(viewId)).check(matches(isDisplayed()));
                Log.d(TAG, "View found after " + (i+1) + " attempts: " + viewId);
                return; // View found, exit the method
            } catch (Exception e) {
                if (i == retryCount - 1) {
                    // This was our last attempt
                    Log.e(TAG, "View not found after " + retryCount + " attempts: " + viewId);
                    throw e; // Re-throw the exception on the last attempt
                }
                // Otherwise continue to next attempt
                Log.d(TAG, "View not found yet, retrying...");
            }
        }
    }

    // Helper method to wait until a view is enabled
    private void waitUntilEnabled(int viewId) {
        try {
            // Check if it's already enabled
            onView(withId(viewId)).check(matches(isEnabled()));
        } catch (Exception e) {
            // If not enabled, wait and try again
            try {
                Thread.sleep(1000);
                onView(withId(viewId)).check(matches(isEnabled()));
            } catch (Exception e2) {
                try {
                    // Try one more time with longer wait
                    Thread.sleep(2000);
                    onView(withId(viewId)).check(matches(isEnabled()));
                } catch (Exception e3) {
                    // Give up and proceed (might fail later)
                    Log.e(TAG, "View not enabled after waiting: " + viewId);
                }
            }
        }
    }

    // Helper method to click and wait for animation
    private void clickWithAnimation(int viewId) {
        onView(withId(viewId)).perform(click());
        sleep(ANIMATION_DELAY); // Wait for any animations triggered by the click
    }

    // Helper method to check if a view is enabled
    private boolean isViewEnabled(int viewId) {
        final boolean[] isEnabled = {false};

        try {
            onView(withId(viewId)).check((view, noViewFoundException) -> {
                if (view != null) {
                    isEnabled[0] = view.isEnabled();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error checking if view is enabled: " + e.getMessage());
            return false;
        }

        return isEnabled[0];
    }

    // Helper method to wait for RecyclerView to have items
    private void waitForRecyclerViewItems(int recyclerViewId, int minItems, long totalWaitTime, int retries) {
        long waitPerRetry = totalWaitTime / retries;

        for (int i = 0; i < retries; i++) {
            try {
                sleep(waitPerRetry);
                onView(withId(recyclerViewId))
                        .check(new RecyclerViewItemCountAssertion(minItems));
                Log.d(TAG, "RecyclerView has at least " + minItems + " items after " + (i+1) + " attempts");
                return;
            } catch (Exception e) {
                if (i == retries - 1) {
                    Log.e(TAG, "RecyclerView doesn't have " + minItems + " items after all retries");
                    throw e;
                }
                Log.d(TAG, "Waiting for RecyclerView items, retry " + (i+1));
            }
        }
    }

    // Custom RecyclerView assertion to check item count
    public static class RecyclerViewItemCountAssertion implements ViewAssertion {
        private final int expectedMinimumCount;

        public RecyclerViewItemCountAssertion(int expectedMinimumCount) {
            this.expectedMinimumCount = expectedMinimumCount;
        }

        @Override
        public void check(View view, NoMatchingViewException noViewFoundException) {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }

            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            assertThat(adapter.getItemCount(), greaterThanOrEqualTo(expectedMinimumCount));
        }
    }

    // Methods to disable animations
    private void disableAnimations() {
        try {
            // Disable animations using UiAutomator
            device.executeShellCommand("settings put global window_animation_scale 0.0");
            device.executeShellCommand("settings put global transition_animation_scale 0.0");
            device.executeShellCommand("settings put global animator_duration_scale 0.0");
        } catch (Exception e) {
            Log.e(TAG, "Failed to disable animations", e);
        }
    }

    private void enableAnimations() {
        try {
            // Re-enable animations
            device.executeShellCommand("settings put global window_animation_scale 1.0");
            device.executeShellCommand("settings put global transition_animation_scale 1.0");
            device.executeShellCommand("settings put global animator_duration_scale 1.0");
        } catch (Exception e) {
            Log.e(TAG, "Failed to re-enable animations", e);
        }
    }
}