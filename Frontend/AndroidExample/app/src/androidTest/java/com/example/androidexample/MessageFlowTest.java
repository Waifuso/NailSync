package com.example.androidexample;

import android.os.RemoteException;
import android.util.Log;
import android.view.View;

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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MessageFlowTest {

    private static final String TAG = "MessageFlowTest";
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
    public void testMessageFlow() {
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

        // 2. Navigate to Messages view
        // Look for the Messages box in ApplicationActivity
        waitForViewWithRetry(R.id.welcomeText, 1000, 3);
        Log.d(TAG, "Welcome screen loaded, looking for Messages box");

        // Use UI Automator to find the messaging element by text content
        boolean messageBoxClicked = false;
        sleep(2000); // Give time for the main screen to fully load

        try {
            // Try first with "Messages" text
            UiObject messagesButton = device.findObject(new UiSelector()
                    .textContains("Message")
                    .clickable(true));

            if (messagesButton.exists()) {
                messagesButton.click();
                messageBoxClicked = true;
                Log.d(TAG, "Clicked Messages box using UiAutomator text search");
            } else {
                // Try alternate common text variations
                String[] possibleTexts = {"Chat", "Inbox", "Contact", "Support"};

                for (String text : possibleTexts) {
                    if (messageBoxClicked) break;

                    UiObject altButton = device.findObject(new UiSelector()
                            .textContains(text)
                            .clickable(true));

                    if (altButton.exists()) {
                        altButton.click();
                        messageBoxClicked = true;
                        Log.d(TAG, "Clicked " + text + " box using UiAutomator text search");
                    }
                }
            }

            // If text search failed, try description search
            if (!messageBoxClicked) {
                UiObject descButton = device.findObject(new UiSelector()
                        .descriptionContains("message")
                        .clickable(true));

                if (descButton.exists()) {
                    descButton.click();
                    messageBoxClicked = true;
                    Log.d(TAG, "Clicked messaging box using description search");
                }
            }

            // Last resort - try to find by resource ID pattern if we have a hint
            if (!messageBoxClicked) {
                // Based on the pattern from JacobSystemTest (boxBookAppointment)
                UiObject idButton = device.findObject(new UiSelector()
                        .resourceIdMatches(".*box(Message|Messages|Chat|Contact).*"));

                if (idButton.exists()) {
                    idButton.click();
                    messageBoxClicked = true;
                    Log.d(TAG, "Clicked box using resource ID pattern search");
                }
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Could not find messages box with UiAutomator", e);
        }

        if (!messageBoxClicked) {
            throw new RuntimeException("Could not find and click the messages box with any approach");
        }

        // Wait for screen transition animation
        sleep(ANIMATION_DELAY * 2);

        // 3. In ViewMessagesActivity - verify page loaded and select a contact
        // Verify we're on the select contact screen
        waitForViewWithRetry(R.id.title, 2000, 3);
        onView(withId(R.id.title)).check(matches(withText("Select Contact")));
        Log.d(TAG, "Successfully navigated to Select Contact screen");

        // Wait for employee list to load
        waitForViewWithRetry(R.id.employeesRecyclerView, 2000, 3);
        waitForRecyclerViewItems(R.id.employeesRecyclerView, 1, 5000, 5);
        Log.d(TAG, "Employee list loaded");

        // Click on the first employee in the list
        onView(withId(R.id.employeesRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Log.d(TAG, "Clicked on first employee in list");

        // Wait for screen transition animation
        sleep(ANIMATION_DELAY * 2);

        // 4. In ChatActivity - verify page loaded and test sending a message
        // Verify we're on the chat screen
        waitForViewWithRetry(R.id.messagesRecyclerView, 2000, 3);
        waitForViewWithRetry(R.id.msgEdt, 2000, 3);
        Log.d(TAG, "Successfully navigated to Chat screen");

        // Type a test message
        onView(withId(R.id.msgEdt))
                .perform(typeText("Hello, this is a test message"), closeSoftKeyboard());
        Log.d(TAG, "Entered test message");

        // Send the message
        waitForViewWithRetry(R.id.sendBtn, 1000, 2);
        clickWithAnimation(R.id.sendBtn);
        Log.d(TAG, "Clicked send button");

        // Verify message appears in the recycler view (optional)
        sleep(2000); // Wait for message to appear

        // Test navigating back
        waitForViewWithRetry(R.id.backButton, 1000, 2);
        clickWithAnimation(R.id.backButton);
        Log.d(TAG, "Clicked back button from chat");

        // Verify we're back at the Select Contact screen
        waitForViewWithRetry(R.id.title, 2000, 3);
        onView(withId(R.id.title)).check(matches(withText("Select Contact")));
        Log.d(TAG, "Successfully navigated back to Select Contact screen");
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
}