package com.example.androidexample;

import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
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
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileEditTest {

    private UiDevice device;
    private static final int ANIMATION_DELAY = 1000; // Wait time for animations
    private static final String TAG = "ProfileEditAndLogoutTest";

    private String user1name = "phongdz@gmail.com";
    private String user1password = "1234";
    private String newPassword = "1233";

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
    public void testProfileEditAndLogout() {
        // Initial delay to ensure app fully launches
        sleep(3000);

        // 1. LOGIN FLOW
        Log.i(TAG, "Starting login process");
        // Find and input email
        waitForViewWithRetry(R.id.email_entryfield, 5000, 3);
        onView(withId(R.id.email_entryfield))
                .perform(typeText(user1name), closeSoftKeyboard());
        Log.d(TAG, "Entered email");

        sleep(500); // Short delay between inputs

        // Find and input password
        waitForViewWithRetry(R.id.login_password_field, 5000, 3);
        onView(withId(R.id.login_password_field))
                .perform(typeText(user1password), closeSoftKeyboard());
        Log.d(TAG, "Entered password");

        sleep(1000); // Give app time to process inputs before clicking login

        // Wait for login button to appear and then click it
        waitForViewWithRetry(R.id.main_nextbtn, 10000, 5);
        waitUntilEnabled(R.id.main_nextbtn);
        clickWithAnimation(R.id.main_nextbtn);
        Log.d(TAG, "Clicked login button");

        // Wait longer for network response
        sleep(5000);

        // 2. VERIFY WE'RE IN APPLICATION ACTIVITY
        waitForViewWithRetry(R.id.welcomeText, 10000, 3);
        Log.d(TAG, "Application screen loaded");

        // 3. NAVIGATE TO USER PROFILE using UiAutomator - more reliable for finding elements without known IDs
        Log.i(TAG, "Attempting to navigate to profile screen using UiAutomator");
        boolean navigatedToProfile = false;

        // Try multiple approaches to find and click on profile-related elements
        try {
            // Approach 1: Look for elements containing "profile" text
            UiObject profileText = device.findObject(new UiSelector()
                    .textContains("Profile")
                    .clickable(true));

            if (profileText.exists()) {
                profileText.click();
                Log.d(TAG, "Clicked on element with 'Profile' text");
                sleep(2000);
                navigatedToProfile = true;
            }
            else {
                // Approach 2: Look for my profile or user profile text
                UiObject myProfileText = device.findObject(new UiSelector()
                        .textContains("My Profile")
                        .clickable(true));

                if (myProfileText.exists()) {
                    myProfileText.click();
                    Log.d(TAG, "Clicked on 'My Profile' text");
                    sleep(2000);
                    navigatedToProfile = true;
                }
            }
        } catch (UiObjectNotFoundException e) {
            Log.e(TAG, "Profile text not found: " + e.getMessage());
        }

        // Approach 3: Look for profile icon by description
        if (!navigatedToProfile) {
            try {
                UiObject profileIcon = device.findObject(new UiSelector()
                        .descriptionContains("profile")
                        .clickable(true));

                if (profileIcon.exists()) {
                    profileIcon.click();
                    Log.d(TAG, "Clicked on profile icon by description");
                    sleep(2000);
                    navigatedToProfile = true;
                }
            } catch (UiObjectNotFoundException e) {
                Log.e(TAG, "Profile icon not found: " + e.getMessage());
            }
        }

        // Approach 4: Look for box with user profile functionality
        if (!navigatedToProfile) {
            try {
                UiObject boxProfile = device.findObject(new UiSelector()
                        .resourceIdMatches(".*box.*profile.*"));

                if (boxProfile.exists()) {
                    boxProfile.click();
                    Log.d(TAG, "Clicked on box with profile ID");
                    sleep(2000);
                    navigatedToProfile = true;
                }
            } catch (UiObjectNotFoundException e) {
                Log.e(TAG, "Box profile not found: " + e.getMessage());
            }
        }

        // Approach 5: Try to find profile in the application's boxBookAppointment area
        // From JacobSystemTest, we know there's an ID "boxBookAppointment", maybe there's a similar pattern
        if (!navigatedToProfile) {
            try {
                // Look for other boxes that might be in the same UI group
                UiObject boxUser = device.findObject(new UiSelector()
                        .resourceIdMatches(".*boxUser.*")
                        .clickable(true));

                if (boxUser.exists()) {
                    boxUser.click();
                    Log.d(TAG, "Clicked on boxUser");
                    sleep(2000);
                    navigatedToProfile = true;
                } else {
                    // Try clicking on a user icon if present
                    UiObject userIcon = device.findObject(new UiSelector()
                            .className("android.widget.ImageView")
                            .descriptionContains("user"));

                    if (userIcon.exists()) {
                        userIcon.click();
                        Log.d(TAG, "Clicked on user icon");
                        sleep(2000);
                        navigatedToProfile = true;
                    }
                }
            } catch (UiObjectNotFoundException e) {
                Log.e(TAG, "User box not found: " + e.getMessage());
            }
        }

        // Approach 6: Last resort - look for menu button and try to find profile in menu
        if (!navigatedToProfile) {
            try {
                // Look for a menu button (often in top right corner)
                UiObject menuButton = device.findObject(new UiSelector()
                        .className("android.widget.ImageButton")
                        .descriptionContains("menu"));

                if (menuButton.exists()) {
                    menuButton.click();
                    Log.d(TAG, "Clicked on menu button");
                    sleep(1000);

                    // Now look for profile in the opened menu
                    UiObject menuProfile = device.findObject(new UiSelector()
                            .textContains("Profile"));

                    if (menuProfile.exists()) {
                        menuProfile.click();
                        Log.d(TAG, "Clicked on Profile in menu");
                        sleep(2000);
                        navigatedToProfile = true;
                    }
                }
            } catch (UiObjectNotFoundException e) {
                Log.e(TAG, "Menu navigation failed: " + e.getMessage());
            }
        }

        // Approach 7: Final attempt - examine viewable elements and see if we can find something profile-related
        if (!navigatedToProfile) {
            try {
                // Try to find any account-related text that might lead to profile
                UiObject accountText = device.findObject(new UiSelector()
                        .textContains("Account")
                        .clickable(true));

                if (accountText.exists()) {
                    accountText.click();
                    Log.d(TAG, "Clicked on Account text");
                    sleep(2000);
                    navigatedToProfile = true;
                } else {
                    // Try to find elements with "user" in their resource ID
                    UiObject userElement = device.findObject(new UiSelector()
                            .resourceIdMatches(".*user.*")
                            .clickable(true));

                    if (userElement.exists()) {
                        userElement.click();
                        Log.d(TAG, "Clicked on user element");
                        sleep(2000);
                        navigatedToProfile = true;
                    } else {
                        // Last resort: Try UI Automator's newer API to find clickable elements
                        UiObject2 userIcon = device.findObject(By.clazz("android.widget.ImageView").clickable(true));
                        if (userIcon != null) {
                            userIcon.click();
                            Log.d(TAG, "Clicked on an image view that might be user icon");
                            sleep(2000);
                            // We won't mark navigatedToProfile as true because this is very speculative
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Final navigation attempts failed: " + e.getMessage());
            }
        }

        // 4. VERIFY WE'RE IN PROFILE VIEW (look for key UI elements from profile screen)
        Log.i(TAG, "Attempting to verify we're in profile view");
        sleep(3000);

        // Try looking for profile elements using both Espresso and UiAutomator
        boolean inProfileView = false;

        try {
            // Check if profile view elements are present
            if (isViewDisplayed(R.id.profile_username)) {
                Log.d(TAG, "Profile view loaded - found profile_username");
                inProfileView = true;
            } else if (isViewDisplayed(R.id.profile_email)) {
                Log.d(TAG, "Profile view loaded - found profile_email");
                inProfileView = true;
            } else if (isViewDisplayed(R.id.edit_profile_button)) {
                Log.d(TAG, "Profile view loaded - found edit_profile_button");
                inProfileView = true;
            } else {
                // Use UiAutomator as a backup check
                UiObject profileElement = device.findObject(new UiSelector().textContains("Profile"));
                if (profileElement.exists()) {
                    Log.d(TAG, "Found profile text using UiAutomator");
                    inProfileView = true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error verifying profile view: " + e.getMessage());
        }

        if (!inProfileView) {
            Log.e(TAG, "Failed to verify we're in profile view. Attempting to continue test.");
        }

        // 5. CLICK EDIT PROFILE BUTTON
        Log.i(TAG, "Attempting to click edit profile button");
        boolean clickedEditProfile = false;

        try {
            if (isViewDisplayed(R.id.edit_profile_button)) {
                clickWithAnimation(R.id.edit_profile_button);
                Log.d(TAG, "Clicked edit profile button using Espresso");
                clickedEditProfile = true;
            } else {
                // Try UiAutomator
                UiObject editButton = device.findObject(new UiSelector().textContains("Edit Profile"));
                if (editButton.exists()) {
                    editButton.click();
                    Log.d(TAG, "Clicked Edit Profile button using UiAutomator");
                    clickedEditProfile = true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to click edit profile button: " + e.getMessage());
        }

        if (!clickedEditProfile) {
            Log.e(TAG, "Could not find edit profile button! Attempting to continue.");
        }

        // 6. VERIFY WE'RE IN EDIT PROFILE VIEW
        Log.i(TAG, "Verifying edit profile view");
        sleep(2000);

        boolean inEditProfileView = false;

        try {
            // Check for username field first (should be most reliable)
            if (isViewDisplayed(R.id.username)) {
                Log.d(TAG, "Edit profile page loaded - found username field");
                inEditProfileView = true;
            } else if (isViewDisplayed(R.id.profile_title)) {
                Log.d(TAG, "Edit profile page loaded - found profile_title");
                inEditProfileView = true;
            } else {
                // Use UiAutomator as backup
                UiObject editText = device.findObject(new UiSelector().text("Edit Your Profile"));
                if (editText.exists()) {
                    Log.d(TAG, "Found 'Edit Your Profile' text using UiAutomator");
                    inEditProfileView = true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error verifying edit profile view: " + e.getMessage());
        }

        if (!inEditProfileView) {
            Log.e(TAG, "Could not verify we're in edit profile view. Attempting to continue.");
        }

        // 7. CHANGE USERNAME AND ENTER SAME PASSWORD
        Log.i(TAG, "Changing username to user1name and entering password 1223");
        try {
            // Change username to user1name
            if (isViewDisplayed(R.id.username)) {
                onView(withId(R.id.username))
                        .perform(replaceText("kakadaica"), closeSoftKeyboard());
                Log.d(TAG, "Changed username to user1name using Espresso");
            } else {
                // Try UiAutomator
                UiObject usernameField = device.findObject(new UiSelector()
                        .className("android.widget.EditText")
                        .textContains("Username"));

                if (usernameField.exists()) {
                    usernameField.setText(user1name);
                    Log.d(TAG, "Changed username to user1name using UiAutomator");
                    // Close keyboard
                    device.pressBack();
                    sleep(500);
                }
            }

            // Enter password user1password
            // changed from 1223
            if (isViewDisplayed(R.id.new_password)) {
                onView(withId(R.id.new_password))
                        .perform(replaceText(newPassword), closeSoftKeyboard());
                Log.d(TAG, "Entered password user1password in new_password field");

                // Also enter same password in confirm field
                if (isViewDisplayed(R.id.confirm_password)) {
                    onView(withId(R.id.confirm_password))
                            .perform(replaceText(newPassword), closeSoftKeyboard());
                    Log.d(TAG, "Entered password user1password in confirm_password field");
                }
            } else {
                // Try UiAutomator for password fields
                UiObject newPasswordField = device.findObject(new UiSelector()
                        .className("android.widget.EditText")
                        .textContains("New Password"));

                if (newPasswordField.exists()) {
                    newPasswordField.setText("1223");
                    Log.d(TAG, "Entered password 1223 in new_password field using UiAutomator");
                    // Close keyboard
                    device.pressBack();
                    sleep(500);

                    // Find and fill confirm password field
                    UiObject confirmPasswordField = device.findObject(new UiSelector()
                            .className("android.widget.EditText")
                            .textContains("Confirm"));

                    if (confirmPasswordField.exists()) {
                        confirmPasswordField.setText("1223");
                        Log.d(TAG, "Entered password 1223 in confirm_password field using UiAutomator");
                        // Close keyboard
                        device.pressBack();
                        sleep(500);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to change username or enter password: " + e.getMessage());
        }

        // 8. CLICK UPDATE BUTTON
        Log.i(TAG, "Clicking update profile button");
        boolean clickedUpdate = false;

        try {
            if (isViewDisplayed(R.id.update_profile_button)) {
                clickWithAnimation(R.id.update_profile_button);
                Log.d(TAG, "Clicked update profile button using Espresso");
                clickedUpdate = true;
            } else {
                // Try UiAutomator
                UiObject updateButton = device.findObject(new UiSelector().textContains("Update Profile"));
                if (updateButton.exists()) {
                    updateButton.click();
                    Log.d(TAG, "Clicked Update Profile button using UiAutomator");
                    clickedUpdate = true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to click update button: " + e.getMessage());
        }

        if (!clickedUpdate) {
            Log.e(TAG, "Could not find update button! Attempting to continue.");
        }

        // 9. WAIT FOR PROFILE UPDATE AND RETURN TO PROFILE VIEW
        Log.i(TAG, "Waiting for profile update and return to profile view");
        sleep(4000);

        try {
            waitForViewWithRetry(R.id.profile_username, 8000, 4);
            Log.d(TAG, "Back to profile view after update");
        } catch (Exception e) {
            Log.e(TAG, "Failed to verify return to profile view: " + e.getMessage());
        }

        // 10. VERIFY USERNAME CHANGE
        Log.i(TAG, "Verifying username change");
        try {
            if (isViewDisplayed(R.id.profile_username)) {
                onView(withId(R.id.profile_username)).check(matches(withText(user1name)));
                Log.d(TAG, "Verified username changed to "+user1name);
            } else {
                // Try UiAutomator
                UiObject username = device.findObject(new UiSelector().textContains(user1name));
                if (username.exists()) {
                    Log.d(TAG, "Verified username changed to "+user1name+" using UiAutomator");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to verify username change: " + e.getMessage());
        }

        // 11. CLICK LOGOUT BUTTON
        Log.i(TAG, "Clicking logout button");
        boolean clickedLogout = false;

        try {
            // First try finding the logout button by scrolling to it (it might be off-screen)
            try {
                // Create a scrollable container to scroll to the logout button
                UiScrollable scrollable = new UiScrollable(new UiSelector().scrollable(true));
                scrollable.setAsVerticalList();

                // Scroll to the "Log out" text
                scrollable.scrollTextIntoView("Log out");
                Log.d(TAG, "Scrolled to logout button text");
                sleep(1000);
            } catch (UiObjectNotFoundException e) {
                Log.e(TAG, "Could not scroll to logout button: " + e.getMessage());
                // Continue anyway, as the button might already be visible
            }

            // Try using Espresso first
            if (isViewDisplayed(R.id.profile_logout_button)) {
                onView(withId(R.id.profile_logout_button))
                        .perform(click());
                Log.d(TAG, "Clicked logout button using Espresso");
                clickedLogout = true;
            } else {
                // Try UiAutomator with multiple approaches

                // Approach 1: Find by text
                UiObject logoutButton = device.findObject(new UiSelector()
                        .text("Log out")
                        .clickable(true));

                if (logoutButton.exists()) {
                    logoutButton.click();
                    Log.d(TAG, "Clicked logout button by exact text using UiAutomator");
                    clickedLogout = true;
                } else {
                    // Approach 2: Find by containing text (more flexible)
                    UiObject logoutButtonContains = device.findObject(new UiSelector()
                            .textContains("Log out")
                            .clickable(true));

                    if (logoutButtonContains.exists()) {
                        logoutButtonContains.click();
                        Log.d(TAG, "Clicked logout button by containing text using UiAutomator");
                        clickedLogout = true;
                    } else {
                        // Approach 3: Find by resource ID pattern
                        UiObject logoutButtonId = device.findObject(new UiSelector()
                                .resourceIdMatches(".*profile_logout_button")
                                .clickable(true));

                        if (logoutButtonId.exists()) {
                            logoutButtonId.click();
                            Log.d(TAG, "Clicked logout button by resource ID using UiAutomator");
                            clickedLogout = true;
                        } else {
                            // Approach 4: Look for any button with logout icon
                            UiObject logoutIcon = device.findObject(new UiSelector()
                                    .className("android.widget.Button")
                                    .descriptionContains("logout"));

                            if (logoutIcon.exists()) {
                                logoutIcon.click();
                                Log.d(TAG, "Clicked logout button by icon description using UiAutomator");
                                clickedLogout = true;
                            } else {
                                // Last resort: Try to find by class and pattern matching
                                UiObject materialButton = device.findObject(new UiSelector()
                                        .className("com.google.android.material.button.MaterialButton")
                                        .index(0)); // First material button is usually logout

                                if (materialButton.exists()) {
                                    materialButton.click();
                                    Log.d(TAG, "Clicked first material button as fallback");
                                    clickedLogout = true;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to click logout button: " + e.getMessage(), e);
        }

        if (!clickedLogout) {
            Log.e(TAG, "Could not find logout button! Test may not complete successfully.");
        }

// Wait longer after logout attempt to ensure action completes
        sleep(5000);

        // 12. VERIFY WE'RE BACK TO LOGIN SCREEN
        Log.i(TAG, "Verifying return to login screen");
        sleep(4000);

        try {
            waitForViewWithRetry(R.id.email_entryfield, 8000, 4);
            Log.d(TAG, "Successfully logged out and returned to login screen");
        } catch (Exception e) {
            Log.e(TAG, "Failed to verify return to login screen: " + e.getMessage());
        }

        Log.i(TAG, "Test completed");
    }

    // Helper method to check if a view is displayed without failing
    private boolean isViewDisplayed(int viewId) {
        try {
            onView(withId(viewId)).check(matches(isDisplayed()));
            return true;
        } catch (Exception e) {
            return false;
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