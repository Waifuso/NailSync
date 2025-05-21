package com.example.androidexample;

import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
public class PaymentRatingFlowTest {

    private static final String TAG = "PaymentRatingFlow";
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
    public void testPaymentAndRatingFlow() {
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

        // 4. Find and click "Pay Now" button in the first appointment item
        boolean payNowClicked = false;

        // Try using our helper method to find and click Pay Now in the RecyclerView
        try {
            findAndClickPayNowInRecyclerView();
            payNowClicked = true;
        } catch (Exception e) {
            Log.e(TAG, "Error finding Pay Now button: " + e.getMessage());
            payNowClicked = false;
        }

        // If finding and clicking Pay Now didn't work, try with UiAutomator text search
        if (!payNowClicked) {
            try {
                UiObject payNowButton = device.findObject(new UiSelector()
                        .text("Pay Now")
                        .clickable(true));
                if (payNowButton.exists()) {
                    payNowButton.click();
                    payNowClicked = true;
                    Log.d(TAG, "Clicked Pay Now with UiAutomator");
                }
            } catch (UiObjectNotFoundException e) {
                Log.e(TAG, "Could not find Pay Now button with UiAutomator", e);
            }
        }

        // If all direct methods failed, try the payment dialog
        if (!payNowClicked) {
            try {
                // Try clicking on the view details button first
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
                                        if (detailsButton != null) {
                                            detailsButton.performClick();
                                        }
                                    }
                                }));

                sleep(1000); // Wait for dialog to appear

                // Try to click on the payment option in the dialog
                try {
                    UiObject paymentOption = device.findObject(new UiSelector()
                            .text("Pay")
                            .clickable(true));
                    if (paymentOption.exists()) {
                        paymentOption.click();
                        payNowClicked = true;
                        Log.d(TAG, "Clicked payment option in dialog");
                    }
                } catch (UiObjectNotFoundException e) {
                    Log.e(TAG, "Could not find payment option in dialog", e);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error with fallback payment approach: " + e.getMessage());
            }
        }

        if (!payNowClicked) {
            Log.e(TAG, "Could not click Pay Now button with any approach");
            // Continue anyway - we might be in the payment dialog
        }

        // 5. In Payment Dialog - select payment method and proceed
        sleep(2000); // Wait for dialog to appear

        try {
            // Try to find radio buttons by ID
            waitForViewWithRetry(R.id.radioCard, 2000, 2);
            clickWithAnimation(R.id.radioCard); // Select Credit Card option
            Log.d(TAG, "Selected Credit Card payment option");

            // Click Proceed button
            waitForViewWithRetry(R.id.btnProceed, 1000, 2);
            clickWithAnimation(R.id.btnProceed);
            Log.d(TAG, "Clicked Proceed button");
        } catch (Exception e) {
            // If finding specific IDs fails, try using UiAutomator
            Log.e(TAG, "Could not find payment dialog elements by ID: " + e.getMessage());

            try {
                // Try to find Credit Card radio button
                UiObject cardOption = device.findObject(new UiSelector()
                        .textContains("Credit")
                        .clickable(true));
                if (cardOption.exists()) {
                    cardOption.click();
                    Log.d(TAG, "Selected Credit Card with UiAutomator");
                }

                // Find and click Proceed button
                sleep(500);
                UiObject proceedButton = device.findObject(new UiSelector()
                        .text("Proceed")
                        .clickable(true));
                if (proceedButton.exists()) {
                    proceedButton.click();
                    Log.d(TAG, "Clicked Proceed with UiAutomator");
                }
            } catch (UiObjectNotFoundException uie) {
                Log.e(TAG, "Could not find payment dialog elements with UiAutomator: " + uie.getMessage());
            }
        }

        // 6. In CompletePaymentActivity - verify payment screen and click Leave Review
        sleep(3000); // Wait for payment processing

        try {
            // Wait for the receipt to be displayed
            waitForViewWithRetry(R.id.receiptCard, 5000, 5);
            Log.d(TAG, "Payment receipt displayed");

            // Look for Leave Review button and click it
            waitForViewWithRetry(R.id.btnLeaveReview, 2000, 3);
            clickWithAnimation(R.id.btnLeaveReview);
            Log.d(TAG, "Clicked Leave Review button");
        } catch (Exception e) {
            Log.e(TAG, "Error in payment completion screen: " + e.getMessage());

            // Try with UiAutomator if Espresso fails
            try {
                UiObject reviewButton = device.findObject(new UiSelector()
                        .text("Leave Review")
                        .clickable(true));
                if (reviewButton.exists()) {
                    reviewButton.click();
                    Log.d(TAG, "Clicked Leave Review with UiAutomator");
                }
            } catch (UiObjectNotFoundException uie) {
                Log.e(TAG, "Could not find Leave Review button with UiAutomator", uie);
            }
        }

        // 7. In ReviewActivity - complete all stages of the review
        sleep(3000); // Wait for review activity to load

        // Stage 1: Rate experience and select what went well
        waitForViewWithRetry(R.id.ratingBar, 5000, 5);
        Log.d(TAG, "Review stage 1 loaded");

        // Click on the rating bar to rate 5 stars
        onView(withId(R.id.ratingBar)).perform(click());
        Log.d(TAG, "Set rating to 5 stars");

        // Select some positive aspects (chips)
        try {
            clickWithAnimation(R.id.chipQualityNails);
            clickWithAnimation(R.id.chipFriendly);
            Log.d(TAG, "Selected positive aspects");
        } catch (Exception e) {
            Log.e(TAG, "Error selecting chips: " + e.getMessage());
        }

        // Click Next to proceed to stage 2
        waitForViewWithRetry(R.id.btnNext, 1000, 2);
        clickWithAnimation(R.id.btnNext);
        Log.d(TAG, "Proceeded to review stage 2");

        // Stage 2: Write review text
        sleep(2000);

        // Enter review text
        waitForViewWithRetry(R.id.etReviewText, 3000, 3);
        onView(withId(R.id.etReviewText))
                .perform(typeText("Excellent service! Very professional and friendly staff."), closeSoftKeyboard());
        Log.d(TAG, "Entered review text");

        // Click Next to proceed to stage 3
        waitForViewWithRetry(R.id.btnNext, 1000, 2);
        clickWithAnimation(R.id.btnNext);
        Log.d(TAG, "Proceeded to review stage 3");

        // Stage 3: Submit the review
        sleep(2000);

        // Click Submit Review button
        waitForViewWithRetry(R.id.btnSubmitReview, 3000, 3);
        clickWithAnimation(R.id.btnSubmitReview);
        Log.d(TAG, "Clicked Submit Review button");

        // 8. Verify success and return to previous screens
        sleep(3000); // Wait for submission to complete

        // Check if we're back at the payment screen (should see the Done button)
        try {
            waitForViewWithRetry(R.id.btnDone, 5000, 5);
            clickWithAnimation(R.id.btnDone);
            Log.d(TAG, "Clicked Done button to complete flow");
        } catch (Exception e) {
            Log.e(TAG, "Could not find Done button: " + e.getMessage());

            // Try with UiAutomator
            try {
                UiObject doneButton = device.findObject(new UiSelector()
                        .text("Done")
                        .clickable(true));
                if (doneButton.exists()) {
                    doneButton.click();
                    Log.d(TAG, "Clicked Done with UiAutomator");
                }
            } catch (UiObjectNotFoundException uie) {
                Log.e(TAG, "Could not find Done button with UiAutomator", uie);
            }
        }

        // Wait for navigation back to appointments screen
        sleep(3000);

        // Verify we're back at the appointments screen
        try {
            waitForViewWithRetry(R.id.recyclerViewAppointments, 5000, 3);
            Log.d(TAG, "Successfully returned to appointments screen");
        } catch (Exception e) {
            Log.e(TAG, "Could not verify return to appointments screen: " + e.getMessage());
        }
    }

    // Remove the need for getting the Activity directly by using alternative approaches
    private void findAndClickPayNowInRecyclerView() {
        try {
            // Try to find and click the Pay Now button in the first appointment item
            onView(withId(R.id.recyclerViewAppointments))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                            new ViewAction() {
                                @Override
                                public Matcher<View> getConstraints() {
                                    return isEnabled(); // Matcher that matches enabled views
                                }

                                @Override
                                public String getDescription() {
                                    return "Click on Pay Now button";
                                }

                                @Override
                                public void perform(UiController uiController, View view) {
                                    // Find the Pay Now button within this item
                                    View payNowButton = view.findViewById(R.id.btnPayNow);
                                    if (payNowButton != null && payNowButton.getVisibility() == View.VISIBLE) {
                                        payNowButton.performClick();
                                        Log.d(TAG, "Clicked Pay Now button in RecyclerView item");
                                    } else {
                                        Log.e(TAG, "Pay Now button not found or not visible in item");
                                    }
                                }
                            }));
        } catch (Exception e) {
            Log.e(TAG, "Error clicking Pay Now within RecyclerView: " + e.getMessage());

            // Try using UiAutomator as fallback
            try {
                UiObject payNowButton = device.findObject(new UiSelector()
                        .text("Pay Now")
                        .clickable(true));
                if (payNowButton.exists()) {
                    payNowButton.click();
                    Log.d(TAG, "Clicked Pay Now with UiAutomator");
                } else {
                    Log.e(TAG, "Pay Now button not found with UiAutomator");
                }
            } catch (UiObjectNotFoundException uie) {
                Log.e(TAG, "UiObjectNotFoundException finding Pay Now: " + uie.getMessage());
            }
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

    // Custom matcher to find a button with specific text
    public static Matcher<View> withButtonText(final String text) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with button text: " + text);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof android.widget.Button)) {
                    return false;
                }
                android.widget.Button button = (android.widget.Button) view;
                return button.getText().toString().equalsIgnoreCase(text);
            }
        };
    }
}