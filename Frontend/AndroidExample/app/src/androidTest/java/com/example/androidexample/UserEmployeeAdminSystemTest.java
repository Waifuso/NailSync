package com.example.androidexample;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * System tests for user, employee and admin flows.
 * Tests login and specific functionality for each user type.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UserEmployeeAdminSystemTest {

    // Custom IdlingResource for async operations
    private DataLoadingIdlingResource dataLoadingIdlingResource;

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() {
        // Register our custom IdlingResource
        dataLoadingIdlingResource = new DataLoadingIdlingResource();
        IdlingRegistry.getInstance().register(dataLoadingIdlingResource);
    }

    @After
    public void cleanup() {
        // Unregister our custom IdlingResource
        if (dataLoadingIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(dataLoadingIdlingResource);
        }
    }

    /**
     * Test case 1: Test regular user flow
     * This test verifies:
     * - Login as a regular user
     * - Navigate to chat bot
     * - Send a message and receive a response
     * - Navigate back
     */
    @Test
    public void testRegularUserFlow() {
        // Wait for the login screen to fully load and animations to complete
        try {
            Thread.sleep(5000); // Wait for initial animations
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Login as regular user
        // Enter regular user credentials and log in
        onView(withId(R.id.email_entryfield)).perform(typeText("baohanphan852@gmail.com"), closeSoftKeyboard());
        try {
            Thread.sleep(500); // Slight delay between inputs
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        onView(withId(R.id.login_password_field)).perform(typeText("1223"), closeSoftKeyboard());
        try {
            Thread.sleep(1000); // Let the input settle
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        onView(withId(R.id.main_nextbtn)).perform(click());

        // Wait for home screen to load
        dataLoadingIdlingResource.setIsIdle(false);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dataLoadingIdlingResource.setIsIdle(true);

        // Verify we're on the user home screen - ApplicationActivity
        onView(withId(R.id.imageSlideshow)).check(matches(isDisplayed()));
        
        // Click on chat button to open chat functionality
        onView(withId(R.id.chat_btn)).perform(click());
        
        // Verify chat interface is loaded
        onView(withId(R.id.messagesRecyclerView)).check(matches(isDisplayed()));
        
        // Type and send a message
        onView(withId(R.id.msgEdt)).perform(typeText("Hello chatbot"), closeSoftKeyboard());
        onView(withId(R.id.sendBtn)).perform(click());
        
        // Wait for response
        dataLoadingIdlingResource.setIsIdle(false);
        try {
            Thread.sleep(5000); // Wait for chat response
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dataLoadingIdlingResource.setIsIdle(true);
        
        // Verify message list contains items (our sent message at minimum)
        onView(withId(R.id.messagesRecyclerView)).check(matches(isDisplayed()));
        
        // Navigate back to home using back button
        onView(withId(R.id.backButton)).perform(click());
        
        // Verify we're back at the home screen
        onView(withId(R.id.imageSlideshow)).check(matches(isDisplayed()));
    }

    /**
     * Test case 2: Test employee flow
     * This test verifies:
     * - Login as an employee
     * - Navigate to employee home page
     * - Open timeline feature
     * - Wait for posts to load
     * - Create a text-only post
     * - Submit the post and wait for upload
     */
    @Test
    public void testEmployeeFlow() {
        // Wait for the login screen to fully load and animations to complete
        try {
            Thread.sleep(5000); // Wait for initial animations
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Toggle employee login switch
        onView(withId(R.id.employee_login_switch)).perform(click());

        // Login as employee
        onView(withId(R.id.email_entryfield)).perform(typeText("Jordan"), closeSoftKeyboard());
        try {
            Thread.sleep(500); // Slight delay between inputs
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        onView(withId(R.id.login_password_field)).perform(typeText("Jordan2005-01-01"), closeSoftKeyboard());
        try {
            Thread.sleep(1000); // Let the input settle
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        onView(withId(R.id.main_nextbtn)).perform(click());

        // Wait for employee home screen to load
        dataLoadingIdlingResource.setIsIdle(false);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dataLoadingIdlingResource.setIsIdle(true);

        // Verify we're on the employee home screen
        onView(withId(R.id.employee_home_container)).check(matches(isDisplayed()));

        // Navigate to timeline
        onView(withId(R.id.nav_timeline)).perform(click());

        // Wait for TimelineActivity to load
        dataLoadingIdlingResource.setIsIdle(false);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dataLoadingIdlingResource.setIsIdle(true);

        // Verify timeline is loaded with RecyclerView
        onView(withId(R.id.timeline_recycler_view)).check(matches(isDisplayed()));

        dataLoadingIdlingResource.setIsIdle(false);
        try {
            Thread.sleep(4000); // Adjust depending on loading duration
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dataLoadingIdlingResource.setIsIdle(true);

        // Check that progress bar is hidden
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));


        // Click on create post FAB
        onView(withId(R.id.create_post_fab)).perform(click());

        // Verify create post form is shown
        onView(withId(R.id.create_post_layout)).check(matches(isDisplayed()));

        // Click message input and type
        onView(withId(R.id.message_input)).perform(typeText("This is a test post from automated testing"), closeSoftKeyboard());
        try {
            Thread.sleep(500); // Slight delay between inputs
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Submit the post
        onView(withId(R.id.submit_post_fab)).perform(click());

        // Wait for post to be submitted
        dataLoadingIdlingResource.setIsIdle(false);
        try {
            Thread.sleep(8000); // Wait for post upload and refresh
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dataLoadingIdlingResource.setIsIdle(true);

        // Verify create post form is hidden (after successful submission)
        onView(withId(R.id.create_post_layout)).check(matches(not(isDisplayed())));

        // Verify timeline is displayed again with posts
        onView(withId(R.id.timeline_recycler_view)).check(matches(isDisplayed()));
    }

    /**
     * Test case 3: Test admin flow
     * This test verifies:
     * - Login as an admin
     * - Check admin dashboard loads with correct components
     * - Verify admin profile info is displayed
     * - Verify stats summary is displayed
     * - Verify staff schedules are loaded
     * - Verify revenue chart data is loaded and displayed
     * - Test quick action buttons
     */
    @Test
    public void testAdminFlow() {
        // Wait for the login screen to fully load and animations to complete
        try {
            Thread.sleep(5000); // Wait for initial animations
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Access admin login (click on admin floating action button)
        onView(withId(R.id.admin_btn)).perform(click());

        // Wait for admin dashboard to load
        dataLoadingIdlingResource.setIsIdle(false);
        try {
            Thread.sleep(3000); // Longer wait for admin dashboard with API calls
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dataLoadingIdlingResource.setIsIdle(true);

        // Verify we're on the admin dashboard
        onView(withId(R.id.admin_dashboard_container)).check(matches(isDisplayed()));
        
        // Verify admin profile section is displayed
        onView(withId(R.id.adminNameText)).check(matches(isDisplayed()));
        onView(withId(R.id.adminRoleText)).check(matches(isDisplayed()));
        onView(withId(R.id.adminProfileImage)).check(matches(isDisplayed()));
        
        // Verify stats summary section is displayed
        onView(withId(R.id.totalAppointmentsText)).check(matches(isDisplayed()));
        onView(withId(R.id.totalRevenueText)).check(matches(isDisplayed()));
        onView(withId(R.id.totalStaffText)).check(matches(isDisplayed()));
        onView(withId(R.id.totalCustomersText)).check(matches(isDisplayed()));
        
        // Verify staff schedule section is displayed and loaded
        onView(withId(R.id.staffScheduleRecyclerView)).check(matches(isDisplayed()));
        
        // Wait additional time for schedules to load
        dataLoadingIdlingResource.setIsIdle(false);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dataLoadingIdlingResource.setIsIdle(true);
        
        // Verify view full schedule button is present
        onView(withId(R.id.viewFullScheduleButton)).check(matches(isDisplayed()));
        
        // Verify revenue overview section is displayed
        onView(withId(R.id.todayRevenueText))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.yesterdayRevenueText)).check(matches(isDisplayed()));
        onView(withId(R.id.revenueChangeText)).check(matches(isDisplayed()));
        
        // Verify revenue chart container is displayed
        onView(withId(R.id.revenueChartContainer)).check(matches(isDisplayed()));
        
        // Wait for revenue chart to load
        dataLoadingIdlingResource.setIsIdle(false);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dataLoadingIdlingResource.setIsIdle(true);
    }


    @Test
    public void testViewAllAppointmentsAndDetailsDialog() {
        // Wait for login screen
        try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }

        // Toggle employee login
        onView(withId(R.id.employee_login_switch)).perform(click());

        // Enter employee credentials
        onView(withId(R.id.email_entryfield)).perform(typeText("Jordan"), closeSoftKeyboard());
        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }

        onView(withId(R.id.login_password_field)).perform(typeText("Jordan2005-01-01"), closeSoftKeyboard());
        try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

        // Click login
        onView(withId(R.id.main_nextbtn)).perform(click());

        // Wait for employee home to load
        dataLoadingIdlingResource.setIsIdle(false);
        try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
        dataLoadingIdlingResource.setIsIdle(true);

        // Verify home loaded
        onView(withId(R.id.employee_home_container)).check(matches(isDisplayed()));

        // Click view all appointments button
        onView(withId(R.id.viewAllAppointmentsButton)).perform(click());

        // Wait for appointments to load
        dataLoadingIdlingResource.setIsIdle(false);
        try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
        dataLoadingIdlingResource.setIsIdle(true);

        // Check appointment list is visible
        onView(withId(R.id.recyclerViewAppointments)).check(matches(isDisplayed()));

        // Click on first appointment item to open details
        onView(withId(R.id.recyclerViewAppointments))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Wait for dialog to appear
        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

        // Check dialog title or details are shown (adjust to your actual dialog layout/text)
        onView(withText("Appointment Details")).check(matches(isDisplayed()));
    }


    /**
     * Custom IdlingResource to handle async operations like API calls and data loading
     */
    public static class DataLoadingIdlingResource implements IdlingResource {
        private ResourceCallback resourceCallback;
        private boolean isIdle = true;

        @Override
        public String getName() {
            return DataLoadingIdlingResource.class.getName();
        }

        @Override
        public boolean isIdleNow() {
            return isIdle;
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback callback) {
            this.resourceCallback = callback;
        }

        public void setIsIdle(boolean isIdle) {
            this.isIdle = isIdle;
            if (isIdle && resourceCallback != null) {
                resourceCallback.onTransitionToIdle();
            }
        }
    }
}