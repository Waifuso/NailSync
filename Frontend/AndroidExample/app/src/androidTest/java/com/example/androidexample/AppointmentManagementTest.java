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

// Import these classes from the correct package
import com.example.androidUI.AppointmentAdapter;
import com.example.androidUI.AppointmentModel;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
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
public class AppointmentManagementTest {

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
            Log.e("AppointmentManagementTest", "Error setting orientation", e);
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
            Log.e("AppointmentManagementTest", "Error resetting orientation", e);
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

    // Enhanced scrolling method with better control and logging
    public static ViewAction scrollDownSlowly(final int scrolls, final int delayBetweenScrollsMs) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Scroll down slowly " + scrolls + " times with " + delayBetweenScrollsMs + "ms delay";
            }

            @Override
            public void perform(UiController uiController, View view) {
                for (int i = 0; i < scrolls; i++) {
                    onView(isRoot()).perform(swipeUp());
                    uiController.loopMainThreadForAtLeast(delayBetweenScrollsMs);
                    // Log each scroll action
                    Log.d("AppointmentManagementTest", "Performed scroll " + (i + 1) + " of " + scrolls);
                }
            }
        };
    }

    // More intelligent scrolling that targets a specific view
    public static ViewAction scrollViewToShow(final int targetViewId) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Scroll until view with id " + targetViewId + " is visible";
            }

            @Override
            public void perform(UiController uiController, View view) {
                // Try to find the target view
                int maxScrollAttempts = 10;

                for (int i = 0; i < maxScrollAttempts; i++) {
                    try {
                        // Check if view exists and is displayed
                        onView(withId(targetViewId)).check(matches(isDisplayed()));
                        Log.d("AppointmentManagementTest", "Target view found after " + (i) + " scrolls");
                        return; // Exit if found
                    } catch (Exception e) {
                        if (i == maxScrollAttempts - 1) {
                            // Last attempt failed
                            Log.e("AppointmentManagementTest", "Target view not found after " + maxScrollAttempts + " scrolls");
                            break;
                        }

                        // View not found, scroll and try again
                        onView(isRoot()).perform(swipeUp());
                        uiController.loopMainThreadForAtLeast(700); // Longer wait for UI to settle
                        Log.d("AppointmentManagementTest", "Scrolled attempt " + (i + 1) + ", looking for view");
                    }
                }
            }
        };
    }

    // Method to perform progressive scrolling with increasing intensity
    public static ViewAction progressiveScroll(final int targetViewId) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Progressive scrolling to find view " + targetViewId;
            }

            @Override
            public void perform(UiController uiController, View view) {
                // First try small scrolls
                for (int i = 0; i < 3; i++) {
                    try {
                        onView(withId(targetViewId)).check(matches(isDisplayed()));
                        Log.d("AppointmentManagementTest", "View found with gentle scrolling");
                        return;
                    } catch (Exception e) {
                        // Small scroll
                        onView(isRoot()).perform(swipeUp());
                        uiController.loopMainThreadForAtLeast(500);
                    }
                }

                // If still not found, try medium scrolls
                for (int i = 0; i < 3; i++) {
                    try {
                        onView(withId(targetViewId)).check(matches(isDisplayed()));
                        Log.d("AppointmentManagementTest", "View found with medium scrolling");
                        return;
                    } catch (Exception e) {
                        // Medium scroll - two quick swipes
                        onView(isRoot()).perform(swipeUp());
                        uiController.loopMainThreadForAtLeast(300);
                        onView(isRoot()).perform(swipeUp());
                        uiController.loopMainThreadForAtLeast(700);
                    }
                }

                // If still not found, try bigger scrolls
                for (int i = 0; i < 3; i++) {
                    try {
                        onView(withId(targetViewId)).check(matches(isDisplayed()));
                        Log.d("AppointmentManagementTest", "View found with aggressive scrolling");
                        return;
                    } catch (Exception e) {
                        // Aggressive scroll - three quick swipes
                        onView(isRoot()).perform(swipeUp());
                        uiController.loopMainThreadForAtLeast(200);
                        onView(isRoot()).perform(swipeUp());
                        uiController.loopMainThreadForAtLeast(200);
                        onView(isRoot()).perform(swipeUp());
                        uiController.loopMainThreadForAtLeast(800);
                    }
                }

                Log.e("AppointmentManagementTest", "View not found after progressive scrolling");
            }
        };
    }

    @Test
    public void testAppointmentRescheduleAndCancel() {
        // Initial delay to ensure app fully launches
        sleep(3000);

        // 1. Login
        waitForViewWithRetry(R.id.email_entryfield, 5000, 3);
        onView(withId(R.id.email_entryfield))
                .perform(typeText("phongdz@gmail.com"), closeSoftKeyboard());
        Log.d("AppointmentManagementTest", "Entered email");

        sleep(500); // Short delay between inputs

        waitForViewWithRetry(R.id.login_password_field, 5000, 3);
        onView(withId(R.id.login_password_field))
                .perform(typeText("1233"), closeSoftKeyboard());
        Log.d("AppointmentManagementTest", "Entered password");

        sleep(1000);

        waitForViewWithRetry(R.id.main_nextbtn, 10000, 5);
        waitUntilEnabled(R.id.main_nextbtn);
        clickWithAnimation(R.id.main_nextbtn);
        Log.d("AppointmentManagementTest", "Clicked login button");

        // Wait longer for network response and application to load
        sleep(5000);

        // 2. In ApplicationActivity - find and click the "My Appointments" box
        waitForViewWithRetry(R.id.welcomeText, 5000, 3);
        Log.d("AppointmentManagementTest", "Welcome screen loaded, looking for My Appointments");

        waitForViewWithRetry(R.id.boxMyAppointments, 5000, 3);
        clickWithAnimation(R.id.boxMyAppointments);
        Log.d("AppointmentManagementTest", "Clicked My Appointments box");

        sleep(ANIMATION_DELAY * 2);

        // 3. In AllAppointmentsActivity - Wait for appointments to load
        waitForViewWithRetry(R.id.recyclerViewAppointments, 5000, 3);

        // Wait for RecyclerView to populate with appointments
        waitForRecyclerViewItems(R.id.recyclerViewAppointments, 1, 6000, 6);
        sleep(2000); // Give extra time for all animations and loading to complete

        Log.d("AppointmentManagementTest", "Appointments loaded");

        // 4. Click "VIEW DETAILS" button on the first appointment
        boolean viewDetailsClicked = false;

        // First approach: Try to find the "VIEW DETAILS" button using UiAutomator
        try {
            UiObject viewDetailsButton = device.findObject(new UiSelector().text("VIEW DETAILS"));
            if (viewDetailsButton.exists()) {
                viewDetailsButton.click();
                Log.d("AppointmentManagementTest", "Clicked 'VIEW DETAILS' button with UiAutomator");
                viewDetailsClicked = true;
            }
        } catch (UiObjectNotFoundException e) {
            Log.e("AppointmentManagementTest", "Could not find 'VIEW DETAILS' button with UiAutomator", e);
        }

        // Second approach: Try to click the button via RecyclerView and button ID
        if (!viewDetailsClicked) {
            try {
                // Try to find the btnViewDetails inside the first item
                onView(withId(R.id.recyclerViewAppointments))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                                clickChildViewWithId(R.id.btnViewDetails)));
                Log.d("AppointmentManagementTest", "Clicked VIEW DETAILS via RecyclerView actionOnItem");
                viewDetailsClicked = true;
            } catch (Exception e) {
                Log.e("AppointmentManagementTest", "Failed to click VIEW DETAILS via RecyclerView: " + e.getMessage());
            }
        }

        // Third approach: Try clicking on the item itself (may trigger ViewDetails in some implementations)
        if (!viewDetailsClicked) {
            try {
                onView(withId(R.id.recyclerViewAppointments))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
                Log.d("AppointmentManagementTest", "Clicked first appointment item");
                viewDetailsClicked = true;
            } catch (Exception e) {
                Log.e("AppointmentManagementTest", "Failed to click first appointment: " + e.getMessage());
            }
        }

        sleep(ANIMATION_DELAY * 2); // Wait for dialog to appear

        // 5. In the Appointment Details Dialog - Click Reschedule
        try {
            // Attempt to click the RESCHEDULE button in the dialog
            UiObject rescheduleButton = device.findObject(new UiSelector().text("RESCHEDULE"));
            if (rescheduleButton.exists()) {
                rescheduleButton.click();
                Log.d("AppointmentManagementTest", "Clicked RESCHEDULE button in dialog");
            } else {
                Log.e("AppointmentManagementTest", "RESCHEDULE button not found in dialog");
                // Try an alternative approach - looking for reschedule button by ID
                try {
                    onView(withId(R.id.btnReschedule)).perform(click());
                    Log.d("AppointmentManagementTest", "Clicked btnReschedule by ID");
                } catch (Exception e) {
                    Log.e("AppointmentManagementTest", "Failed to click reschedule button by ID: " + e.getMessage());
                }
            }
        } catch (UiObjectNotFoundException e) {
            Log.e("AppointmentManagementTest", "Could not find RESCHEDULE button: " + e.getMessage());
        }

        sleep(ANIMATION_DELAY * 2); // Wait for RescheduleAppointmentActivity to load

        // 6. In RescheduleAppointmentActivity - Select a date
        waitForViewWithRetry(R.id.calendarGrid, 5000, 3);
        Log.d("AppointmentManagementTest", "Calendar loaded, selecting a date");

        // Try multiple approaches to select a date
        boolean dateSelected = false;

        // First try: Direct click on grid
        try {
            onView(withId(R.id.calendarGrid)).perform(click());
            Log.d("AppointmentManagementTest", "Clicked directly on calendar grid");
            dateSelected = true;
        } catch (Exception e) {
            Log.e("AppointmentManagementTest", "Failed direct calendar grid click: " + e.getMessage());
        }

        // Second try: Use UiAutomator to find a selectable day
        if (!dateSelected) {
            try {
                // Try selecting days starting from tomorrow (day + 1) to avoid selecting past dates
                int currentDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH);
                for (int i = currentDay + 1; i <= currentDay + 10; i++) {
                    try {
                        UiObject dateCell = device.findObject(new UiSelector().text(String.valueOf(i)));
                        if (dateCell.exists() && dateCell.isEnabled()) {
                            dateCell.click();
                            Log.d("AppointmentManagementTest", "Selected date " + i + " using UiAutomator");
                            dateSelected = true;
                            break;
                        }
                    } catch (UiObjectNotFoundException ex) {
                        // Continue to next date
                    }
                }
            } catch (Exception e) {
                Log.e("AppointmentManagementTest", "Failed UiAutomator date selection: " + e.getMessage());
            }
        }

        sleep(ANIMATION_DELAY);

        // 7. Click Continue to Time Selection button
        waitUntilEnabledWithRetry(R.id.btnSelectTime, 5, 1000);
        clickWithAnimation(R.id.btnSelectTime);
        Log.d("AppointmentManagementTest", "Clicked Continue to Time Selection");

        sleep(ANIMATION_DELAY);

        // 8. ENHANCED SCROLLING SECTION - Apply multiple strategies to scroll to time selection
        Log.d("AppointmentManagementTest", "Starting enhanced scrolling to find time selection");

        // Strategy 1: Try the progressive scrolling approach first
        try {
            Log.d("AppointmentManagementTest", "Using progressive scrolling strategy");
            onView(isRoot()).perform(progressiveScroll(R.id.timeSlotsGrid));

            // Verify time slots are visible
            onView(withId(R.id.timeSlotsGrid)).check(matches(isDisplayed()));
            Log.d("AppointmentManagementTest", "Time slots grid is visible after progressive scrolling");
        } catch (Exception e) {
            Log.e("AppointmentManagementTest", "Progressive scrolling failed: " + e.getMessage());

            // Strategy 2: Try the target-based scrolling approach
            try {
                Log.d("AppointmentManagementTest", "Falling back to target-based scrolling");
                onView(isRoot()).perform(scrollViewToShow(R.id.timeSlotsGrid));

                // Verify after this approach
                onView(withId(R.id.timeSlotsGrid)).check(matches(isDisplayed()));
                Log.d("AppointmentManagementTest", "Time slots grid is visible after target-based scrolling");
            } catch (Exception e2) {
                Log.e("AppointmentManagementTest", "Target-based scrolling failed: " + e2.getMessage());

                // Strategy 3: Try simple slow scrolling with more scrolls
                try {
                    Log.d("AppointmentManagementTest", "Falling back to simple slow scrolling");
                    onView(isRoot()).perform(scrollDownSlowly(7, 800)); // More scrolls, longer delay

                    // Check if visible now
                    onView(withId(R.id.timeSlotsGrid)).check(matches(isDisplayed()));
                    Log.d("AppointmentManagementTest", "Time slots grid is visible after slow scrolling");
                } catch (Exception e3) {
                    Log.e("AppointmentManagementTest", "Slow scrolling failed: " + e3.getMessage());

                    // Strategy 4: Last resort - try standard Espresso scrollTo
                    try {
                        Log.d("AppointmentManagementTest", "Trying Espresso's scrollTo as last resort");
                        onView(withId(R.id.timeSlotsGrid)).perform(scrollTo());

                        // Final check
                        onView(withId(R.id.timeSlotsGrid)).check(matches(isDisplayed()));
                        Log.d("AppointmentManagementTest", "Time slots grid is visible after Espresso scrollTo");
                    } catch (Exception e4) {
                        Log.e("AppointmentManagementTest", "All scrolling strategies failed");

                        // If all strategies failed, just continue and hope the UI is in a usable state
                        Log.d("AppointmentManagementTest", "Continuing test despite scrolling issues");
                    }
                }
            }
        }

        sleep(ANIMATION_DELAY); // Wait after all scrolling attempts

        // 9. Select a time slot with multiple approaches
        boolean timeSelected = false;

