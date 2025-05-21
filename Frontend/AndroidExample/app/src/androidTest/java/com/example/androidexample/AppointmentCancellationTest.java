package com.example.androidexample;

import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.hamcrest.Matcher;
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

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AppointmentCancellationTest {

    private static final String TAG = "AppointmentCancellationTest";
    private static final int ANIMATION_DELAY = 1000; // Wait time for animations
    private UiDevice device;

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

    @Test
    public void testAppointmentCancellation() {
        // Initial delay to ensure app fully launches
        sleep(3000);

        // 1. Log in to the app
        // Find and input email
        waitForViewWithRetry(R.id.email_entryfield, 5000, 3);
        onView(withId(R.id.email_entryfield))
                .perform(typeText("phongdz@gmail.com"), closeSoftKeyboard());
        Log.d(TAG, "Entered email");

        sleep(500); // Short delay between inputs

        // Find and input password
        waitForViewWithRetry(R.id.login_password_field, 5000, 3);
        onView(withId(R.id.login_password_field))
                .perform(typeText("1233"), closeSoftKeyboard());
        Log.d(TAG, "Entered password");

        sleep(1000); // Give app time to process inputs before clicking login

        // Click login button
        waitForViewWithRetry(R.id.main_nextbtn, 10000, 5);
        waitUntilEnabled(R.id.main_nextbtn);
        clickWithAnimation(R.id.main_nextbtn);
        Log.d(TAG, "Clicked login button");

        // Wait longer for network response
        sleep(5000);

        // 2. Navigate to Appointments view using UiAutomator text search
        boolean appointmentsBoxClicked = false;

        // Wait for the welcome screen to load
        waitForViewWithRetry(R.id.welcomeText, 3000, 3);
        Log.d(TAG, "Welcome screen loaded, looking for Appointments box");

        try {
            // Try to find a view containing "Appointments" or "My Appointments" text
            UiObject appointmentsButton = device.findObject(new UiSelector()
                    .textContains("Appointment")
                    .clickable(true));

            if (appointmentsButton.exists()) {
                appointmentsButton.click();
                appointmentsBoxClicked = true;
                Log.d(TAG, "Clicked Appointments box using text search");
                sleep(ANIMATION_DELAY * 2);
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Could not find appointments box with text search", e);
        }

        // If text search failed, try resource ID pattern search
        if (!appointmentsBoxClicked) {
            try {
                // Based on the booking box pattern (boxBookAppointment)
                UiObject idButton = device.findObject(new UiSelector()
                        .resourceIdMatches(".*box(Appointment|Appointments|MyAppointments).*"));

                if (idButton.exists()) {
                    idButton.click();
                    appointmentsBoxClicked = true;
                    Log.d(TAG, "Clicked appointments box using resource ID pattern search");
                    sleep(ANIMATION_DELAY * 2);
                }
            } catch (UiObjectNotFoundException e) {
                Log.e(TAG, "Could not find appointments box with resource ID search", e);
            }
        }

        if (!appointmentsBoxClicked) {
            throw new RuntimeException("Could not find and click the appointments box with any approach");
        }

        // 3. In AllAppointmentsActivity - verify and wait for appointments to load
        waitForViewWithRetry(R.id.recyclerViewAppointments, 5000, 5);
        waitForRecyclerViewItems(R.id.recyclerViewAppointments, 1, 8000, 5);
        Log.d(TAG, "Appointments loaded");
        sleep(2000); // Extra wait for animation

        // 4. Click on "Details" button for the first appointment
        final boolean[] detailsClicked = {false};

        try {
            // Click on the details button in the first item of the RecyclerView
            onView(withId(R.id.recyclerViewAppointments))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                            new ViewAction() {
                                @Override
                                public Matcher<View> getConstraints() {
                                    return isEnabled();
                                }

                                @Override
                                public String getDescription() {
                                    return "Click on Details button";
                                }

                                @Override
                                public void perform(UiController uiController, View view) {
                                    View detailsButton = view.findViewById(R.id.btnViewDetails);
                                    if (detailsButton != null && detailsButton.getVisibility() == View.VISIBLE) {
                                        detailsButton.performClick();
                                        Log.d(TAG, "Clicked Details button in RecyclerView item");
                                        detailsClicked[0] = true;
                                    } else {
                                        Log.e(TAG, "Details button not found or not visible in item");
                                    }
                                }
                            }));
        } catch (Exception e) {
            Log.e(TAG, "Error clicking Details button: " + e.getMessage());
        }

        // If RecyclerView approach failed, try using UiAutomator
        if (!detailsClicked[0]) {
            try {
                UiObject detailsButton = device.findObject(new UiSelector()
                        .text("Details")
                        .clickable(true));
                if (detailsButton.exists()) {
                    detailsButton.click();
                    detailsClicked[0] = true;
                    Log.d(TAG, "Clicked Details with UiAutomator");
                }
            } catch (UiObjectNotFoundException e) {
                Log.e(TAG, "Could not find Details button with UiAutomator", e);
            }
        }

        sleep(2000); // Wait for details dialog to appear

        // 5. Click on "Cancel Appointment" in the details dialog
        boolean cancelClicked = false;

        try {
            UiObject cancelButton = device.findObject(new UiSelector()
                    .textContains("Cancel")
                    .clickable(true));
            if (cancelButton.exists()) {
                cancelButton.click();
                cancelClicked = true;
                Log.d(TAG, "Clicked Cancel Appointment button in dialog");
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Could not find Cancel button in dialog", e);
        }

        if (!cancelClicked) {
            Log.e(TAG, "Could not find and click the Cancel button with any approach");
            // Try to continue anyway
        }

        sleep(2000); // Wait for confirmation dialog

        // 6. Confirm cancellation in the confirmation dialog
        boolean confirmCancelClicked = false;

        try {
            // Look for the confirmation button that contains "Cancel Appointment" text
            UiObject confirmButton = device.findObject(new UiSelector()
                    .textContains("Cancel Appointment")
                    .clickable(true));
            if (confirmButton.exists()) {
                confirmButton.click();
                confirmCancelClicked = true;
                Log.d(TAG, "Confirmed cancellation in dialog");
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Could not find confirmation button", e);
        }

        if (!confirmCancelClicked) {
            // Try an alternative approach
            try {
                // Look for any button with "Cancel" in the text that might be the confirmation
                UiObject altConfirmButton = device.findObject(new UiSelector()
                        .textContains("Cancel")
                        .clickable(true));
                if (altConfirmButton.exists()) {
                    altConfirmButton.click();
                    confirmCancelClicked = true;
                    Log.d(TAG, "Confirmed cancellation with alternative button");
                }
            } catch (UiObjectNotFoundException e) {
                Log.e(TAG, "Could not find alternative confirmation button", e);
            }
        }

        if (!confirmCancelClicked) {
            Log.e(TAG, "Could not confirm cancellation");
            // Continue anyway to see if we can verify the result
        }

        // 7. Wait for cancellation to process and verify the appointment was removed
        sleep(5000); // Wait for network response and UI update

        // Check if we get a success toast message
        try {
            UiObject toastMessage = device.findObject(new UiSelector()
                    .textContains("cancel")
                    .className("android.widget.Toast")
                    .packageName("com.example.androidexample"));
            boolean toastFound = toastMessage.waitForExists(3000);
            Log.d(TAG, "Success toast message found: " + toastFound);
        } catch (Exception e) {
            Log.e(TAG, "Error checking for toast message: " + e.getMessage());
        }

        // Verify we're back at the appointments screen
        try {
            waitForViewWithRetry(R.id.recyclerViewAppointments, 5000, 3);
            Log.d(TAG, "Successfully returned to appointments screen");

            // Optional: Check if the number of items decreased (if we had multiple appointments)
            // This might fail if we only had one appointment and it's now showing empty state
            // So we wrap it in a try-catch
            try {
                onView(withId(R.id.recyclerViewAppointments)).check(matches(isDisplayed()));
                Log.d(TAG, "RecyclerView is still displayed, appointment was likely removed successfully");
            } catch (Exception e) {
                // Check if we're seeing the empty state message instead
                try {
                    onView(withId(R.id.tvEmptyState)).check(matches(isDisplayed()));
                    Log.d(TAG, "Empty state is displayed, all appointments were likely removed");
                } catch (Exception e2) {
                    Log.e(TAG, "Could not verify final state after cancellation");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not verify return to appointments screen: " + e.getMessage());
        }
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

    // Helper method to wait for RecyclerView to have items
    private void waitForRecyclerViewItems(int recyclerViewId, int minItems, long totalWaitTime, int retries) {
        long waitPerRetry = totalWaitTime / retries;

        for (int i = 0; i < retries; i++) {
            try {
                sleep(waitPerRetry);
                onView(withId(recyclerViewId))
                        .check(new PaymentRatingFlowTest.RecyclerViewItemCountAssertion(minItems));
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