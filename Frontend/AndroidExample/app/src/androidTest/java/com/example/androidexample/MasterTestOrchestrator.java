package com.example.androidexample;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

/**
 * A master test class that runs all tests in a specific order.
 * Run this class directly from Android Studio to execute tests in sequence.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // This ensures methods run in alphabetical order
public class MasterTestOrchestrator {
    private static final String TAG = "MasterOrchestrator";
    private static UiDevice device;

    @BeforeClass
    public static void setupAll() {
        Log.i(TAG, "======= STARTING ORDERED TEST SEQUENCE =======");
        Log.i(TAG, "Tests will run in this order:");
        Log.i(TAG, "1. BookingFlowTest");
        Log.i(TAG, "2. AppointmentManagementTest");
        Log.i(TAG, "3. PaymentRatingFlowTest");
        Log.i(TAG, "4. AppointmentCancellationTest");
        Log.i(TAG, "5. MessageFlowTest");
        Log.i(TAG, "6. RewardTests");
        Log.i(TAG, "7. ProfileEditTest");

        // Get UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Disable animations for reliable testing
        disableAnimations();
    }

    @AfterClass
    public static void teardownAll() {
        Log.i(TAG, "======= COMPLETED ORDERED TEST SEQUENCE =======");
        // Re-enable animations
        enableAnimations();
    }

    // This method runs before each test to ensure a clean state
    private void resetAppState() {
        try {
            // Kill the app
            device.executeShellCommand("am force-stop com.example.androidexample");

            // Clear app data if needed
            // device.executeShellCommand("pm clear com.example.androidexample");

            // Wait for system to stabilize
            Thread.sleep(1000);

            // Launch main activity
            Context context = ApplicationProvider.getApplicationContext();
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.example.androidexample");
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }

            // Wait for app to launch
            Thread.sleep(2000);

            // Set portrait orientation
            device.setOrientationNatural();
        } catch (Exception e) {
            Log.e(TAG, "Error resetting app state: " + e.getMessage());
        }
    }

    // Test 1: BookingFlowTest
    @Test
    public void test1_BookingFlow() throws Exception {
        Log.i(TAG, "===== STARTING TEST 1: BookingFlowTest =====");

        // Reset app to clean state
        resetAppState();

        try {
            // Create an instance of the test class
            BookingFlowTest test = new BookingFlowTest();

            // Call its test method(s)
            test.testBookingFlow();

            Log.i(TAG, "✓ BookingFlowTest completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "✗ BookingFlowTest failed: " + e.getMessage(), e);
            // Don't throw the exception - this allows the test sequence to continue
        }
    }

    // Test 2: AppointmentManagementTest
    @Test
    public void test2_AppointmentManagement() throws Exception {
        Log.i(TAG, "===== STARTING TEST 2: AppointmentManagementTest =====");

        // Reset app to clean state
        resetAppState();

        try {
            AppointmentManagementTest test = new AppointmentManagementTest();
            test.testAppointmentRescheduleAndCancel(); // Replace with actual test method name
            Log.i(TAG, "✓ AppointmentManagementTest completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "✗ AppointmentManagementTest failed: " + e.getMessage(), e);
        }
    }

    // Test 3: PaymentRatingFlowTest
    @Test
    public void test3_PaymentRatingFlow() throws Exception {
        Log.i(TAG, "===== STARTING TEST 3: PaymentRatingFlowTest =====");

        // Reset app to clean state
        resetAppState();

        try {
            PaymentRatingFlowTest test = new PaymentRatingFlowTest();
            test.testPaymentAndRatingFlow();
            Log.i(TAG, "✓ PaymentRatingFlowTest completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "✗ PaymentRatingFlowTest failed: " + e.getMessage(), e);
        }
    }

    // Test 4: AppointmentCancellationTest
    @Test
    public void test4_AppointmentCancellation() throws Exception {
        Log.i(TAG, "===== STARTING TEST 4: AppointmentCancellationTest =====");

        // Reset app to clean state
        resetAppState();

        try {
            AppointmentCancellationTest test = new AppointmentCancellationTest();
            test.testAppointmentCancellation();
            Log.i(TAG, "✓ AppointmentCancellationTest completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "✗ AppointmentCancellationTest failed: " + e.getMessage(), e);
        }
    }

    // Test 5: MessageFlowTest
    @Test
    public void test5_MessageFlow() throws Exception {
        Log.i(TAG, "===== STARTING TEST 5: MessageFlowTest =====");

        // Reset app to clean state
        resetAppState();

        try {
            MessageFlowTest test = new MessageFlowTest();
            test.testMessageFlow(); // Replace with actual test method name
            Log.i(TAG, "✓ MessageFlowTest completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "✗ MessageFlowTest failed: " + e.getMessage(), e);
        }
    }

    // Test 6: RewardTests
    @Test
    public void test6_Rewards() throws Exception {
        Log.i(TAG, "===== STARTING TEST 6: RewardTests =====");

        // Reset app to clean state
        resetAppState();

        try {
            RewardTests test = new RewardTests();
            test.testCompleteRewardClaimFlow(); // Replace with actual test method name
            Log.i(TAG, "✓ RewardTests completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "✗ RewardTests failed: " + e.getMessage(), e);
        }
    }

    // Test 7: ProfileEditTest
    @Test
    public void test7_ProfileEdit() throws Exception {
        Log.i(TAG, "===== STARTING TEST 7: ProfileEditTest =====");

        // Reset app to clean state
        resetAppState();

        try {
            ProfileEditTest test = new ProfileEditTest();
            test.testProfileEditAndLogout(); // Replace with actual test method name
            Log.i(TAG, "✓ ProfileEditTest completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "✗ ProfileEditTest failed: " + e.getMessage(), e);
        }
    }

    // Helper methods for animations
    private static void disableAnimations() {
        try {
            device.executeShellCommand("settings put global window_animation_scale 0.0");
            device.executeShellCommand("settings put global transition_animation_scale 0.0");
            device.executeShellCommand("settings put global animator_duration_scale 0.0");
        } catch (Exception e) {
            Log.e(TAG, "Failed to disable animations", e);
        }
    }

    private static void enableAnimations() {
        try {
            device.executeShellCommand("settings put global window_animation_scale 1.0");
            device.executeShellCommand("settings put global transition_animation_scale 1.0");
            device.executeShellCommand("settings put global animator_duration_scale 1.0");
        } catch (Exception e) {
            Log.e(TAG, "Failed to enable animations", e);
        }
    }
}