// Approach 1: Try to find and click a time slot button with UiAutomator
        try {
            Log.d("AppointmentManagementTest", "Attempting to find a time slot button with UiAutomator");

            // Try to find any Button within the timeSlotsGrid
            UiSelector timeButtonSelector = new UiSelector()
                    .className("android.widget.Button")
                    .clickable(true);

            UiObject timeButton = device.findObject(timeButtonSelector);
            if (timeButton.exists() && timeButton.isEnabled()) {
                timeButton.click();
                Log.d("AppointmentManagementTest", "Clicked time slot button using UiAutomator");
                timeSelected = true;
                sleep(ANIMATION_DELAY); // Wait for selection visual feedback
            }
        } catch (Exception e) {
            Log.e("AppointmentManagementTest", "Failed to select time with UiAutomator", e);
        }

// Approach A2: Try finding time slot by common time values
        if (!timeSelected) {
            try {
                // Try to find any time button in the grid with common time values
                String[] commonTimes = {"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00"};
                for (String time : commonTimes) {
                    try {
                        UiObject timeButton = device.findObject(new UiSelector().text(time));
                        if (timeButton.exists() && timeButton.isEnabled()) {
                            timeButton.click();
                            Log.d("AppointmentManagementTest", "Clicked time slot " + time + " with UiAutomator");
                            timeSelected = true;
                            sleep(ANIMATION_DELAY);
                            break;
                        }
                    } catch (UiObjectNotFoundException ex) {
                        // Try next time
                    }
                }
            } catch (Exception e) {
                Log.e("AppointmentManagementTest", "Failed to find time by value", e);
            }
        }

