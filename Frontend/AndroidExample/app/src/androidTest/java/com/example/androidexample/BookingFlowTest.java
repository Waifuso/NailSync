package com.example.androidexample;

import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

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
import androidx.test.uiautomator.UiSelector;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BookingFlowTest {

    private UiDevice device;
    private static final int ANIMATION_DELAY = 1000; // Wait time for animations

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
            Log.e("JacobSystemTest", "Error setting orientation", e);
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
            Log.e("JacobSystemTest", "Error resetting orientation", e);
        }
    }

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

    // Custom action for measured, incremental scrolling with visual feedback
    public static ViewAction scrollDownSlowly(final int scrolls, final int pixelsPerScroll) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Scroll down slowly " + scrolls + " times, " + pixelsPerScroll + " pixels at a time";
            }

            @Override
            public void perform(UiController uiController, View view) {
                for (int i = 0; i < scrolls; i++) {
                    onView(isRoot()).perform(swipeUp());
                    uiController.loopMainThreadForAtLeast(500); // Increased pause between scrolls
                }
            }
        };
    }

    @Test
    public void testBookingFlow() {
        // Initial delay to ensure app fully launches
        sleep(3000);

        // Find and input email
        waitForViewWithRetry(R.id.email_entryfield, 5000, 3);
        onView(withId(R.id.email_entryfield))
                .perform(typeText("phongdz@gmail.com"), closeSoftKeyboard());
        Log.d("JacobSystemTest", "Entered email");

        sleep(500); // Short delay between inputs

        // Find and input password
        waitForViewWithRetry(R.id.login_password_field, 5000, 3);
        onView(withId(R.id.login_password_field))
                .perform(typeText("1233"), closeSoftKeyboard());
        Log.d("JacobSystemTest", "Entered password");

        sleep(1000); // Give app time to process inputs before clicking login

        // Set bypassLogin to false so the actual network call is made
        // (This will work automatically since we're not modifying the app's code)

        // Wait for login button to appear and then click it
        waitForViewWithRetry(R.id.main_nextbtn, 10000, 5);
        waitUntilEnabled(R.id.main_nextbtn);
        clickWithAnimation(R.id.main_nextbtn);
        Log.d("JacobSystemTest", "Clicked login button");

        // Wait longer for network response
        sleep(5000);

        // 2. In ApplicationActivity - find and click the booking box
        waitForViewWithRetry(R.id.welcomeText, 1000, 3);
        Log.d("JacobSystemTest", "Welcome screen loaded, looking for Book Appointment");

        // Try a systematic approach to find the Book Appointment button
        waitForViewWithRetry(R.id.boxBookAppointment, 1000, 2);
        clickWithAnimation(R.id.boxBookAppointment);
        Log.d("JacobSystemTest", "Clicked book appointment box");

        // Wait for screen transition animation
        sleep(ANIMATION_DELAY);

        // 3. In SelectNailTechActivity - select a technician
        waitForViewWithRetry(R.id.recyclerViewTechnicians, 1000, 3);
        sleep(1000);  // Longer wait for recyclerview to be populated with API data

        // Try to use the "Book Any Technician" button first
        boolean technicianSelected = false;

        try {
            waitForViewWithRetry(R.id.btnBookAnyTechnician, 4000, 2);
            clickWithAnimation(R.id.btnBookAnyTechnician);
            Log.d("JacobSystemTest", "Clicked on btnBookAnyTechnician");
            technicianSelected = true;
        } catch (Exception e) {
            Log.e("JacobSystemTest", "Failed to click btnBookAnyTechnician", e);
        }

        // If we couldn't use the "Any Technician" button, try using the recycler view
        if (!technicianSelected) {
            try {
                // Try UiAutomator to find the "Book Now" text on any book button
                for (int i = 0; i < 3; i++) {
                    sleep(1500); // More time between attempts
                    try {
                        UiObject bookNowBtn = device.findObject(new UiSelector().text("Book Now"));
                        if (bookNowBtn.exists()) {
                            bookNowBtn.click();
                            // Wait after clicking to allow for any animations/state changes
                            sleep(ANIMATION_DELAY);
                            Log.d("JacobSystemTest", "Clicked 'Book Now' button using UiAutomator");
                            technicianSelected = true;
                            break;
                        }
                    } catch (UiObjectNotFoundException e) {
                        Log.e("JacobSystemTest", "Could not find 'Book Now' on try " + i, e);
                    }
                }
            } catch (Exception e) {
                Log.e("JacobSystemTest", "Error in UiAutomator approach", e);
            }
        }

        // Final attempt using RecyclerView if needed
        if (!technicianSelected) {
            try {
                // Check if RecyclerView has items
                waitForRecyclerViewItems(R.id.recyclerViewTechnicians, 1, 6000, 6);

                Log.d("JacobSystemTest", "Attempting to click on first technician in recycler view");
                onView(withId(R.id.recyclerViewTechnicians))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

                // Wait for any animations after item selection
                sleep(ANIMATION_DELAY);

                waitForViewWithRetry(R.id.btnBookTechnician, 3000, 3);
                clickWithAnimation(R.id.btnBookTechnician);
                Log.d("JacobSystemTest", "Clicked on book button for first technician");
                technicianSelected = true;
            } catch (Exception e) {
                Log.e("JacobSystemTest", "Failed to click on technician from RecyclerView", e);
            }
        }

        // Wait for screen transition animation regardless of which approach worked
        sleep(ANIMATION_DELAY * 2);

        // 4. In SelectBookingDate - select a date and continue
        waitForViewWithRetry(R.id.calendarGrid, 1000, 3);
        sleep(1000);  // Extra wait for calendar to be populated & animations to complete

        try {
            // Click anywhere in the calendar grid to select a date
            for (int i = 0; i < 3; i++) {
                try {
                    onView(withId(R.id.calendarGrid)).perform(click());
                    Log.d("JacobSystemTest", "Clicked on calendarGrid");

                    // Wait for selection animation
                    sleep(ANIMATION_DELAY);
                    break;
                } catch (Exception e) {
                    if (i == 2) throw e;
                    sleep(1000);
                }
            }
        } catch (Exception e) {
            Log.e("JacobSystemTest", "Failed to click on calendarGrid", e);
        }

        waitUntilEnabledWithRetry(R.id.btnContinue, 5, 1000);
        clickWithAnimation(R.id.btnContinue);
        Log.d("JacobSystemTest", "Clicked continue to proceed to service selection");

        // Wait for screen transition animation
        sleep(ANIMATION_DELAY * 2);

        // 5. In SelectServiceActivity - select a service and continue
        waitForViewWithRetry(R.id.servicesRecyclerView, 1000, 4);
        sleep(1000);  // Extended wait for services to populate from API

        // Check if RecyclerView has items before trying to interact
        waitForRecyclerViewItems(R.id.servicesRecyclerView, 1, 2000, 6);

        // Try clicking on the service selector with multiple approaches
        boolean serviceSelected = false;

        // First approach - try multiple strategies to select the service
        for (int attempt = 0; attempt < 3 && !serviceSelected; attempt++) {
            try {
                switch (attempt) {
                    case 0:
                        // First attempt - direct click on serviceSelector
                        try {
                            Log.d("JacobSystemTest", "Attempt 1: Trying direct selector click");
                            onView(allOf(
                                    withId(R.id.serviceSelector),
                                    isDescendantOfA(withId(R.id.servicesRecyclerView))
                            )).perform(click());
                            serviceSelected = true;
                        } catch (Exception e) {
                            Log.e("JacobSystemTest", "Direct selector click failed", e);
                        }
                        break;

                    case 1:
                        // Second attempt - custom ViewAction
                        try {
                            Log.d("JacobSystemTest", "Attempt 2: Trying custom ViewAction");
                            clickOnViewInRecyclerViewItem(R.id.servicesRecyclerView, 0, R.id.serviceSelector);
                            serviceSelected = true;
                        } catch (Exception e) {
                            Log.e("JacobSystemTest", "Custom ViewAction failed", e);
                        }
                        break;

                    case 2:
                        // Third attempt - UiAutomator
                        try {
                            Log.d("JacobSystemTest", "Attempt 3: Trying UiAutomator");
                            UiSelector selector = new UiSelector()
                                    .resourceId("com.example.androidexample:id/serviceSelector");
                            UiObject selectorObj = device.findObject(selector);

                            if (selectorObj.exists()) {
                                selectorObj.click();
                                serviceSelected = true;
                            } else {
                                Log.e("JacobSystemTest", "Selector not found with UiAutomator");

                                // Fallback to clicking the entire first item
                                Log.d("JacobSystemTest", "Last resort: Clicking entire first item");
                                onView(withId(R.id.servicesRecyclerView))
                                        .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
                                serviceSelected = true;
                            }
                        } catch (Exception e) {
                            Log.e("JacobSystemTest", "UiAutomator approach failed", e);
                        }
                        break;
                }

                if (serviceSelected) {
                    Log.d("JacobSystemTest", "Successfully selected service on attempt " + (attempt + 1));

                    // Wait for any animations after selection (expand/collapse, highlighting)
                    sleep(ANIMATION_DELAY);
                    break;
                }

                // Wait between attempts
                sleep(1500);
            } catch (Exception e) {
                Log.e("JacobSystemTest", "Exception during service selection attempt " + (attempt + 1), e);
            }
        }

        // Verify service was selected by checking if next button is enabled
        waitUntilEnabledWithRetry(R.id.nextButton, 6, 1500);

        // If button not enabled, try clicking service again
        try {
            onView(withId(R.id.nextButton)).check(matches(isEnabled()));
        } catch (Exception e) {
            Log.e("JacobSystemTest", "Next button not enabled, trying service selection again");

            // Last attempt with more direct approach
            try {
                onView(withId(R.id.servicesRecyclerView))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
                sleep(ANIMATION_DELAY);  // Wait for animations
            } catch (Exception ex) {
                Log.e("JacobSystemTest", "Final service selection attempt failed", ex);
            }
        }

        // Click next button with retry
        for (int i = 0; i < 3; i++) {
            try {
                clickWithAnimation(R.id.nextButton);
                Log.d("JacobSystemTest", "Clicked next to proceed to time selection");
                break;
            } catch (Exception e) {
                if (i == 2) {
                    Log.e("JacobSystemTest", "Failed to click next button after all retries", e);
                    throw e;
                }
                sleep(1500);
            }
        }

        // Wait for screen transition animation
        sleep(ANIMATION_DELAY * 2);

        // 6. In SelectBookingTimeActivity - select a time slot and book
        waitForViewWithRetry(R.id.timeSlotsGrid, 2000, 4);
        sleep(2000);  // Extended wait for time slots to be populated with API data

// Try multiple approaches to select a time slot
        boolean timeSlotSelected = false;

// Approach 1: Try to find and click a time slot button with UiAutomator
        try {
            Log.d("JacobSystemTest", "Attempting to find a time slot button with UiAutomator");

            // Look for any Button within the timeSlotsGrid
            UiSelector timeButtonSelector = new UiSelector()
                    .className("android.widget.Button")
                    .clickable(true);

            UiObject timeButton = device.findObject(timeButtonSelector);
            if (timeButton.exists() && timeButton.isEnabled()) {
                timeButton.click();
                Log.d("JacobSystemTest", "Clicked time slot button using UiAutomator");
                timeSlotSelected = true;
                sleep(ANIMATION_DELAY); // Wait for selection visual feedback
            }
        } catch (Exception e) {
            Log.e("JacobSystemTest", "Failed to select time with UiAutomator", e);
        }

// Approach 2: If UiAutomator failed, try direct click on the grid with adjustment
        if (!timeSlotSelected) {
            try {
                Log.d("JacobSystemTest", "Attempting direct click on timeSlotsGrid with coordinates");

                // Try clicking at various positions within the grid to find a button
                onView(withId(R.id.timeSlotsGrid)).perform(new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isDisplayed();
                    }

                    @Override
                    public String getDescription() {
                        return "Click in the center of the time grid";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        // Get the dimensions of the grid
                        int width = view.getWidth();
                        int height = view.getHeight();

                        // Try clicking at 1/4, 1/2, and 3/4 positions to find buttons
                        float[][] clickPositions = {
                                {0.25f, 0.25f}, {0.5f, 0.25f}, {0.75f, 0.25f},
                                {0.25f, 0.5f}, {0.5f, 0.5f}, {0.75f, 0.5f}
                        };

                        for (float[] pos : clickPositions) {
                            int x = (int)(width * pos[0]);
                            int y = (int)(height * pos[1]);

                            // Click at this position
                            view.performClick();
                            uiController.loopMainThreadForAtLeast(500);

                            // Try to find a child view at this position
                            View childAtPosition = findChildAtPosition(view, x, y);
                            if (childAtPosition != null && childAtPosition.isClickable()) {
                                childAtPosition.performClick();
                                uiController.loopMainThreadForAtLeast(500);
                                return; // Exit on first successful click
                            }
                        }

                        // If no clickable child found, just click in the center
                        view.performClick();
                    }

                    private View findChildAtPosition(View parent, int x, int y) {
                        if (parent instanceof ViewGroup) {
                            ViewGroup viewGroup = (ViewGroup) parent;
                            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                                View child = viewGroup.getChildAt(i);
                                if (child.getVisibility() == View.VISIBLE &&
                                        x >= child.getLeft() && x <= child.getRight() &&
                                        y >= child.getTop() && y <= child.getBottom()) {
                                    return child;
                                }
                            }
                        }
                        return null;
                    }
                });

                Log.d("JacobSystemTest", "Performed grid click with coordinates");
                timeSlotSelected = true;
                sleep(ANIMATION_DELAY);
            } catch (Exception e) {
                Log.e("JacobSystemTest", "Failed to click within timeSlotsGrid", e);
            }
        }