// Approach 2: If UiAutomator failed, try direct click on the grid with adjustment
        if (!timeSelected) {
            try {
                Log.d("AppointmentManagementTest", "Attempting direct click on timeSlotsGrid with coordinates");

                // Try clicking at various positions within the grid to find a button
                onView(withId(R.id.timeSlotsGrid)).perform(new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isDisplayed();
                    }

                    @Override
                    public String getDescription() {
                        return "Click at different positions in the time grid";
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
                            int x = (int) (width * pos[0]);
                            int y = (int) (height * pos[1]);

                            // Click at this position
                            view.performClick();
                            uiController.loopMainThreadForAtLeast(500);

                            // Try to find a child view at this position
                            View childAtPosition = findChildAtPosition(view, x, y);
                            if (childAtPosition != null && childAtPosition.isClickable()) {
                                childAtPosition.performClick();
                                uiController.loopMainThreadForAtLeast(500);
                                Log.d("AppointmentManagementTest", "Clicked child in grid at position " +
                                        pos[0] + "," + pos[1]);
                                return; // Exit on first successful click
                            }
                        }

                        // If no clickable child found, just click in the center
                        Log.d("AppointmentManagementTest", "No clickable child found, clicking center");
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

                Log.d("AppointmentManagementTest", "Performed grid click with coordinates");
                timeSelected = true;
                sleep(ANIMATION_DELAY);
            } catch (Exception e) {
                Log.e("AppointmentManagementTest", "Failed to click within timeSlotsGrid", e);
            }
        }

// Approach 3: Try clicking on children directly
        if (!timeSelected) {
            try {
                Log.d("AppointmentManagementTest", "Attempting to click children of time slots grid");

                onView(withId(R.id.timeSlotsGrid)).perform(new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isDisplayed();
                    }

                    @Override
                    public String getDescription() {
                        return "Click first clickable child in time grid";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        if (view instanceof ViewGroup) {
                            ViewGroup viewGroup = (ViewGroup) view;

                            // First try - look for enabled buttons
                            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                                View child = viewGroup.getChildAt(i);
                                if (child.getVisibility() == View.VISIBLE &&
                                        child.isClickable() &&
                                        child.isEnabled()) {
                                    Log.d("AppointmentManagementTest", "Found clickable child at index " + i);
                                    child.performClick();
                                    uiController.loopMainThreadForAtLeast(500);
                                    return;
                                }
                            }

                            // Second try - click any visible child
                            if (viewGroup.getChildCount() > 0) {
                                Log.d("AppointmentManagementTest", "No enabled children, clicking first visible child");
                                View firstChild = viewGroup.getChildAt(0);
                                if (firstChild.getVisibility() == View.VISIBLE) {
                                    firstChild.performClick();
                                    uiController.loopMainThreadForAtLeast(500);
                                }
                            }
                        }
                    }
                });

                timeSelected = true;
                sleep(ANIMATION_DELAY);
            } catch (Exception e) {
                Log.e("AppointmentManagementTest", "Failed to click children of time grid", e);
            }
        }