// Approach 3: Fall back to using the alternative time entry method
        if (!timeSlotSelected) {
            try {
                Log.d("JacobSystemTest", "Attempting to use preferred time entry mode");

                // Switch to preferred time entry mode
                onView(withId(R.id.tvEnterPreferredTime)).perform(click());
                sleep(ANIMATION_DELAY);

                // Enter a valid time (e.g., "14:30")
                onView(withId(R.id.etPreferredTime))
                        .perform(typeText("14:30"), closeSoftKeyboard());

                Log.d("JacobSystemTest", "Entered preferred time");
                timeSlotSelected = true;
                sleep(ANIMATION_DELAY);
            } catch (Exception e) {
                Log.e("JacobSystemTest", "Failed to use preferred time entry", e);
            }
        }

// Check if button is enabled before proceeding
        waitUntilEnabledWithRetry(R.id.btnBookAppointment, 5, 1000);
        clickWithAnimation(R.id.btnBookAppointment);
        Log.d("JacobSystemTest", "Clicked book appointment button");

        // 7. In BookingConfirmationActivity - verify we reached the confirmation screen
        waitForViewWithRetry(R.id.tvConfirmationTitle, 1000, 4);
        onView(withId(R.id.tvConfirmationTitle))
                .check(matches(isDisplayed()));
        Log.d("JacobSystemTest", "Confirmed we're on the booking confirmation screen");

        sleep(1500); // Wait before final click
        clickWithAnimation(R.id.btnBackToHome);
        Log.d("JacobSystemTest", "Clicked back to home to complete the flow");
    }

    // Helper method to click and wait for animation
    private void clickWithAnimation(int viewId) {
        onView(withId(viewId)).perform(click());
        sleep(ANIMATION_DELAY); // Wait for any animations triggered by the click
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
                Log.d("JacobSystemTest", "View found after " + (i+1) + " attempts: " + viewId);
                return; // View found, exit the method
            } catch (Exception e) {
                if (i == retryCount - 1) {
                    // This was our last attempt
                    Log.e("JacobSystemTest", "View not found after " + retryCount + " attempts: " + viewId);
                    throw e; // Re-throw the exception on the last attempt
                }
                // Otherwise continue to next attempt
                Log.d("JacobSystemTest", "View not found yet, retrying...");
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
                    Log.e("JacobSystemTest", "View not enabled after waiting: " + viewId);
                }
            }
        }
    }

    // Enhanced version of waitUntilEnabled with better retry handling
    private void waitUntilEnabledWithRetry(int viewId, int maxRetries, long waitBetweenRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                onView(withId(viewId)).check(matches(isEnabled()));
                Log.d("JacobSystemTest", "View " + viewId + " is enabled");
                return; // Success, view is enabled
            } catch (Exception e) {
                if (i == maxRetries - 1) {
                    Log.e("JacobSystemTest", "View " + viewId + " not enabled after " + maxRetries + " retries");
                    // Continue anyway, we'll catch any further issues when trying to interact
                    return;
                }
                Log.d("JacobSystemTest", "View " + viewId + " not enabled yet, retry " + (i+1));
                sleep(waitBetweenRetries);
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

    // Helper method to wait for RecyclerView to have items
    private void waitForRecyclerViewItems(int recyclerViewId, int minItems, long totalWaitTime, int retries) {
        long waitPerRetry = totalWaitTime / retries;

        for (int i = 0; i < retries; i++) {
            try {
                sleep(waitPerRetry);
                onView(withId(recyclerViewId))
                        .check(new RecyclerViewItemCountAssertion(minItems));
                Log.d("JacobSystemTest", "RecyclerView has at least " + minItems + " items after " + (i+1) + " attempts");
                return;
            } catch (Exception e) {
                if (i == retries - 1) {
                    Log.e("JacobSystemTest", "RecyclerView doesn't have " + minItems + " items after all retries");
                    throw e;
                }
                Log.d("JacobSystemTest", "Waiting for RecyclerView items, retry " + (i+1));
            }
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
            Log.e("JacobSystemTest", "Failed to disable animations", e);
        }
    }

    private void enableAnimations() {
        try {
            // Re-enable animations
            device.executeShellCommand("settings put global window_animation_scale 1.0");
            device.executeShellCommand("settings put global transition_animation_scale 1.0");
            device.executeShellCommand("settings put global animator_duration_scale 1.0");
        } catch (Exception e) {
            Log.e("JacobSystemTest", "Failed to re-enable animations", e);
        }
    }

    // Helper method to click on a specific view within a RecyclerView item
    private void clickOnViewInRecyclerViewItem(int recyclerViewId, int itemPosition, int targetViewId) {
        try {
            onView(withId(recyclerViewId))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(itemPosition, new ViewAction() {
                        @Override
                        public Matcher<View> getConstraints() {
                            return isEnabled();
                        }

                        @Override
                        public String getDescription() {
                            return "Click on view with id: " + targetViewId;
                        }

                        @Override
                        public void perform(UiController uiController, View view) {
                            View targetView = view.findViewById(targetViewId);
                            if (targetView != null) {
                                targetView.performClick();
                                Log.d("JacobSystemTest", "Clicked on view with id: " + targetViewId);

                                // Wait in the UI thread for any immediate animations
                                uiController.loopMainThreadForAtLeast(500);
                            } else {
                                Log.e("JacobSystemTest", "Could not find view with id: " + targetViewId);
                            }
                        }
                    }));
        } catch (Exception e) {
            Log.e("JacobSystemTest", "Failed to click on view in RecyclerView", e);
        }
    }
}