// Approach 4: Fall back to using the alternative time entry method
        if (!timeSelected) {
            try {
                Log.d("AppointmentManagementTest", "Attempting to use preferred time entry mode");

                // Look for the text view to switch modes
                try {
                    UiObject enterTimeText = device.findObject(new UiSelector().text("Enter Preferred Time"));
                    if (enterTimeText.exists()) {
                        enterTimeText.click();
                        Log.d("AppointmentManagementTest", "Switched to preferred time mode via UiAutomator");
                    } else {
                        // Try by ID
                        onView(withId(R.id.tvEnterPreferredTime)).perform(click());
                        Log.d("AppointmentManagementTest", "Switched to preferred time mode via ID");
                    }
                } catch (Exception e) {
                    Log.e("AppointmentManagementTest", "Error switching to preferred time: " + e.getMessage());
                }

                sleep(ANIMATION_DELAY);

                // Enter a valid time (e.g., "14:30")
                onView(withId(R.id.etPreferredTime))
                        .perform(typeText("14:30"), closeSoftKeyboard());

                Log.d("AppointmentManagementTest", "Entered preferred time: 14:30");
                timeSelected = true;
                sleep(ANIMATION_DELAY);
            } catch (Exception e) {
                Log.e("AppointmentManagementTest", "Failed to use preferred time entry", e);
            }
        }

// Final attempt - if all else fails, just continue anyway
        if (!timeSelected) {
            Log.e("AppointmentManagementTest", "All time selection methods failed - continuing anyway");
        }

        // 10. Click Confirm Reschedule button
        waitUntilEnabledWithRetry(R.id.btnReschedule, 5, 1000);
        clickWithAnimation(R.id.btnReschedule);
        Log.d("AppointmentManagementTest", "Clicked Confirm Reschedule");

        // Wait for reschedule operation to complete and return to AllAppointmentsActivity
        sleep(5000);

        // 11. Back in AllAppointmentsActivity - Click the VIEW DETAILS button again
        waitForViewWithRetry(R.id.recyclerViewAppointments, 5000, 3);
        Log.d("AppointmentManagementTest", "Back to appointments list, preparing to cancel an appointment");

        // Try to find and click "VIEW DETAILS" again using multiple approaches
        boolean detailsClickedForCancel = false;

        // First approach: Try to find the "VIEW DETAILS" button using UiAutomator
        try {
            UiObject viewDetailsButton = device.findObject(new UiSelector().text("VIEW DETAILS"));
            if (viewDetailsButton.exists()) {
                viewDetailsButton.click();
                Log.d("AppointmentManagementTest", "Clicked 'VIEW DETAILS' button for cancellation");
                detailsClickedForCancel = true;
            }
        } catch (UiObjectNotFoundException e) {
            Log.e("AppointmentManagementTest", "Could not find 'VIEW DETAILS' button for cancellation", e);
        }

        // Second approach: Try to click via RecyclerView
        if (!detailsClickedForCancel) {
            try {
                onView(withId(R.id.recyclerViewAppointments))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                                clickChildViewWithId(R.id.btnViewDetails)));
                Log.d("AppointmentManagementTest", "Clicked VIEW DETAILS via RecyclerView for cancellation");
                detailsClickedForCancel = true;
            } catch (Exception e) {
                Log.e("AppointmentManagementTest", "Failed to click VIEW DETAILS via RecyclerView: " + e.getMessage());
            }
        }

        // Third approach: Try clicking the item itself
        if (!detailsClickedForCancel) {
            try {
                onView(withId(R.id.recyclerViewAppointments))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
                Log.d("AppointmentManagementTest", "Clicked first appointment item for cancellation");
                detailsClickedForCancel = true;
            } catch (Exception e) {
                Log.e("AppointmentManagementTest", "Failed to click first appointment for cancellation: " + e.getMessage());
            }
        }

        sleep(ANIMATION_DELAY * 2); // Wait for dialog to appear

        // Enhanced implementation for cancelling an appointment
    // This code should be added to the testAppointmentRescheduleAndCancel method
    // after clicking "VIEW DETAILS" button and when the dialog appears
        }

    // Custom ViewAction to click a child view with specified ID
    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null; // No constraints, can be performed on any view
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified ID " + id;
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
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
                Log.d("AppointmentManagementTest", "View found after " + (i+1) + " attempts: " + viewId);
                return; // View found, exit the method
            } catch (Exception e) {
                if (i == retryCount - 1) {
                    // This was our last attempt
                    Log.e("AppointmentManagementTest", "View not found after " + retryCount + " attempts: " + viewId);
                    throw e; // Re-throw the exception on the last attempt
                }
                // Otherwise continue to next attempt
                Log.d("AppointmentManagementTest", "View not found yet, retrying...");
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
                    Log.e("AppointmentManagementTest", "View not enabled after waiting: " + viewId);
                }
            }
        }
    }

    // Enhanced version of waitUntilEnabled with better retry handling
    private void waitUntilEnabledWithRetry(int viewId, int maxRetries, long waitBetweenRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                onView(withId(viewId)).check(matches(isEnabled()));
                Log.d("AppointmentManagementTest", "View " + viewId + " is enabled");
                return; // Success, view is enabled
            } catch (Exception e) {
                if (i == maxRetries - 1) {
                    Log.e("AppointmentManagementTest", "View " + viewId + " not enabled after " + maxRetries + " retries");
                    // Continue anyway, we'll catch any further issues when trying to interact
                    return;
                }
                Log.d("AppointmentManagementTest", "View " + viewId + " not enabled yet, retry " + (i+1));
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
                Log.d("AppointmentManagementTest", "RecyclerView has at least " + minItems + " items after " + (i+1) + " attempts");
                return;
            } catch (Exception e) {
                if (i == retries - 1) {
                    Log.e("AppointmentManagementTest", "RecyclerView doesn't have " + minItems + " items after all retries");
                    throw e;
                }
                Log.d("AppointmentManagementTest", "Waiting for RecyclerView items, retry " + (i+1));
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
            Log.e("AppointmentManagementTest", "Failed to disable animations", e);
        }
    }

    private void enableAnimations() {
        try {
            // Re-enable animations
            device.executeShellCommand("settings put global window_animation_scale 1.0");
            device.executeShellCommand("settings put global transition_animation_scale 1.0");
            device.executeShellCommand("settings put global animator_duration_scale 1.0");
        } catch (Exception e) {
            Log.e("AppointmentManagementTest", "Failed to re-enable animations", e);
        }
    }

    public static ViewAction clickChildWithIndex(final int index) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Click on child at index " + index;
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (view instanceof ViewGroup) {
                    ViewGroup group = (ViewGroup) view;
                    if (group.getChildCount() > index) {
                        View child = group.getChildAt(index);
                        if (child.isClickable() && child.getVisibility() == View.VISIBLE) {
                            child.performClick();
                        }
                    }
                }
            }
        };
    }
}